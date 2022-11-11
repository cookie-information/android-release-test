package com.cookieinformation.mobileconsents.ui

import com.cookieinformation.mobileconsents.ConsentItem
import com.cookieinformation.mobileconsents.ConsentItem.Type.Info
import com.cookieinformation.mobileconsents.ConsentItem.Type.Setting
import com.cookieinformation.mobileconsents.ConsentSolution
import com.cookieinformation.mobileconsents.UiTexts
import com.cookieinformation.mobileconsents.ui.PrivacyFragmentItem.PrivacyFragmentDetailsItem
import com.cookieinformation.mobileconsents.ui.PrivacyFragmentItem.PrivacyFragmentInfoItem
import com.cookieinformation.mobileconsents.ui.PrivacyFragmentItem.PrivacyFragmentPreferencesItem
import com.cookieinformation.mobileconsents.ui.PrivacyFragmentView.IntentListener2
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.util.UUID

/**
 * The presenter for the [PrivacyCenterView] view.
 */
internal class PrivacyFragmentPresenter(
  dispatcher: CoroutineDispatcher = Dispatchers.Main
) : ConsentSolutionPresenter<PrivacyFragmentView, PrivacyFragmentViewData, IntentListener2>(dispatcher),
  IntentListener2 {

  private val preferencesItemId = UUID(0, 0)

  private val preferencesInitiallyExpanded = true

  private lateinit var preferencesItem: PrivacyFragmentPreferencesItem

  override fun getViewIntentListener(): IntentListener2 = this

  override fun createViewData(
    consentSolution: ConsentSolution,
    savedConsents: Map<UUID, Boolean>
  ): PrivacyFragmentViewData {
    preferencesItem = createPreferencesItem(consentSolution, savedConsents)
    return createPrivacyFragmentViewData(consentSolution.consentItems, consentSolution.uiTexts)
  }

  override fun getGivenConsents(viewData: PrivacyFragmentViewData): GivenConsent =
    preferencesItem.items.map { it.id to Pair(it.accepted, it.language) }.toMap()

  override fun onConsentsChangedWhileFetched(consents: Map<UUID, Boolean>) {
    @Suppress("UNCHECKED_CAST")
    val currentViewState = viewState as ViewState.Fetched<PrivacyFragmentViewData>

    if (currentViewState.data.items.last() is PrivacyFragmentPreferencesItem) {
      viewState = currentViewState.copy(
        data = newViewData(
          currentViewState.data,
          newPreferencesItems(preferencesItem.items, consents)
        )
      )
    } else {
      preferencesItem = preferencesItem.copy(items = newPreferencesItems(preferencesItem.items, consents))
    }
  }

  override fun onConsentsChangedWhileSendError(consents: Map<UUID, Boolean>) {
    @Suppress("UNCHECKED_CAST")
    val currentViewState = viewState as ViewState.SendError<PrivacyFragmentViewData>

    if (currentViewState.data.items.last() is PrivacyFragmentPreferencesItem) {
      viewState = currentViewState.copy(
        data = newViewData(
          currentViewState.data,
          newPreferencesItems(preferencesItem.items, consents)
        )
      )
    } else {
      preferencesItem = preferencesItem.copy(items = newPreferencesItems(preferencesItem.items, consents))
    }
  }

  private fun newPreferencesItems(
    items: List<PrivacyPreferencesItem>,
    consents: Map<UUID, Boolean>
  ): List<PrivacyPreferencesItem> =
    items.map {
      val accepted = consents[it.id] ?: false
      if (it.accepted != accepted) it.copy(accepted = accepted) else it
    }

  override fun onPrivacyCenterChoiceChanged(id: UUID, accepted: Boolean) {
    @Suppress("UNCHECKED_CAST")
    val currentViewState = viewState as? ViewState.Fetched<PrivacyFragmentViewData> ?: return
    require(currentViewState.data.items.last() is PrivacyFragmentPreferencesItem)

    val preferenceItems = preferencesItem.items.map { if (it.id == id) it.copy(accepted = accepted) else it }
    viewState = currentViewState.copy(data = newViewData(currentViewState.data, preferenceItems))
  }

  private fun newViewData(
    viewData: PrivacyFragmentViewData,
    preferenceItems: List<PrivacyPreferencesItem>
  ): PrivacyFragmentViewData {
    preferencesItem = preferencesItem.copy(items = preferenceItems)
    val newItems = mutableListOf<PrivacyFragmentItem>().apply {
      addAll(viewData.items)
      removeLast()
      add(preferencesItem)
    }

    return viewData.copy(
      items = newItems,
      acceptButtonEnabled = areAllRequiredAccepted(preferenceItems)
    )
  }

  override fun onPrivacyCenterDetailsToggle(id: UUID) {
    @Suppress("UNCHECKED_CAST")
    val currentViewState = viewState as? ViewState.Fetched<PrivacyFragmentViewData> ?: return
    val newItems = mutableListOf<PrivacyFragmentItem>().apply {
      addAll(currentViewState.data.items)
    }
    val itemIndex = newItems.indexOfFirst { it is PrivacyFragmentInfoItem && it.id == id }
    val item = newItems[itemIndex] as PrivacyFragmentInfoItem
    val (newItem, detailsItem) = item.setExpanded(!item.expanded)

    newItems.removeAt(itemIndex)
    newItems.add(itemIndex, newItem)
    if (detailsItem != null) newItems.add(itemIndex + 1, detailsItem) else newItems.removeAt(itemIndex + 1)

    viewState = currentViewState.copy(data = currentViewState.data.copy(items = newItems))
  }

  override fun onPrivacyCenterAcceptClicked() {
    @Suppress("UNCHECKED_CAST")
    viewState as? ViewState.Fetched<PrivacyFragmentViewData> ?: return
    require(areAllRequiredAccepted(preferencesItem.items))
    sendConsent()
  }

  override fun onPrivacyCenterDismissRequest() {
    listener?.onDismissed()
  }

  private fun PrivacyFragmentInfoItem.setExpanded(expanded: Boolean): Pair<PrivacyFragmentInfoItem, PrivacyFragmentItem?> {
    val infoItem = copy(expanded = expanded)
    val detailsItem = if (expanded) {
      if (id == preferencesItemId) {
        preferencesItem
      } else {
        createDetailsItem()
      }
    } else null
    return infoItem to detailsItem
  }

  private fun PrivacyFragmentInfoItem.createDetailsItem() = PrivacyFragmentDetailsItem(id = id, details = details)

  private fun createPreferencesItem(
    consentSolution: ConsentSolution,
    savedConsents: Map<UUID, Boolean>
  ): PrivacyFragmentPreferencesItem {
    val items = consentSolution.consentItems
      .filter { it.type == Setting }
      .map { it.toPrivacyPreferencesItem(savedConsents) }

    val titleTranslation = consentSolution.uiTexts.consentPreferencesLabel.translate()
    val subTitleTranslation = consentSolution.uiTexts.poweredByLabel.translate()

    return PrivacyFragmentPreferencesItem(
      id = preferencesItemId,
      title = titleTranslation.text,
      subTitle = "<a href=\"$cookieInformationUrl\">${subTitleTranslation.text}</a>",
      items = items,
    )
  }

  private fun createPrivacyFragmentViewData(
    consentItems: List<ConsentItem>,
    uiTexts: UiTexts
  ): PrivacyFragmentViewData {
    val items = consentItems
      .filter { it.type == Info }
      .map { it.toPrivacyFragmentInfoItem() }
      .toMutableList()

    items.add(createPrivacyPreferencesItem(uiTexts))
    if (preferencesInitiallyExpanded) {
      items.add(preferencesItem)
    }

    return PrivacyFragmentViewData(
      title = uiTexts.privacyCenterTitle.translate().text,
      items = items,
      acceptButtonText = uiTexts.savePreferencesButton.translate().text,
      acceptButtonEnabled = areAllRequiredAccepted(preferencesItem.items),
    )
  }

  private fun createPrivacyPreferencesItem(uiTexts: UiTexts): PrivacyFragmentInfoItem {
    val textTranslation = uiTexts.privacyPreferencesTabLabel.translate()

    return PrivacyFragmentInfoItem(
      id = preferencesItemId,
      text = textTranslation.text,
      details = "",
      language = textTranslation.languageCode,
      expanded = preferencesInitiallyExpanded
    )
  }

  private fun areAllRequiredAccepted(items: List<PrivacyPreferencesItem>): Boolean =
    items.firstOrNull { it.required && !it.accepted } == null

  private fun ConsentItem.toPrivacyFragmentInfoItem(): PrivacyFragmentItem {
    val textTranslation = shortText.translate()
    val detailsTranslation = longText.translate()

    return PrivacyFragmentInfoItem(
      id = consentItemId,
      text = textTranslation.text,
      details = detailsTranslation.text,
      language = textTranslation.languageCode,
      expanded = false
    )
  }
}
