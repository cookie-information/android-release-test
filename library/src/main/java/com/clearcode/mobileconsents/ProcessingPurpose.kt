package com.clearcode.mobileconsents

import com.clearcode.mobileconsents.networking.request.ProcessingPurposeRequest
import java.util.UUID

public data class ProcessingPurpose(
  val consentItemId: UUID,
  val consentGiven: Boolean,
  val language: String
)

internal fun ProcessingPurpose.toRequest() = ProcessingPurposeRequest(
  consentItemId = consentItemId,
  consentGiven = consentGiven,
  language = language
)
