package com.cookieinformation.mobileconsents

import java.util.UUID

/**
 * One of [ConsentSolution] items, which user can accept or reject.
 *
 * All items with type [ConsentItem.Type.Setting] are required and should be accepted by default.
 *
 * @param consentItemId [UUID] of consent item.
 * @param translations list of all available translations.
 * @param required true if uses must accept the consent, otherwise false.
 * @param required true if uses must accept the consent, otherwise false.
 */
public data class ConsentItem(
  val consentItemId: UUID,
  val translations: List<ConsentTranslation>,
  val required: Boolean,
  val type: Type,
) {
  public enum class Type {
    Setting,
    Info,
  }
}
