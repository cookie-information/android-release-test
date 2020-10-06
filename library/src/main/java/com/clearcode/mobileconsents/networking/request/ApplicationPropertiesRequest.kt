package com.clearcode.mobileconsents.networking.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class ApplicationPropertiesRequest(
  @Json(name = "operatingSystem") val operatingSystem: String,
  @Json(name = "applicationId") val applicationId: String,
  @Json(name = "applicationName") val applicationName: String
)
