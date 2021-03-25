package com.cookieinformation.mobileconsents.ui

import java.util.UUID

public data class PrivacyPreferencesItem(
  val id: UUID,
  val required: Boolean,
  val accepted: Boolean,
  val text: String,
  val details: String,
  val language: String,
)
