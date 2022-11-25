package com.cookieinformation.mobileconsents.ui

import java.util.UUID

/**
 * The model for [PrivacyFragmentView]
 * The preferences item, where user can choose consents.
 */
public data class PrivacyFragmentPreferencesItem(
  val id: UUID,
  val title: String,
  val subTitle: String,
  val items: List<PrivacyPreferencesItem>,
)