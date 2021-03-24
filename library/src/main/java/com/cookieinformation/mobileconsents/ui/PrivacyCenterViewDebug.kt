package com.cookieinformation.mobileconsents.ui

import com.cookieinformation.mobileconsents.ui.PrivacyCenterItem.PrivacyCenterDetailsItem
import com.cookieinformation.mobileconsents.ui.PrivacyCenterItem.PrivacyCenterInfoItem
import com.cookieinformation.mobileconsents.ui.PrivacyCenterItem.PrivacyCenterPreferencesItem
import java.util.UUID

// TODO remove this file when presenter is ready

internal val privacyCenterViewData = PrivacyCenterViewData(
  title = "Privacy",

  items = listOf(
    PrivacyCenterInfoItem(
      id = UUID.fromString("7d477dbf-5f88-420f-8dfc-2506907ebe07"),
      text = "Processing purposes", // Because of reasons
      language = "EN",
      expanded = false
    ),
    PrivacyCenterInfoItem(
      id = UUID.fromString("1d5920c7-c5d1-4c08-93cc-4238457d7a1f"),
      text = "What we collect", // The data collected includes:<br><ul><li>Your shoe size</li></ul>"
      language = "EN",
      expanded = false
    ),
    PrivacyCenterInfoItem(
      id = UUID.fromString("99f6f633-7193-4d69-bf8a-759e7cee349a"),
      text = "Your rights",
      language = "EN",
      expanded = false
    ),
    PrivacyCenterInfoItem(
      id = UUID(0, 0),
      text = "privacyPreferencesTabLabel",
      language = "EN",
      expanded = false
    ),
  ),
  acceptButtonText = "Accept",
  acceptButtonEnabled = true,
)

internal val privacyPreferencesItems = listOf(
  PrivacyPreferencesItem(
    id = UUID.randomUUID(),
    required = true,
    accepted = true,
    text = "I agree to the Terms of Services.",
    details = "",
    language = "EN",
  ),
  PrivacyPreferencesItem(
    id = UUID.randomUUID(),
    required = false,
    accepted = false,
    text = "I consent to the use of my personal data. <a href=\"https://cookieinformation.com\">Example link</a>",
    details = "",
    language = "EN",
  ),
  PrivacyPreferencesItem(
    id = UUID.randomUUID(),
    required = false,
    accepted = false,
    text = "Personalised Experience",
    details = "I consent to the use of my personal data. We do this to create statistics, personalise the experience",
    language = "EN",
  ),
  PrivacyPreferencesItem(
    id = UUID.randomUUID(),
    required = false,
    accepted = false,
    text = "Personalised Experience",
    details = "",
    language = "EN",
  ),
)

internal object PrivacyCenterViewIntentListener : PrivacyCenterView.IntentListener {
  var ViewData = privacyCenterViewData

  override fun onPrivacyCenterChoiceChanged(id: UUID, accepted: Boolean) = Unit

  override fun onPrivacyCenterAcceptClicked() = Unit

  override fun onPrivacyCenterDismissRequest() = Unit

  override fun onPrivacyCenterDetailsToggle(id: UUID) {
    val newItems = mutableListOf<PrivacyCenterItem>().apply {
      addAll(ViewData.items)
    }
    val itemIndex = newItems.indexOfFirst { it is PrivacyCenterInfoItem && it.id == id }
    val item = newItems[itemIndex] as PrivacyCenterInfoItem
    val (newItem, detailsItem) = item.setExpanded(!item.expanded)
    newItems.removeAt(itemIndex)
    newItems.add(itemIndex, newItem)
    if (detailsItem != null) newItems.add(itemIndex + 1, detailsItem) else newItems.removeAt(itemIndex + 1)
    ViewData = ViewData.copy(items = newItems)
  }

  private fun PrivacyCenterInfoItem.setExpanded(expanded: Boolean): Pair<PrivacyCenterInfoItem, PrivacyCenterItem?> {
    val infoItem = copy(expanded = expanded)
    val detailsItem = if (expanded) {
      if (id == UUID(0, 0)) {
        createPreferencesItem(id)
      } else {
        createDetailsItem(id)
      }
    } else null
    return infoItem to detailsItem
  }

  private fun createDetailsItem(id: UUID) = PrivacyCenterDetailsItem(
    id = id,
    details = "Lorem ipsum dolor sit amet, ...<br> The data collected includes:<br><ul><li>Your shoe size</li></ul>",
    language = "EN"
  )

  private fun createPreferencesItem(id: UUID) = PrivacyCenterPreferencesItem(
    id = id,
    title = "consentPreferencesLabel",
    subTitle = "<a href=\"https://cookieinformation.com\">Powered by Cookie Information</a>",
    items = privacyPreferencesItems,
  )
}
