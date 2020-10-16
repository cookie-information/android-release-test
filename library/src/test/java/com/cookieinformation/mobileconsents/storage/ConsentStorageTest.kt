package com.cookieinformation.mobileconsents.storage

import com.cookieinformation.mobileconsents.ProcessingPurpose
import com.cookieinformation.mobileconsents.adapter.moshi
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import java.io.File
import java.util.UUID

private val firstConsentId = UUID.fromString("6cf68ae8-f6ee-48f3-9876-18cb0f6a18b5")
private val secondConsentId = UUID.fromString("6cf69ae8-f6ee-48f3-9876-18cb0f6a18b5")

internal class ConsentStorageTest : DescribeSpec({

  val firstProcessingPurpose = ProcessingPurpose(
    consentItemId = firstConsentId,
    consentGiven = true,
    language = "EN"
  )
  val secondProcessingPurpose = ProcessingPurpose(
    consentItemId = secondConsentId,
    consentGiven = false,
    language = "EN"
  )
  val file = tempfile(suffix = ".txt")
  val consentStorage = ConsentStorage(
    mutex = Mutex(),
    file = file,
    fileHandler = MoshiFileHandler(moshi),
    dispatcher = Dispatchers.Unconfined
  )

  afterTest {
    file.flush()
  }

  describe("Consent Storage") {
    it("stores all consent choices") {
      consentStorage.storeConsentChoices(listOf(firstProcessingPurpose, secondProcessingPurpose))

      val choices = consentStorage.getAllConsentChoices()

      choices[firstConsentId] shouldBe true
      choices[secondConsentId] shouldBe false
      choices.size shouldBeExactly 2
    }

    it("returns consent choice") {
      consentStorage.storeConsentChoices(listOf(firstProcessingPurpose))

      val choice = consentStorage.getConsentChoice(firstConsentId)

      choice shouldBe true
    }

    it("overwrites consent choice") {
      consentStorage.storeConsentChoices(listOf(firstProcessingPurpose))
      consentStorage.storeConsentChoices(listOf(firstProcessingPurpose.copy(consentGiven = false)))

      val choice = consentStorage.getConsentChoice(firstConsentId)

      choice shouldBe false
    }

    it("filters out user id when getting all consent choices") {
      consentStorage.storeConsentChoices(listOf(firstProcessingPurpose))
      consentStorage.getUserId()

      val choices = consentStorage.getAllConsentChoices()

      choices["user_id_key"] shouldBe null
    }

    it("returns empty map when no choices have been stored") {
      val choices = consentStorage.getAllConsentChoices()

      choices shouldBe emptyMap()
    }

    it("on first usage generates user id and then doesn't overwrite it") {
      val userId = consentStorage.getUserId()

      val secondUserId = consentStorage.getUserId()

      userId shouldBe secondUserId
    }
  }
})

private fun File.flush() {
  this.printWriter().close()
}
