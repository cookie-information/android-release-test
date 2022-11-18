package com.cookieinformation.mobileconsents.ui

/**
 * The model for [PrivacyFragmentView]
 */
public data class PrivacyFragmentViewData(
  val privacyTitleText: String,
  val privacyDescriptionShortText: String,
  val privacyDescriptionLongText: String,
  val privacyReadMoreText: String,
  val acceptSelectedButtonText: String,
  val acceptSelectedButtonEnabled: Boolean,
  val acceptAllButtonText: String,
  val poweredByLabelText: String,
  val items: List<PrivacyFragmentItem>,
)
