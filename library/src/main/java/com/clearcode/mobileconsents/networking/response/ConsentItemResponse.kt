package com.clearcode.mobileconsents.networking.response

import com.clearcode.mobileconsents.domain.ConsentItem
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.UUID

@JsonClass(generateAdapter = true)
internal data class ConsentItemResponse(
  @Json(name = "translations") val translations: List<TranslationResponse>,
  @Json(name = "universalConsentItemId") val consentItemId: UUID
)

internal fun ConsentItemResponse.toDomain() = ConsentItem(
  translations = translations.map(TranslationResponse::toDomain),
  consentItemId = consentItemId
)
