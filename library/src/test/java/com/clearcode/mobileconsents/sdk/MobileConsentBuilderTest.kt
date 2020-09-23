package com.clearcode.mobileconsents.sdk

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.throwable.shouldHaveMessage
import java.lang.IllegalStateException

internal class MobileConsentBuilderTest : DescribeSpec({

  val invalidUrl = "abcd"
  val validUrl = "https://test.api"

  describe("MobileConsentBuilder") {
    it("throws error when post url is not defined") {
      shouldThrowExactly<IllegalStateException> {
        MobileConsentSdk.Builder().build()
      } shouldHaveMessage "Use postUrl() method to specify url for posting consents"
    }

    it("throws error when post url is not valid") {
      shouldThrowExactly<IllegalArgumentException> {
        MobileConsentSdk.Builder().postUrl(invalidUrl).build()
      } shouldHaveMessage "$invalidUrl is not a valid url"
    }

    it("returns sdk object when all parameters are valid") {
      shouldNotThrowAny {
        MobileConsentSdk.Builder().postUrl(validUrl).build()
      }
    }
  }
})
