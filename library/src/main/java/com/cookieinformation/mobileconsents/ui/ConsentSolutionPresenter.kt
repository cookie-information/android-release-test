package com.cookieinformation.mobileconsents.ui

import android.content.Context
import androidx.annotation.MainThread
import com.cookieinformation.mobileconsents.BuildConfig
import com.cookieinformation.mobileconsents.Consent
import com.cookieinformation.mobileconsents.ConsentItem
import com.cookieinformation.mobileconsents.ConsentSolution
import com.cookieinformation.mobileconsents.MobileConsentSdk
import com.cookieinformation.mobileconsents.ProcessingPurpose
import com.cookieinformation.mobileconsents.TextTranslation
import com.cookieinformation.mobileconsents.storage.Preferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Locale
import java.util.UUID

internal typealias GivenConsent = Map<UUID, Pair<Boolean, String>>

/**
 * Base presenter for the view that displays [ConsentSolution] and allows the user to chose and save the consents.
 */
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

  private lateinit var applicationContext: Context
  private lateinit var consentSdk: MobileConsentSdk
  private lateinit var consentSolutionId: UUID
  private lateinit var localeProvider: LocaleProvider
  private lateinit var preferences: Preferences
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

  /**
   * Attaches the view to the presenter.
   */
  @MainThread
  fun attachView(view: ViewType) {
    require(this.view == null)
    this.view = view
    view.addIntentListener(getViewIntentListener())
    viewState = viewState // force update view
  }

  /**
   * Detaches curentlly attached view from the presenter.
   */
  @MainThread
  fun detachView() {
    requireNotNull(view)
    view?.removeIntentListener(getViewIntentListener())
    view = null
  }

  /**
   * Initializes the presenter.
   *
   * @param consentSdk instance of the [MobileConsentSdk].
   * @param consentSolutionId [UUID] of the consent solution.
   * @param localeProvider implementation of the [LocaleProvider].
   * @param listener implementation of the presenters event handler - [ConsentSolutionListener].
   */
  @MainThread
  fun initialize(
    applicationContext: Context,
    consentSdk: MobileConsentSdk,
    consentSolutionId: UUID,
    localeProvider: LocaleProvider,
    listener: ConsentSolutionListener
  ) {
    this.applicationContext = applicationContext
    this.consentSdk = consentSdk
    this.consentSolutionId = consentSolutionId
    this.localeProvider = localeProvider
    this.listener = listener
    preferences = Preferences(applicationContext)
    observeConsents()
  }

  private fun observeConsents() {
    consentSdk.saveConsentsFlow
      .onEach {
        // For the presenter, changes are only useful when data are shown. Loading states can be ignored
        when (val currentViewState = viewState) {
          is ViewState.Fetched<*> -> {
            onConsentsChangedWhileFetched(it)
            listener?.onConsentsChosen(currentViewState.consentSolution, it, true)
          }
          is ViewState.SendError<*> -> {
            onConsentsChangedWhileSendError(it)
            listener?.onConsentsChosen(currentViewState.consentSolution, it, true)
          }
          else -> Unit
        }
      }
      .launchIn(scope)
  }

  /**
   * Disposes the instance. After calling this method the instance can not be used again.
   */
  @MainThread
  fun dispose() {
    scope.cancel()
    listener = null
    view = null
  }

  /**
   * Authenticate the client id with secret key and save the access token.
   */
  fun authenticate() {
    // First check that client id and secret key are present
    if (BuildConfig.CLIENT_ID.isBlank() || BuildConfig.CLIENT_SECRET.isBlank() || BuildConfig.SOLUTION_ID.isBlank()) {
      throw RuntimeException("\nlocal.properties is missing client id and/or client secret and/or solution id. Please add:\nCLIENT_ID = \"XXX\"\nCLIENT_SECRET = \"XXX\"\nSOLUTION_ID = \"XXX\"")
    }

    preferences.getAccessToken()?.let {
      // We have a valid access token
      fetch()
    } ?: fetchToken()
  }

  private fun fetchToken() {
    // TODO fetch token and then authenticate again
    scope.launch {
      val tokenResponse = consentSdk.authenticate()
      preferences.setTokenResponse(tokenResponse)
    }
    //authenticate()
  }

  /**
   * Fetches the consent solution from server, reads saved choices and shows data if there is an attached view.
   */
  fun fetch() {
    scope.launch {
      try {
        viewState = ViewState.Fetching
        val consentSolution = consentSdk.fetchConsentSolution(consentSolutionId)
        viewState = ViewState.Fetched(createViewData(consentSolution, consentSdk.getSavedConsents()), consentSolution)
      } catch (_: IOException) {
        viewState = ViewState.FetchError
      }
    }
  }

  /**
   * Sends the user choice to server and saves it locally.
   */
  fun send() {
    scope.launch {
      try {
        @Suppress("UNCHECKED_CAST")
        val currentViewState = viewState as ViewState.Fetched<ViewDataType>
        viewState = ViewState.Sending(currentViewState.data, currentViewState.consentSolution)
        consentSdk.postConsent(createConsent())
        listener?.onConsentsChosen(currentViewState.consentSolution, consentSdk.getSavedConsents(), false)
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

  protected abstract fun onConsentsChangedWhileFetched(consents: Map<UUID, Boolean>)

  protected abstract fun onConsentsChangedWhileSendError(consents: Map<UUID, Boolean>)
}
