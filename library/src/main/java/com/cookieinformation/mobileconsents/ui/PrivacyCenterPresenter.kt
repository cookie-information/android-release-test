package com.cookieinformation.mobileconsents.ui

import com.cookieinformation.mobileconsents.ConsentItem
import com.cookieinformation.mobileconsents.ConsentItem.Type.Info
import com.cookieinformation.mobileconsents.ConsentItem.Type.Setting
import com.cookieinformation.mobileconsents.ConsentSolution
import com.cookieinformation.mobileconsents.UiTexts
import com.cookieinformation.mobileconsents.ui.PrivacyCenterItem.PrivacyCenterDetailsItem
import com.cookieinformation.mobileconsents.ui.PrivacyCenterItem.PrivacyCenterInfoItem
import com.cookieinformation.mobileconsents.ui.PrivacyCenterItem.PrivacyCenterPreferencesItem
import com.cookieinformation.mobileconsents.ui.PrivacyCenterView.IntentListener
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.util.UUID

internal class PrivacyCenterPresenter(
  dispatcher: CoroutineDispatcher = Dispatchers.Main
) : ConsentSolutionPresenter<PrivacyCenterView, PrivacyCenterViewData, IntentListener>(dispatcher), IntentListener {

  private val preferencesItemId = UUID(0, 0)

  private val preferencesInitiallyExpanded = true

  private lateinit var preferencesItem: PrivacyCenterPreferencesItem

  override fun getViewIntentListener(): IntentListener = this

  override fun createViewData(
    consentSolution: ConsentSolution,
    savedConsents: Map<UUID, Boolean>
  ): PrivacyCenterViewData {
    preferencesItem = createPreferencesItem(consentSolution, savedConsents)
    return createPrivacyCenterViewData(consentSolution.consentItems, consentSolution.uiTexts)
  }

  override fun getGivenConsents(viewData: PrivacyCenterViewData): GivenConsent =
    preferencesItem.items.map { it.id to Pair(it.accepted, it.language) }.toMap()

  override fun onPrivacyCenterChoiceChanged(id: UUID, accepted: Boolean) {
    @Suppress("UNCHECKED_CAST")
    val currentViewState = viewState as ViewState.Fetched<PrivacyCenterViewData>
    require(currentViewState.data.items.last() is PrivacyCenterPreferencesItem)

    val preferenceItems = preferencesItem.items.map { if (it.id == id) it.copy(accepted = accepted) else it }
    preferencesItem = preferencesItem.copy(items = preferenceItems)
    val newItems = mutableListOf<PrivacyCenterItem>().apply {
      addAll(currentViewState.data.items)
      removeLast()
      add(preferencesItem)
    }

    viewState = currentViewState.copy(
      data = currentViewState.data.copy(
        items = newItems,
        acceptButtonEnabled = areAllRequiredAccepted(preferenceItems)
      )
    )
  }

  override fun onPrivacyCenterDetailsToggle(id: UUID) {
    @Suppress("UNCHECKED_CAST")
    val currentViewState = viewState as ViewState.Fetched<PrivacyCenterViewData>
    val newItems = mutableListOf<PrivacyCenterItem>().apply {
      addAll(currentViewState.data.items)
    }
    val itemIndex = newItems.indexOfFirst { it is PrivacyCenterInfoItem && it.id == id }
    val item = newItems[itemIndex] as PrivacyCenterInfoItem
    val (newItem, detailsItem) = item.setExpanded(!item.expanded)

    newItems.removeAt(itemIndex)
    newItems.add(itemIndex, newItem)
    if (detailsItem != null) newItems.add(itemIndex + 1, detailsItem) else newItems.removeAt(itemIndex + 1)

    viewState = currentViewState.copy(data = currentViewState.data.copy(items = newItems))
  }

  override fun onPrivacyCenterAcceptClicked() {
    @Suppress("UNCHECKED_CAST")
    viewState as ViewState.Fetched<PrivacyCenterViewData> // Check state
    require(areAllRequiredAccepted(preferencesItem.items))
    send()
  }

  override fun onPrivacyCenterDismissRequest() {
    listener?.onDismissed()
  }

  private fun PrivacyCenterInfoItem.setExpanded(expanded: Boolean): Pair<PrivacyCenterInfoItem, PrivacyCenterItem?> {
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

  private fun PrivacyCenterInfoItem.createDetailsItem() = PrivacyCenterDetailsItem(id = id, details = details)

  private fun createPreferencesItem(
    consentSolution: ConsentSolution,
    savedConsents: Map<UUID, Boolean>
  ): PrivacyCenterPreferencesItem {
    val items = consentSolution.consentItems
      .filter { it.type == Setting }
      .map { it.toPrivacyPreferencesItem(savedConsents) }

    val titleTranslation = consentSolution.uiTexts.consentPreferencesLabel.translate()
    val subTitleTranslation = consentSolution.uiTexts.poweredByLabel.translate()

    return PrivacyCenterPreferencesItem(
      id = preferencesItemId,
      title = titleTranslation.text,
      subTitle = "<a href=\"$cookieInformationUrl\">${subTitleTranslation.text}</a>",
      items = items,
    )
  }

  private fun createPrivacyCenterViewData(consentItems: List<ConsentItem>, uiTexts: UiTexts): PrivacyCenterViewData {
    val items = consentItems
      .filter { it.type == Info }
      .map { it.toPrivacyCenterInfoItem() }
      .toMutableList()

    items.add(createPrivacyPreferencesItem(uiTexts))
    if (preferencesInitiallyExpanded) {
      items.add(preferencesItem)
    }

    return PrivacyCenterViewData(
      title = uiTexts.privacyCenterTitle.translate().text,
      items = items,
      acceptButtonText = uiTexts.savePreferencesButton.translate().text,
      acceptButtonEnabled = areAllRequiredAccepted(preferencesItem.items),
    )
  }

  private fun createPrivacyPreferencesItem(uiTexts: UiTexts): PrivacyCenterInfoItem {
    val textTranslation = uiTexts.privacyPreferencesTabLabel.translate()

    return PrivacyCenterInfoItem(
      id = preferencesItemId,
      text = textTranslation.text,
      details = "",
      language = textTranslation.languageCode,
      expanded = preferencesInitiallyExpanded
    )
  }

  private fun areAllRequiredAccepted(items: List<PrivacyPreferencesItem>): Boolean =
    items.firstOrNull { it.required && !it.accepted } == null

  private fun ConsentItem.toPrivacyCenterInfoItem(): PrivacyCenterItem {
    val textTranslation = shortText.translate()
    val detailsTranslation = longText.translate()

    return PrivacyCenterInfoItem(
      id = consentItemId,
      text = textTranslation.text,
      details = detailsTranslation.text,
      language = textTranslation.languageCode,
      expanded = false
    )
  }
}
