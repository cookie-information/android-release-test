package com.cookieinformation.mobileconsents.networking.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class TokenResponse(
  @Json(name = "tokenType") val tokenType: String,
  @Json(name = "accessToken") val accessToken: String,
  @Json(name = "expiresIn") val expiresIn: String
)
