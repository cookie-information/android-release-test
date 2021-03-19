package com.cookieinformation.mobileconsents.ui

import java.util.UUID

internal data class PrivacyPreferencesItem(
  val id: UUID,
  val required: Boolean,
  val accepted: Boolean,
  val text: String,
)
