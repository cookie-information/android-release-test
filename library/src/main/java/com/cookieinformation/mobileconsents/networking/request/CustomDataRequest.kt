package com.cookieinformation.mobileconsents.networking.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class CustomDataRequest(
  @Json(name = "fieldName") val fieldName: String,
  @Json(name = "fieldValue") val fieldValue: String
)
