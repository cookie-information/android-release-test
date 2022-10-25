package com.cookieinformation.mobileconsents.networking.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class TokenRequest (
  @Json(name = "clientId") val clientId: String,
  @Json(name = "clientSecret") val clientSecret: String,
  @Json(name = "grantType") val grantType: String
)