package com.clearcode.mobileconsents

import com.clearcode.mobileconsents.networking.request.ConsentRequest
import com.clearcode.mobileconsents.networking.request.CustomDataRequest
import java.util.UUID

public data class Consent(
  val consentSolutionId: UUID,
  val consentSolutionVersionId: UUID,
  val processingPurposes: List<ProcessingPurpose>,
  val customData: Map<String, String>,
)

internal fun Consent.toRequest(userId: UUID, timestamp: String) = ConsentRequest(
  userId,
  consentSolutionId,
  consentSolutionVersionId,
  timestamp,
  processingPurposes.map(ProcessingPurpose::toRequest),
  customData.map { CustomDataRequest(it.key, it.value) }
)
