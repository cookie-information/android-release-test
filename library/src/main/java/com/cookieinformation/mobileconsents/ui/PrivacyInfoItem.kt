package com.cookieinformation.mobileconsents.ui

import com.cookieinformation.mobileconsents.ConsentItem.Type

/**
 * The model for info item of [PrivacyFragmentView]
 */
public data class PrivacyInfoItem(
  val text: String,
  val details: String,
  val language: String,
  val type: Type
)
