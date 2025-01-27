package com.cookieinformation.mobileconsents.ui

import com.cookieinformation.mobileconsents.ConsentItem.Type
import java.util.UUID

/**
 * The model for item of [PrivacyPreferencesListAdapter]
 */
public data class PrivacyPreferencesItem(
  val id: UUID,
  val required: Boolean,
  val accepted: Boolean,
  val text: String,
  val details: String,
  val language: String,
  val type: Type
)
