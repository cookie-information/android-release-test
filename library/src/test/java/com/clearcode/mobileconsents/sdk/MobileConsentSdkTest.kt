package com.clearcode.mobileconsents.sdk

import com.clearcode.mobileconsents.ApplicationProperties
import com.clearcode.mobileconsents.Consent
import com.clearcode.mobileconsents.ConsentSolution
import com.clearcode.mobileconsents.MobileConsentSdk
import com.clearcode.mobileconsents.adapter.moshi
import com.clearcode.mobileconsents.networking.CallListener
import com.clearcode.mobileconsents.networking.ConsentClient
import com.clearcode.mobileconsents.networking.response.ConsentSolutionResponseJsonAdapter
import com.clearcode.mobileconsents.networking.response.toDomain
import com.clearcode.mobileconsents.storage.ConsentStorage
import com.clearcode.mobileconsents.storage.MoshiFileHandler
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.io.IOException
import java.util.UUID
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal class MobileConsentSdkTest : DescribeSpec({

  val uuid = UUID.fromString("843ddd4a-3eae-4286-a17b-0e8d3337e767")
  val consentString = javaClass.getResourceAsString("/consent_solution.json")
  val applicationProperties = ApplicationProperties(
    osVersion = "4.4.4",
    packageName = "com.example",
    appName = "Sample"
  )
  val consent = Consent(
    consentSolutionId = uuid,
    consentSolutionVersionId = uuid,
    processingPurposes = emptyList(),
    customData = emptyMap()
  )
  lateinit var server: MockWebServer
  lateinit var baseUrl: HttpUrl
  lateinit var consentClient: ConsentClient
  lateinit var storage: ConsentStorage
  lateinit var consentSdk: MobileConsentSdk
  lateinit var consentSolution: ConsentSolution

  beforeTest {
    server = MockWebServer()
    server.start()

    baseUrl = server.url("/api/test")
    consentClient = ConsentClient(baseUrl, baseUrl, OkHttpClient(), moshi)
    storage = ConsentStorage(Mutex(), tempfile(suffix = ".txt"), MoshiFileHandler(moshi))
    consentSolution = ConsentSolutionResponseJsonAdapter(moshi).fromJson(consentString)!!.toDomain()
    consentSdk = MobileConsentSdk(consentClient, storage, applicationProperties, Dispatchers.Unconfined)
  }

  afterTest {
    server.shutdown()
  }

  describe("MobileConsentsSdk") {
    it("gets consent and parse to domain object") {
      server.enqueue(MockResponse().setBody(consentString))

      consentSdk.getConsentSuspending(consentId = uuid) shouldBe consentSolution
    }

    it("posts consent and returns result") {
      server.enqueue(MockResponse().setBody(true.toString()))

      consentSdk.postConsentSuspending(consent) shouldBe Unit
    }

    it("on fetching exception returns valid exception") {
      server.enqueue(MockResponse().setResponseCode(404).setBody(notFoundBody))

      shouldThrowExactly<IOException> {
        consentSdk.getConsentSuspending(uuid)
      } shouldHaveMessage """
         |Url: ${baseUrl.newBuilder().addPathSegments("$uuid/consent-data.json").build()}
         |Code: 404
         |Message: $notFoundBody
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

internal suspend fun MobileConsentSdk.getConsentSuspending(consentId: UUID): ConsentSolution =
  suspendCoroutine { continuation ->
    getConsent(
      consentId = consentId,
      listener = object : CallListener<ConsentSolution> {
        override fun onSuccess(result: ConsentSolution) {
          continuation.resumeWith(Result.success(result))
        }

        override fun onFailure(error: IOException) {
          continuation.resumeWithException(error)
        }
      }
    )
  }

internal suspend fun MobileConsentSdk.postConsentSuspending(consent: Consent) =
  suspendCoroutine<Unit> { continuation ->
    postConsentItem(
      consent = consent,
      listener = object : CallListener<Unit> {
        override fun onSuccess(result: Unit) {
          continuation.resumeWith(Result.success(result))
        }

        override fun onFailure(error: IOException) {
          continuation.resumeWithException(error)
        }
      }
    )
  }
