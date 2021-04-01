package com.cookieinformation.mobileconsents.ui

import java.util.UUID

/**
 * The model for [PrivacyPreferencesView]
 */
public sealed class PrivacyCenterItem {

  /**
   * The expandable information item.
   */
  public data class PrivacyCenterInfoItem(
    val id: UUID,
    val text: String,
    val details: String,
    val language: String,
    val expanded: Boolean
  ) : PrivacyCenterItem()

  /**
   * The details item, shown when [PrivacyCenterInfoItem] is expanded.
   */
  public data class PrivacyCenterDetailsItem(
    val id: UUID,
    val details: String,
  ) : PrivacyCenterItem()

  /**
   * The preferences item, where user can choose consents.
   */
  public data class PrivacyCenterPreferencesItem(
    val id: UUID,
    val title: String,
    val subTitle: String,
    val items: List<PrivacyPreferencesItem>,
  ) : PrivacyCenterItem()
}
