package com.cookieinformation.mobileconsents.ui

import java.util.UUID

/**
 * The model for [PrivacyPreferencesView]
 */
public sealed class PrivacyFragmentItem {

  /**
   * The expandable information item.
   */
  public data class PrivacyFragmentInfoItem(
    val id: UUID,
    val text: String,
    val details: String,
    val language: String,
    val expanded: Boolean
  ) : PrivacyFragmentItem()

  /**
   * The details item, shown when [PrivacyCenterInfoItem] is expanded.
   */
  public data class PrivacyFragmentDetailsItem(
    val id: UUID,
    val details: String,
  ) : PrivacyFragmentItem()

  /**
   * The preferences item, where user can choose consents.
   */
  public data class PrivacyFragmentPreferencesItem(
    val id: UUID,
    val title: String,
    val subTitle: String,
    val items: List<PrivacyPreferencesItem>,
  ) : PrivacyFragmentItem()
}
