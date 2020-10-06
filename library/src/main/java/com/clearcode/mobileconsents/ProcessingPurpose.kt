package com.clearcode.mobileconsents

import com.clearcode.mobileconsents.networking.request.ProcessingPurposeRequest
import java.util.UUID

/**
 * Contains user choice for specified [ConsentItem] as well as language in which consent has been given.
 * @param consentItemId [UUID] of [ConsentItem].
 * @param consentGiven user consent choice.
 * @param language language in which consent has been given / revoked.
 */
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
