package com.cookieinformation.mobileconsents

import java.util.UUID

/**
 * One of [ConsentSolution] items, which user can accept or reject.
 * @param consentItemId [UUID] of consent item.
 * @param translations list of all available translations.
 */
public data class ConsentItem(
  val consentItemId: UUID,
  val translations: List<ConsentTranslation>
)
