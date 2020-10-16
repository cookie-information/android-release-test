package com.cookieinformation.mobileconsents

import java.util.UUID

/**
 * Solution fetched from CDN server, containing consents presented to the user.
 * @param consentSolutionId UUID of consent solution.
 * @param consentSolutionVersionId UUID of consent solution version.
 * @param consentItems list of consent items, which can be displayed to the user.
 */
public data class ConsentSolution(
  val consentItems: List<ConsentItem>,
  val consentSolutionId: UUID,
  val consentSolutionVersionId: UUID
)
