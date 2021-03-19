package com.cookieinformation.mobileconsents.ui

internal data class PrivacyPreferencesViewData(
  val title: String,
  val subTitle: String,
  val description: String,
  val items: List<PrivacyPreferencesItem>,

  val buttonReadMore: ButtonState,
  val buttonAcceptAll: ButtonState,
  val buttonRejectAll: ButtonState,
  val buttonAcceptSelected: ButtonState,
) {

  data class ButtonState(val text: CharSequence, val enabled: Boolean)
}
