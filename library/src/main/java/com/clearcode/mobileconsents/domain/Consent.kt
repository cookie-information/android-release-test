package com.clearcode.mobileconsents.domain

import java.util.UUID

public data class Consent(
  val consentItems: List<ConsentItem>,
  val consentSolutionId: UUID,
  val consentSolutionVersionId: UUID
)
