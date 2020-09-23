package com.clearcode.mobileconsents.networking.responses

import com.clearcode.mobileconsents.domain.Consent
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.UUID

@JsonClass(generateAdapter = true)
internal data class ConsentResponse(
  @Json(name = "universalConsentItems") val consentItems: List<ConsentItemResponse>,
  @Json(name = "universalConsentSolutionId") val consentSolutionId: UUID,
  @Json(name = "universalConsentSolutionVersionId") val consentSolutionVersionId: UUID
)

internal fun ConsentResponse.toDomain() = Consent(
  consentItems = consentItems.map(ConsentItemResponse::toDomain),
  consentSolutionId = consentSolutionId,
  consentSolutionVersionId = consentSolutionVersionId
)
