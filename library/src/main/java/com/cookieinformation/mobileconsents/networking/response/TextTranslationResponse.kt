package com.cookieinformation.mobileconsents.networking.response

import com.cookieinformation.mobileconsents.TextTranslation
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class TextTranslationResponse(
  @Json(name = "language") val language: String,
  @Json(name = "text") val text: String
)

internal fun TextTranslationResponse.toDomain() = TextTranslation(
  languageCode = language,
  text = text
)
