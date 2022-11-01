package com.cookieinformation.mobileconsents.networking.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class TokenResponse(
  @Json(name = "token_type") val tokenType: String,
  @Json(name = "access_token") val accessToken: String,
  @Json(name = "expires_in") val expiresIn: Int
)
