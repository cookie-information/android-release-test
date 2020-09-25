package com.clearcode.mobileconsents.sdk

import com.clearcode.mobileconsents.adapter.moshi
import com.clearcode.mobileconsents.domain.Consent
import com.clearcode.mobileconsents.networking.CallListener
import com.clearcode.mobileconsents.networking.ConsentClient
import com.clearcode.mobileconsents.networking.response.ConsentResponseJsonAdapter
import com.clearcode.mobileconsents.networking.response.toDomain
import com.clearcode.mobileconsents.storage.ConsentStorage
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.io.IOException
import java.util.UUID
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

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

      consentSdk.getConsentSuspending(consentId = uuid) shouldBe consent
    }

    it("posts consent and returns result") {
      server.enqueue(MockResponse().setBody(true.toString()))

      consentSdk.postConsentSuspending("consentItem") shouldBe true
    }

    it("on fetching exception returns valid exception") {
      server.enqueue(MockResponse().setResponseCode(404).setBody(notFoundBody))

      shouldThrowExactly<IOException> {
        consentSdk.getConsentSuspending(uuid)
      } shouldHaveMessage """
         |Url: ${baseUrl.newBuilder().addPathSegments("$uuid/consent-data.json").build()}
         |Code: 404
         """.trimMargin()
    }

    it("on parse exception returns valid exception") {
      server.enqueue(MockResponse().setBody(malformedJson))

      shouldThrowExactly<IOException> {
        consentSdk.getConsentSuspending(uuid)
      } shouldHaveMessage "Required value 'consentItems' (JSON name 'universalConsentItems') missing at $"
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

internal fun Class<*>.getResourceAsString(path: String) = getResourceAsStream(path)!!.readBytes().decodeToString()

internal suspend fun MobileConsentSdk.getConsentSuspending(consentId: UUID): Consent =
  suspendCoroutine { continuation ->
    getConsent(
      consentId = consentId,
      listener = object : CallListener<Consent> {
        override fun onSuccess(result: Consent) {
          continuation.resumeWith(Result.success(result))
        }

        override fun onFailure(error: IOException) {
          continuation.resumeWithException(error)
        }
      }
    )
  }

internal suspend fun MobileConsentSdk.postConsentSuspending(consentItem: String): Boolean =
  suspendCoroutine { continuation ->
    postConsentItem(
      consentItem = consentItem,
      listener = object : CallListener<Boolean> {
        override fun onSuccess(result: Boolean) {
          continuation.resumeWith(Result.success(result))
        }

        override fun onFailure(error: IOException) {
          continuation.resumeWithException(error)
        }
      }
    )
  }
