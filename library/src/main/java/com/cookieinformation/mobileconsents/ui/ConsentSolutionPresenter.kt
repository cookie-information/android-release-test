package com.cookieinformation.mobileconsents.ui

import androidx.annotation.MainThread
import com.cookieinformation.mobileconsents.Consent
import com.cookieinformation.mobileconsents.ConsentItem
import com.cookieinformation.mobileconsents.ConsentSolution
import com.cookieinformation.mobileconsents.MobileConsentSdk
import com.cookieinformation.mobileconsents.ProcessingPurpose
import com.cookieinformation.mobileconsents.TextTranslation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Locale
import java.util.UUID

internal typealias GivenConsent = Map<UUID, Pair<Boolean, String>>

internal abstract class ConsentSolutionPresenter<ViewType, ViewDataType, ViewIntentListenerType>(
  dispatcher: CoroutineDispatcher
) where ViewType : ConsentSolutionView<ViewDataType, ViewIntentListenerType> {

  protected val cookieInformationUrl = "https://cookieinformation.com"

  protected sealed class ViewState {

    object Idle : ViewState()

    object Fetching : ViewState()

    object FetchError : ViewState()

    data class Fetched<VD>(
      val data: VD,
      val consentSolution: ConsentSolution,
    ) : ViewState()

    data class Sending<VD>(
      val data: VD,
      val consentSolution: ConsentSolution,
    ) : ViewState()

    data class SendError<VD>(
      val data: VD,
      val consentSolution: ConsentSolution,
    ) : ViewState()
  }

  private var view: ViewType? = null

  private lateinit var consentSdk: MobileConsentSdk
  private lateinit var consentSolutionId: UUID
  private lateinit var localeProvider: LocaleProvider
  private val locales: List<Locale>
    get() = localeProvider.getLocales()

  protected var listener: ConsentSolutionListener? = null
    private set

  private val scope = CoroutineScope(dispatcher)

  @Suppress("UNCHECKED_CAST")
  protected var viewState: ViewState = ViewState.Idle
    set(value) {
      field = value
      view?.let { view ->
        when (value) {
          ViewState.Idle -> Unit
          ViewState.Fetching -> handleLoading(view)
          is ViewState.Fetched<*> -> handleFetched(view, value as ViewState.Fetched<ViewDataType>)
          ViewState.FetchError -> handleFetchError(view)
          is ViewState.Sending<*> -> handleSending(view, value as ViewState.Sending<ViewDataType>)
          is ViewState.SendError<*> -> handleSendError(view, value as ViewState.SendError<ViewDataType>)
        }
      }
    }

  private fun handleLoading(view: ViewType) = with(view) {
    hideViewData()
    showProgressBar()
  }

  private fun handleFetched(view: ViewType, state: ViewState.Fetched<ViewDataType>) = with(view) {
    hideProgressBar()
    showViewData(state.data)
  }

  private fun handleFetchError(view: ViewType) = with(view) {
    hideProgressBar()
    hideViewData()
    showRetryDialog(
      onRetry = ::fetch,
      onDismiss = { listener?.onDismissed() }
    )
  }

  private fun handleSending(view: ViewType, state: ViewState.Sending<ViewDataType>) = with(view) {
    showViewData(state.data)
    showProgressBar()
  }

  private fun handleSendError(view: ViewType, state: ViewState.SendError<ViewDataType>) = with(view) {
    hideProgressBar()
    showViewData(state.data)
    showErrorDialog {
      val currentViewState = viewState
      require(currentViewState is ViewState.SendError<*>)
      viewState = ViewState.Fetched(currentViewState.data, currentViewState.consentSolution)
    }
  }

  @MainThread
  fun attachView(view: ViewType) {
    require(this.view == null)
    this.view = view
    view.addIntentListener(getViewIntentListener())
    viewState = viewState // force update view
  }

  @MainThread
  fun detachView() {
    requireNotNull(view)
    view?.removeIntentListener(getViewIntentListener())
    view = null
  }

  @MainThread
  fun initialize(
    consentSdk: MobileConsentSdk,
    consentSolutionId: UUID,
    localeProvider: LocaleProvider,
    listener: ConsentSolutionListener
  ) {
    this.consentSdk = consentSdk
    this.consentSolutionId = consentSolutionId
    this.localeProvider = localeProvider
    this.listener = listener
  }

  @MainThread
  fun dispose() {
    scope.cancel()
    listener = null
    view = null
  }

  fun fetch() {
    scope.launch {
      try {
        viewState = ViewState.Fetching
        val savedConsents = consentSdk.getSavedConsents()
        val consentSolution = consentSdk.fetchConsentSolution(consentSolutionId)
        viewState = ViewState.Fetched(createViewData(consentSolution, savedConsents), consentSolution)
      } catch (_: IOException) {
        viewState = ViewState.FetchError
      }
    }
  }

  fun send() {
    scope.launch {
      try {
        @Suppress("UNCHECKED_CAST")
        val currentViewState = viewState as ViewState.Fetched<ViewDataType>
        viewState = ViewState.Sending(currentViewState.data, currentViewState.consentSolution)
        consentSdk.postConsent(createConsent())
        listener?.onConsentsChosen(consentSdk.getSavedConsents())
      } catch (_: IOException) {
        val currentViewState = viewState
        require(currentViewState is ViewState.Sending<*>)
        viewState = ViewState.SendError(currentViewState.data, currentViewState.consentSolution)
      }
    }
  }

  private fun createConsent(): Consent {
    @Suppress("UNCHECKED_CAST")
    val currentViewState = viewState as ViewState.Sending<ViewDataType>
    val consentSolution = currentViewState.consentSolution
    return Consent(
      consentSolutionId = consentSolution.consentSolutionId,
      consentSolutionVersionId = consentSolution.consentSolutionVersionId,
      processingPurposes = createProcessingPurposes(consentSolution, currentViewState.data),
      customData = emptyMap()
    )
  }

  private fun createProcessingPurposes(
    consentSolution: ConsentSolution,
    viewData: ViewDataType,
  ): List<ProcessingPurpose> {
    val givenConsents = getGivenConsents(viewData)
    return consentSolution.consentItems.map {
      val givenConsent = givenConsents[it.consentItemId] // not null => type == Setting
      it.toProcessingPurpose(givenConsent)
    }
  }

  private fun ConsentItem.toProcessingPurpose(givenConsent: Pair<Boolean, String>?) = ProcessingPurpose(
    consentItemId = consentItemId,
    consentGiven = givenConsent?.first ?: true,
    language = givenConsent?.second ?: TextTranslation.getTranslationFor(shortText, locales).languageCode
  )

  protected fun ConsentItem.toPrivacyPreferencesItem(savedConsents: Map<UUID, Boolean>): PrivacyPreferencesItem {
    val textTranslation = shortText.translate()

    return PrivacyPreferencesItem(
      id = consentItemId,
      required = required,
      accepted = savedConsents[consentItemId] ?: false,
      text = textTranslation.text,
      details = longText.translate().text,
      language = textTranslation.languageCode
    )
  }

  protected fun List<TextTranslation>.translate(): TextTranslation =
    TextTranslation.getTranslationFor(this, locales)

  protected abstract fun getViewIntentListener(): ViewIntentListenerType

  protected abstract fun createViewData(
    consentSolution: ConsentSolution,
    savedConsents: Map<UUID, Boolean>
  ): ViewDataType

  protected abstract fun getGivenConsents(viewData: ViewDataType): GivenConsent
}
