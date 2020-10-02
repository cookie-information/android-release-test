package com.clearcode.mobileconsents

import java.util.UUID

public data class ConsentSolution(
  val consentItems: List<ConsentItem>,
  val consentSolutionId: UUID,
  val consentSolutionVersionId: UUID
)
