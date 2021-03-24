package com.cookieinformation.mobileconsents.ui

import androidx.annotation.MainThread
import com.cookieinformation.mobileconsents.Consent
import com.cookieinformation.mobileconsents.ConsentItem
import com.cookieinformation.mobileconsents.ConsentItem.Type.Setting
import com.cookieinformation.mobileconsents.ConsentSolution
import com.cookieinformation.mobileconsents.MobileConsentSdk
import com.cookieinformation.mobileconsents.ProcessingPurpose
import com.cookieinformation.mobileconsents.TextTranslation
import com.cookieinformation.mobileconsents.UiTexts
import com.cookieinformation.mobileconsents.ui.PrivacyPreferencesView.ButtonId
import com.cookieinformation.mobileconsents.ui.PrivacyPreferencesView.ButtonId.AcceptAll
import com.cookieinformation.mobileconsents.ui.PrivacyPreferencesView.ButtonId.AcceptSelected
import com.cookieinformation.mobileconsents.ui.PrivacyPreferencesView.ButtonId.ReadMore
import com.cookieinformation.mobileconsents.ui.PrivacyPreferencesView.ButtonId.RejectAll
import com.cookieinformation.mobileconsents.ui.PrivacyPreferencesViewData.ButtonState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID

private const val cookieInformationUrl = "https://cookieinformation.com"

