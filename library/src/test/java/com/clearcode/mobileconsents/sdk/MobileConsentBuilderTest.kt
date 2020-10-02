package com.clearcode.mobileconsents.sdk

import com.clearcode.mobileconsents.MobileConsentSdk
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.throwable.shouldHaveMessage
import okhttp3.OkHttpClient

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
        MobileConsentSdk.Builder().partnerUrl(invalidUrl).build()
      } shouldHaveMessage "$invalidUrl is not a valid url"
    }

    it("throws error context is not provided") {
      shouldThrowExactly<IllegalArgumentException> {
        MobileConsentSdk.Builder().partnerUrl(validUrl).callFactory(OkHttpClient()).build()
      } shouldHaveMessage "Use applicationContext() method to specify your application Context."
    }
  }
})
