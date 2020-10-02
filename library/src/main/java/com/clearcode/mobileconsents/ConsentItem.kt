package com.clearcode.mobileconsents

import java.util.UUID

public data class ConsentItem(
  val translations: List<Translation>,
  val consentItemId: UUID
)
