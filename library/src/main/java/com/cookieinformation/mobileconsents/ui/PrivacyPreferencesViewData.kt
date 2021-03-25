package com.cookieinformation.mobileconsents.ui

public data class PrivacyPreferencesViewData(
  val title: String,
  val subTitle: String,
  val description: String,
  val items: List<PrivacyPreferencesItem>,

  val buttonReadMore: ButtonState,
  val buttonAcceptAll: ButtonState,
  val buttonRejectAll: ButtonState,
  val buttonAcceptSelected: ButtonState,
) {

  public data class ButtonState(val text: String, val enabled: Boolean)
}
