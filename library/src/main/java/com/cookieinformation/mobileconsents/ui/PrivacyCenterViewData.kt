package com.cookieinformation.mobileconsents.ui

/**
 * The model for [PrivacyCenterView]
 */
public data class PrivacyCenterViewData(
  val title: String,
  val acceptButtonText: String,
  val acceptButtonEnabled: Boolean,
  val items: List<PrivacyCenterItem>,
)
