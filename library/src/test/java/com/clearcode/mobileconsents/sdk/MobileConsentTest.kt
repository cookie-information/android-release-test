package com.clearcode.mobileconsents.sdk

import com.clearcode.mobileconsents.adapter.moshi
import com.clearcode.mobileconsents.domain.Consent
import com.clearcode.mobileconsents.networking.CallListener
import com.clearcode.mobileconsents.networking.ConsentClient
import com.clearcode.mobileconsents.networking.responses.ConsentResponseJsonAdapter
import com.clearcode.mobileconsents.networking.responses.toDomain
import com.clearcode.mobileconsents.storage.ConsentStorage
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.util.UUID
import java.util.concurrent.CountDownLatch

internal class MobileConsentTest : DescribeSpec({

  val uuid = UUID.fromString("843ddd4a-3eae-4286-a17b-0e8d3337e767")
  val consentString = javaClass.getResourceAsString("/consent.json")
  lateinit var server: MockWebServer
  lateinit var baseUrl: HttpUrl
  lateinit var consentClient: ConsentClient
  lateinit var consentSdk: MobileConsentSdk
  lateinit var consent: Consent

  beforeTest {
    server = MockWebServer()
    server.start()

    baseUrl = server.url("/api/test")
    consentClient = ConsentClient(baseUrl, baseUrl)
    consent = ConsentResponseJsonAdapter(moshi).fromJson(consentString)!!.toDomain()
    consentSdk = MobileConsentSdk(consentClient, ConsentStorage(), moshi)
  }

  afterTest {
    server.shutdown()
  }

  describe("MobileConsentsSdk") {
    it("gets consent and parse to domain object") {
      server.enqueue(MockResponse().setBody(consentString))
      val countdownLatch = CountDownLatch(1)
      var consentResult: Consent? = null

      consentSdk.getConsent(
        consentId = uuid,
        listener = onSuccess {
          consentResult = it
          countdownLatch.countDown()
        }
      )

      countdownLatch.await()
      consentResult shouldBe consent
    }
    it("posts consent and returns result") {
      server.enqueue(MockResponse().setBody(true.toString()))
      val countdownLatch = CountDownLatch(1)
      var result = false

      consentSdk.postConsentItem(
        consentItem = "consent",
        listener = onSuccess {
          result = it
          countdownLatch.countDown()
        }
      )

      countdownLatch.await()
      result shouldBe true
    }
  }
})

internal inline fun <T> onSuccess(crossinline onSuccess: (T) -> Unit) = object : CallListener<T> {
  override fun onSuccess(result: T) {
    onSuccess(result)
  }

  override fun onFailure(error: Throwable) = throw error
}

internal fun Class<*>.getResourceAsString(path: String) = getResourceAsStream(path)!!.readBytes().decodeToString()
