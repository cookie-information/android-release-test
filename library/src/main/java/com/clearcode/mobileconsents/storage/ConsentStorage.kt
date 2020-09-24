package com.clearcode.mobileconsents.storage

import java.util.UUID

// TODO [CLEAR-11] implement storage (based on DataStore concept)
@Suppress("UnusedPrivateMember")
internal class ConsentStorage {
  fun storeConsentChoice(consentId: UUID, choice: Boolean) = Unit
  fun getUserId() = UUID.randomUUID()
  fun getConsentChoice(consentId: UUID) = Boolean
}
