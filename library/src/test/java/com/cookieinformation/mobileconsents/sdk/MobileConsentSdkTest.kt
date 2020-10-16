package com.cookieinformation.mobileconsents.sdk

import com.cookieinformation.mobileconsents.CallListener
import com.cookieinformation.mobileconsents.Consent
import com.cookieinformation.mobileconsents.ConsentSolution
import com.cookieinformation.mobileconsents.MobileConsentSdk
import com.cookieinformation.mobileconsents.ProcessingPurpose
import com.cookieinformation.mobileconsents.adapter.moshi
import com.cookieinformation.mobileconsents.networking.ConsentClient
import com.cookieinformation.mobileconsents.networking.response.ConsentSolutionResponseJsonAdapter
import com.cookieinformation.mobileconsents.networking.response.toDomain
import com.cookieinformation.mobileconsents.storage.ConsentStorage
import com.cookieinformation.mobileconsents.storage.MoshiFileHandler
import com.cookieinformation.mobileconsents.system.ApplicationProperties
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
  val secondUuid = UUID.fromString("844ddd4a-3eae-4286-a17b-0e8d3337e767")
  val consentString = javaClass.getResourceAsString("/consent_solution.json")
  val applicationProperties = ApplicationProperties(
    operatingSystem = "4.4.4",
    applicationId = "com.example",
    applicationName = "Sample"
  )
  val consent = Consent(
    consentSolutionId = uuid,
    consentSolutionVersionId = uuid,
    processingPurposes = listOf(
      ProcessingPurpose(consentItemId = uuid, consentGiven = true, language = "en"),
      ProcessingPurpose(consentItemId = secondUuid, consentGiven = false, language = "en")
    ),
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
    consentSdk = MobileConsentSdk(
      consentClient = consentClient,
      consentStorage = storage,
      applicationProperties = applicationProperties,
      dispatcher = Dispatchers.Unconfined
    )
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
      server.enqueue(MockResponse())

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

    it("returns all stored consent choices") {
      server.enqueue(MockResponse())

      consentSdk.postConsentSuspending(consent)

      val choices = consentSdk.getConsentChoicesSuspending()

      choices[uuid] shouldBe true
      choices[secondUuid] shouldBe false
    }

    it("returns specific consent choice") {
      server.enqueue(MockResponse())

      consentSdk.postConsentSuspending(consent)

      val choice = consentSdk.getConsentChoiceSuspending(uuid)
      val secondChoice = consentSdk.getConsentChoiceSuspending(secondUuid)

      choice shouldBe true
      secondChoice shouldBe false
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
    getConsentSolution(
      consentSolutionId = consentId,
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
    postConsent(
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

internal suspend fun MobileConsentSdk.getConsentChoicesSuspending() =
  suspendCoroutine<Map<UUID, Boolean>> { continuation ->
    getConsentChoices(
      listener = object : CallListener<Map<UUID, Boolean>> {
        override fun onSuccess(result: Map<UUID, Boolean>) {
          continuation.resumeWith(Result.success(result))
        }

        override fun onFailure(error: IOException) {
          continuation.resumeWithException(error)
        }
      }
    )
  }

internal suspend fun MobileConsentSdk.getConsentChoiceSuspending(consentId: UUID) =
  suspendCoroutine<Boolean> { continuation ->
    getConsentChoice(
      consentItemId = consentId,
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
