package com.cookieinformation.mobileconsents.networking.response

import com.cookieinformation.mobileconsents.ConsentSolution
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.UUID

@JsonClass(generateAdapter = true)
internal data class ConsentSolutionResponse(
  @Json(name = "universalConsentItems") val consentItems: List<ItemResponse>,
  @Json(name = "universalConsentSolutionId") val consentSolutionId: UUID,
  @Json(name = "universalConsentSolutionVersionId") val consentSolutionVersionId: UUID
)

internal fun ConsentSolutionResponse.toDomain() = ConsentSolution(
  consentItems = consentItems.map(ItemResponse::toDomain),
  consentSolutionId = consentSolutionId,
  consentSolutionVersionId = consentSolutionVersionId
)