internal class PrivacyPreferencesPresenter(
  dispatcher: CoroutineDispatcher = Dispatchers.Main
) : PrivacyPreferencesView.IntentListener {

  private sealed class ViewState {

    object Idle : ViewState()

    object Fetching : ViewState()

    object FetchError : ViewState()

    data class Fetched(
      val data: PrivacyPreferencesViewData,
      val consentSolution: ConsentSolution,
    ) : ViewState()

    data class Sending(
      val data: PrivacyPreferencesViewData,
      val consentSolution: ConsentSolution,
    ) : ViewState()

    data class SendError(
      val data: PrivacyPreferencesViewData,
      val consentSolution: ConsentSolution,
    ) : ViewState()
  }

  private var privacyPreferencesView: PrivacyPreferencesView? = null

  private lateinit var consentSdk: MobileConsentSdk
  private lateinit var consentSolutionId: UUID
  private lateinit var localeProvider: LocaleProvider
  private var listener: PrivacyPreferencesListener? = null

  private val scope = CoroutineScope(dispatcher)

  private var viewState: ViewState = ViewState.Idle
    set(value) {
      field = value
      privacyPreferencesView?.let { view ->
        when (value) {
          ViewState.Idle -> Unit
          ViewState.Fetching -> handleLoading(view)
          is ViewState.Fetched -> handleFetched(view, value)
          ViewState.FetchError -> handleFetchError(view)
          is ViewState.Sending -> handleSending(view, value)
          is ViewState.SendError -> handleSendError(view, value)
        }
      }
    }

  private fun handleLoading(view: PrivacyPreferencesView) = with(view) {
    hideViewData()
    showProgressBar()
  }

  private fun handleFetched(view: PrivacyPreferencesView, state: ViewState.Fetched) = with(view) {
    hideProgressBar()
    showViewData(state.data)
  }

  private fun handleFetchError(view: PrivacyPreferencesView) = with(view) {
    hideProgressBar()
    hideViewData()
    showRetryDialog(
      onRetry = ::fetch,
      onDismiss = { listener?.onDismissed() }
    )
  }

  private fun handleSending(view: PrivacyPreferencesView, state: ViewState.Sending) = with(view) {
    showViewData(state.data)
    showProgressBar()
  }

  private fun handleSendError(view: PrivacyPreferencesView, state: ViewState.SendError) = with(view) {
    hideProgressBar()
    showViewData(state.data)
    showErrorDialog {
      val currentViewState = viewState
      require(currentViewState is ViewState.SendError)
      viewState = ViewState.Fetched(currentViewState.data, currentViewState.consentSolution)
    }
  }

  @MainThread
  fun attachView(view: PrivacyPreferencesView) {
    require(privacyPreferencesView == null)
    privacyPreferencesView = view
    view.addIntentListener(this)
    viewState = viewState // force update view
  }

  @MainThread
  fun detachView() {
    requireNotNull(privacyPreferencesView)
    privacyPreferencesView?.removeIntentListener(this)
    privacyPreferencesView = null
  }

  override fun onPrivacyPreferenceChoiceChanged(id: UUID, accepted: Boolean): Unit = viewState.let { currentViewState ->
    require(currentViewState is ViewState.Fetched)
    currentViewState.data.let { data ->
      val items = data.items.map { if (it.id == id) it.copy(accepted = accepted) else it }
      val newViewState = currentViewState.copy(
        data = data.copy(
          items = items,
          buttonAcceptSelected = data.buttonAcceptSelected.copy(
            enabled = areAllRequiredAccepted(items)
          )
        )
      )
      viewState = newViewState
    }
  }

  override fun onPrivacyPreferenceButtonClicked(buttonId: ButtonId): Unit =
    when (buttonId) {
      ReadMore -> readMore()
      RejectAll -> rejectAll()
      AcceptAll -> acceptAll()
      AcceptSelected -> acceptSelected()
    }

  @MainThread
  fun initialize(
    consentSdk: MobileConsentSdk,
    consentSolutionId: UUID,
    localeProvider: LocaleProvider,
    listener: PrivacyPreferencesListener
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
    privacyPreferencesView = null
  }

  fun fetch() {
    scope.launch {
      try {
        viewState = ViewState.Fetching

        val savedConsents = consentSdk.getSavedConsents()
        val consentSolution = consentSdk.fetchConsentSolution(consentSolutionId)
        val items = consentSolution.consentItems.filter { it.type == Setting }.map {
          mapConsentItem(it, savedConsents)
        }
        val data = createViewState(items, consentSolution.uiTexts)

        viewState = ViewState.Fetched(data, consentSolution)
      } catch (_: IOException) {
        viewState = ViewState.FetchError
      }
    }
  }

  private fun send() {
    scope.launch {
      try {
        val currentViewState = viewState as ViewState.Fetched
        viewState = ViewState.Sending(currentViewState.data, currentViewState.consentSolution)
        consentSdk.postConsent(createConsent())
        listener?.onConsentsChosen(consentSdk.getSavedConsents())
      } catch (_: IOException) {
        val currentViewState = viewState
        require(currentViewState is ViewState.Sending)
        viewState = ViewState.SendError(currentViewState.data, currentViewState.consentSolution)
      }
    }
  }

  private fun createConsent(): Consent = viewState.let { currentViewState ->
    require(currentViewState is ViewState.Sending)

    val consentSolution = currentViewState.consentSolution
    val givenConsents = currentViewState.data.items.map { it.id to Pair(it.accepted, it.language) }.toMap()

    Consent(
      consentSolutionId = consentSolution.consentSolutionId,
      consentSolutionVersionId = consentSolution.consentSolutionVersionId,
      processingPurposes = consentSolution.consentItems.map {
        val givenConsent = givenConsents[it.consentItemId] // not null => type == Setting
        processingPurpose(it, givenConsent)
      },
      customData = emptyMap()
    )
  }

  private fun processingPurpose(
    it: ConsentItem,
    givenConsent: Pair<Boolean, String>?
  ) = ProcessingPurpose(
    consentItemId = it.consentItemId,
    consentGiven = givenConsent?.first ?: true,
    language = givenConsent?.second ?: TextTranslation.getTranslationFor(
      it.shortText,
      localeProvider.getLocales()
    ).languageCode
  )

  private fun readMore() {
    listener?.onReadMore()
  }

  private fun rejectAll(): Unit = viewState.let { currentViewState ->
    require(currentViewState is ViewState.Fetched)
    currentViewState.data.let { data ->
      val items = data.items.map { it.copy(accepted = false) }
      val newViewState = currentViewState.copy(
        data = data.copy(
          items = items,
          buttonAcceptSelected = data.buttonAcceptSelected.copy(
            enabled = areAllRequiredAccepted(items)
          )
        )
      )
      viewState = newViewState
      if (areAllRequiredAccepted(items)) {
        send()
      }
    }
  }

  private fun acceptAll(): Unit = viewState.let { currentViewState ->
    require(currentViewState is ViewState.Fetched)
    currentViewState.data.let { data ->
      val items = data.items.map { it.copy(accepted = true) }
      val newViewState = currentViewState.copy(
        data = data.copy(
          items = items,
          buttonAcceptSelected = data.buttonAcceptSelected.copy(
            enabled = true
          )
        )
      )
      viewState = newViewState
      send()
    }
  }

  private fun acceptSelected(): Unit = viewState.let { currentViewState ->
    require(currentViewState is ViewState.Fetched)
    require(areAllRequiredAccepted(currentViewState.data.items))
    send()
  }

  private fun areAllRequiredAccepted(items: List<PrivacyPreferencesItem>): Boolean =
    items.firstOrNull { it.required && !it.accepted } == null

  private fun areAllNotRequired(items: List<PrivacyPreferencesItem>): Boolean =
    items.firstOrNull { it.required } == null

  private fun mapTextTranslation(translations: List<TextTranslation>): String =
    TextTranslation.getTranslationFor(translations, localeProvider.getLocales()).text

  private fun createViewState(items: List<PrivacyPreferencesItem>, uiTexts: UiTexts) =
    PrivacyPreferencesViewData(
      title = mapTextTranslation(uiTexts.privacyPreferencesTitle),
      subTitle = "<a href=\"$cookieInformationUrl\">${mapTextTranslation(uiTexts.poweredByLabel)}</a>",
      description = mapTextTranslation(uiTexts.privacyPreferencesDescription),
      items = items,
      buttonReadMore = ButtonState(mapTextTranslation(uiTexts.privacyCenterButton), true),
      buttonAcceptAll = ButtonState(mapTextTranslation(uiTexts.acceptAllButton), true),
      buttonRejectAll = ButtonState(mapTextTranslation(uiTexts.rejectAllButton), areAllNotRequired(items)),
      buttonAcceptSelected = ButtonState(
        mapTextTranslation(uiTexts.acceptSelectedButton),
        areAllRequiredAccepted(items)
      ),
    )

  private fun mapConsentItem(item: ConsentItem, savedConsents: Map<UUID, Boolean>): PrivacyPreferencesItem {
    val translation = TextTranslation.getTranslationFor(item.shortText, localeProvider.getLocales())
    return PrivacyPreferencesItem(
      id = item.consentItemId,
      required = item.required,
      accepted = savedConsents[item.consentItemId] ?: false,
      text = translation.text,
      details = "",
      language = translation.languageCode
    )
  }
}
