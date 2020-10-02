package com.clearcode.mobileconsents.networking.response

import com.clearcode.mobileconsents.ConsentItem
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.UUID

@JsonClass(generateAdapter = true)
internal data class ItemResponse(
  @Json(name = "translations") val translations: List<TranslationResponse>,
  @Json(name = "universalConsentItemId") val consentItemId: UUID
)

internal fun ItemResponse.toDomain() = ConsentItem(
  translations = translations.map(TranslationResponse::toDomain),
  consentItemId = consentItemId
)
