package com.cookieinformation.mobileconsents.networking.response

import com.cookieinformation.mobileconsents.ConsentItem
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.UUID

private const val typeSetting = "setting"
private const val typeInfo = "info"

@JsonClass(generateAdapter = true)
internal data class ItemResponse(
  @Json(name = "translations") val translations: List<ConsentTranslationResponse>,
  @Json(name = "universalConsentItemId") val consentItemId: UUID,
  @Json(name = "required") val required: Boolean,
  @Json(name = "type") val type: String
)

internal fun ItemResponse.toDomain() = ConsentItem(
  translations = translations.map(ConsentTranslationResponse::toDomain),
  consentItemId = consentItemId,
  required = required,
  type = type.toDomainItemType()
)

private fun String.toDomainItemType(): ConsentItem.Type =
  when (this) {
    typeSetting -> ConsentItem.Type.Setting
    typeInfo -> ConsentItem.Type.Info
    else -> ConsentItem.Type.Setting // TODO Ensure if default value should be "SETTING"
  }
