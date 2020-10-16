package com.cookieinformation.mobileconsents.networking.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.UUID

@JsonClass(generateAdapter = true)
internal data class ProcessingPurposeRequest(
  @Json(name = "consentGiven") val consentGiven: Boolean,
  @Json(name = "language") val language: String,
  @Json(name = "universalConsentItemId") val consentItemId: UUID
)
