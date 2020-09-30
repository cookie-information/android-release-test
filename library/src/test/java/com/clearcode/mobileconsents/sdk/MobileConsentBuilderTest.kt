package com.clearcode.mobileconsents.sdk

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.throwable.shouldHaveMessage

internal class MobileConsentBuilderTest : DescribeSpec({

  val invalidUrl = "abcd"
  val validUrl = "https://test.api"

  describe("MobileConsentBuilder") {
    it("throws error when post url is not defined") {
      shouldThrowExactly<IllegalArgumentException> {
        MobileConsentSdk.Builder().build()
      } shouldHaveMessage "Use postUrl() method to specify url for posting consents."
    }

    it("throws error when post url is not valid") {
      shouldThrowExactly<IllegalArgumentException> {
        MobileConsentSdk.Builder().postUrl(invalidUrl).build()
      } shouldHaveMessage "$invalidUrl is not a valid url"
    }

    it("throws error when internal file is not defined") {
      shouldThrowExactly<IllegalArgumentException> {
        MobileConsentSdk.Builder().postUrl(validUrl).build()
      } shouldHaveMessage "Use androidContext() method to specify Context."
    }
  }
})
