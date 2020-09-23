package com.clearcode.mobileconsents.storage

import java.util.UUID

// TODO implement [CLEAR-11]
@Suppress("UnusedPrivateMember")
internal class ConsentStorage {
  fun storeConsentChoice(consentId: UUID, choice: Boolean) = Unit
  fun getUserId() = UUID.randomUUID()
  fun getConsentChoice(consentId: UUID) = Boolean
}
