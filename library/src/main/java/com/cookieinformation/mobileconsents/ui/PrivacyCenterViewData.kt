package com.cookieinformation.mobileconsents.ui

public data class PrivacyCenterViewData(
  val title: String,
  val acceptButtonText: String,
  val acceptButtonEnabled: Boolean,
  val items: List<PrivacyCenterItem>,
)
