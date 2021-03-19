package com.cookieinformation.mobileconsents

import com.cookieinformation.mobileconsents.ConsentItem.Type
import java.util.UUID

/**
 * One of [ConsentSolution] items, which user can accept or reject.
 *
 * All items with type [ConsentItem.Type.Setting] are required and should be accepted by default.
 *
 * @param consentItemId [UUID] of consent item.
 * @param shortText list of all available translations for brief description of the consent.
 * @param longText list of all available translations for full description of the consent.
 * @param required true if uses must accept the consent, otherwise false.
 * @param type [Type] of the consent.
 */
public data class ConsentItem(
  val consentItemId: UUID,
  val shortText: List<TextTranslation>,
  val longText: List<TextTranslation>,
  val required: Boolean,
  val type: Type,
) {
  public enum class Type {
    Setting,
    Info,
  }
}
