package com.cookieinformation.mobileconsents.ui

import java.util.UUID

public sealed class PrivacyCenterItem {

  public data class PrivacyCenterInfoItem(
    val id: UUID,
    val text: String,
    val details: String,
    val language: String,
    val expanded: Boolean
  ) : PrivacyCenterItem()

  public data class PrivacyCenterDetailsItem(
    val id: UUID,
    val details: String,
  ) : PrivacyCenterItem()

  public data class PrivacyCenterPreferencesItem(
    val id: UUID,
    val title: String,
    val subTitle: String,
    val items: List<PrivacyPreferencesItem>,
  ) : PrivacyCenterItem()
}
