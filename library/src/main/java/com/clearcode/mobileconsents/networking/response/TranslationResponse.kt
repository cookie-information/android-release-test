package com.clearcode.mobileconsents.networking.response

import com.clearcode.mobileconsents.domain.Translation
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class TranslationResponse(
  @Json(name = "language") val language: String,
  @Json(name = "longText") val longText: String,
  @Json(name = "shortText") val shortText: String
)

internal fun TranslationResponse.toDomain() = Translation(
  language = language,
  longText = longText,
  shortText = shortText
)
