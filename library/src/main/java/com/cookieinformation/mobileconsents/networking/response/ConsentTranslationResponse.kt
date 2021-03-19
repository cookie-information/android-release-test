package com.cookieinformation.mobileconsents.networking.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class ConsentTranslationResponse(
  @Json(name = "language") val language: String,
  @Json(name = "longText") val longText: String,
  @Json(name = "shortText") val shortText: String
)
