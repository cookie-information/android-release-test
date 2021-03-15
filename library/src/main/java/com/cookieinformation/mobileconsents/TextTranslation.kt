package com.cookieinformation.mobileconsents

/**
 * Translation of [ConsentItem], with short and long text of consent (one of them cannot be empty).
 * @param languageCode code of transaction's language, e.g. "EN",
 * @param text text in a given language.
 */
public data class TextTranslation(
  val languageCode: String,
  val text: String
)
