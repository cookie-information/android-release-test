package com.cookieinformation.mobileconsents.ui

internal data class PrivacyCenterViewData(
  val title: String,
  val acceptButtonText: String,
  val acceptButtonEnabled: Boolean,
  val items: List<PrivacyCenterItem>,
)
