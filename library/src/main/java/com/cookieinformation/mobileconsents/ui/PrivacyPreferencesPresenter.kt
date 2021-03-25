package com.cookieinformation.mobileconsents.ui

import com.cookieinformation.mobileconsents.ConsentItem.Type.Setting
import com.cookieinformation.mobileconsents.ConsentSolution
import com.cookieinformation.mobileconsents.UiTexts
import com.cookieinformation.mobileconsents.ui.PrivacyPreferencesView.ButtonId
import com.cookieinformation.mobileconsents.ui.PrivacyPreferencesView.ButtonId.AcceptAll
import com.cookieinformation.mobileconsents.ui.PrivacyPreferencesView.ButtonId.AcceptSelected
import com.cookieinformation.mobileconsents.ui.PrivacyPreferencesView.ButtonId.ReadMore
import com.cookieinformation.mobileconsents.ui.PrivacyPreferencesView.ButtonId.RejectAll
import com.cookieinformation.mobileconsents.ui.PrivacyPreferencesView.IntentListener
import com.cookieinformation.mobileconsents.ui.PrivacyPreferencesViewData.ButtonState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.util.UUID

internal class PrivacyPreferencesPresenter(
  dispatcher: CoroutineDispatcher = Dispatchers.Main
) : ConsentSolutionPresenter<PrivacyPreferencesView, PrivacyPreferencesViewData, IntentListener>(dispatcher),
  IntentListener {

  override fun getViewIntentListener(): IntentListener = this

  override fun createViewData(
    consentSolution: ConsentSolution,
    savedConsents: Map<UUID, Boolean>
  ): PrivacyPreferencesViewData {
    val items = consentSolution.consentItems
      .filter { it.type == Setting }
      .map { it.toPrivacyPreferencesItem(savedConsents) }
    return createPrivacyPreferencesViewData(items, consentSolution.uiTexts)
  }

  override fun getGivenConsents(viewData: PrivacyPreferencesViewData): GivenConsent =
    viewData.items.map { it.id to Pair(it.accepted, it.language) }.toMap()

  override fun onPrivacyPreferenceChoiceChanged(id: UUID, accepted: Boolean) {
    @Suppress("UNCHECKED_CAST")
    val currentViewState = viewState as ViewState.Fetched<PrivacyPreferencesViewData>

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

  private fun readMore() {
    listener?.onReadMore()
  }

  private fun rejectAll() {
    @Suppress("UNCHECKED_CAST")
    val currentViewState = viewState as ViewState.Fetched<PrivacyPreferencesViewData>

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

  private fun acceptAll() {
    @Suppress("UNCHECKED_CAST")
    val currentViewState = viewState as ViewState.Fetched<PrivacyPreferencesViewData>

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

  private fun acceptSelected() {
    @Suppress("UNCHECKED_CAST")
    val currentViewState = viewState as ViewState.Fetched<PrivacyPreferencesViewData>
    require(areAllRequiredAccepted(currentViewState.data.items))

    send()
  }

  private fun areAllRequiredAccepted(items: List<PrivacyPreferencesItem>): Boolean =
    items.firstOrNull { it.required && !it.accepted } == null

  private fun areAllNotRequired(items: List<PrivacyPreferencesItem>): Boolean =
    items.firstOrNull { it.required } == null

  private fun createPrivacyPreferencesViewData(items: List<PrivacyPreferencesItem>, uiTexts: UiTexts) =
    PrivacyPreferencesViewData(
      title = uiTexts.privacyPreferencesTitle.translate().text,
      subTitle = "<a href=\"$cookieInformationUrl\">${uiTexts.poweredByLabel.translate().text}</a>",
      description = uiTexts.privacyPreferencesDescription.translate().text,
      items = items,
      buttonReadMore = ButtonState(uiTexts.privacyCenterButton.translate().text, true),
      buttonAcceptAll = ButtonState(uiTexts.acceptAllButton.translate().text, true),
      buttonRejectAll = ButtonState(uiTexts.rejectAllButton.translate().text, areAllNotRequired(items)),
      buttonAcceptSelected = ButtonState(
        uiTexts.acceptSelectedButton.translate().text,
        areAllRequiredAccepted(items)
      ),
    )
}
