package com.cookieinformation.mobileconsents.ui

import java.util.UUID

internal sealed class PrivacyCenterItem {

  data class PrivacyCenterInfoItem(
    val id: UUID,
    val text: String,
    val language: String,
    val expanded: Boolean
  ) : PrivacyCenterItem()

  data class PrivacyCenterDetailsItem(
    val id: UUID,
    val details: String,
    val language: String,
  ) : PrivacyCenterItem()

  data class PrivacyCenterPreferencesItem(
    val id: UUID,
    val title: String,
    val subTitle: String,
    val items: List<PrivacyPreferencesItem>,
  ) : PrivacyCenterItem()
}
