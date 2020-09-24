package com.clearcode.mobileconsents.sdk

import com.clearcode.mobileconsents.adapter.moshi
import com.clearcode.mobileconsents.domain.Consent
import com.clearcode.mobileconsents.networking.CallListener
import com.clearcode.mobileconsents.networking.ConsentClient
import com.clearcode.mobileconsents.networking.response.ConsentResponseJsonAdapter
import com.clearcode.mobileconsents.networking.response.toDomain
import com.clearcode.mobileconsents.storage.ConsentStorage
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.io.IOException
import java.util.UUID
import java.util.concurrent.CountDownLatch

// TODO add coroutine helper for CountDownLatch
internal class MobileConsentSdkTest : DescribeSpec({

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
      val countDownLatch = CountDownLatch(1)
      var consentResult: Consent? = null

      consentSdk.getConsent(
        consentId = uuid,
        listener = onSuccess {
          consentResult = it
          countDownLatch.countDown()
        }
      )

      countDownLatch.await()
      consentResult shouldBe consent
    }
    it("posts consent and returns result") {
      server.enqueue(MockResponse().setBody(true.toString()))
      val countDownLatch = CountDownLatch(1)
      var result = false

      consentSdk.postConsentItem(
        consentItem = "consent",
        listener = onSuccess {
          result = it
          countDownLatch.countDown()
        }
      )

      countDownLatch.await()
      result shouldBe true
    }
    it("on fetching exception returns valid exception") {
      server.enqueue(MockResponse().setResponseCode(404).setBody(notFoundBody))
      val countDownLatch = CountDownLatch(1)
      var result: Throwable? = null

      consentSdk.getConsent(
        consentId = uuid,
        listener = onFailure {
          result = it
          countDownLatch.countDown()
        }
      )

      countDownLatch.await()
      result shouldBe IOException(
        """
        |Url: ${baseUrl.newBuilder().addPathSegments("$uuid/consent-data.json").build()}
        |Code: 404
        """.trimMargin()
      )
    }
    it("on parse exception returns valid exception") {
      server.enqueue(MockResponse().setBody(malformedJson))
      val countdownLatch = CountDownLatch(1)
      var result: Throwable? = null

      consentSdk.getConsent(
        consentId = uuid,
        listener = onFailure {
          result = it
          countdownLatch.countDown()
        }
      )

      countdownLatch.await()
      result shouldBe IOException("Required value 'consentItems' (JSON name 'universalConsentItems') missing at $")
    }
  }
})

private val notFoundMessage =
  """
  The specified blob does not exist.
  RequestId:02ada520-301e-009f-6e49-92744f000000
  Time:2020-09-24T08:07:34.2253137Z
  """.trimIndent()

private val notFoundBody =
  """
  something
  <Message>$notFoundMessage</Message>
  something
  """.trimIndent()

private const val malformedJson =
  """{"malformed": "json"}"""

internal inline fun <T> onSuccess(crossinline onSuccess: (T) -> Unit) = object : CallListener<T> {
  override fun onSuccess(result: T) = onSuccess(result)

  override fun onFailure(error: IOException) = throw error
}

internal inline fun <T> onFailure(crossinline onFailure: (Throwable) -> Unit) = object : CallListener<T> {
  override fun onSuccess(result: T) = throw error("error")

  override fun onFailure(error: IOException) = onFailure(error)
}

internal fun Class<*>.getResourceAsString(path: String) = getResourceAsStream(path)!!.readBytes().decodeToString()
