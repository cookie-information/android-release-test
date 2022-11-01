package com.cookieinformation.mobileconsents.networking.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class TokenRequest (
  @Json(name = "client_id") val clientId: String,
  @Json(name = "client_secret") val clientSecret: String,
  @Json(name = "grant_type") val grantType: String
)