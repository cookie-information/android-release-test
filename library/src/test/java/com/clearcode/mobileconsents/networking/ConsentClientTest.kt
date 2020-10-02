package com.clearcode.mobileconsents.networking

import com.clearcode.mobileconsents.Consent
import com.clearcode.mobileconsents.ProcessingPurpose
import com.clearcode.mobileconsents.adapter.moshi
import com.clearcode.mobileconsents.sdk.getResourceAsString
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.mockwebserver.MockWebServer
import java.io.IOException
import java.util.UUID

internal class ConsentClientTest : DescribeSpec({

  val uuid = UUID.fromString("095ed226-1ed9-4bbf-95a9-2df1e5118d2d")
  val timestamp = "2020-07-29T09:00:00.000Z"
  val okHttpClient = OkHttpClient()
  val consentRequest = javaClass.getResourceAsString("/consent.json")
  lateinit var server: MockWebServer
  lateinit var baseUrl: HttpUrl
  lateinit var consentClient: ConsentClient

  beforeTest {
    server = MockWebServer()
    server.start()

    baseUrl = server.url("/api/test")

    consentClient = ConsentClient(baseUrl, baseUrl, okHttpClient, moshi)
  }

  afterTest {
    server.shutdown()
  }

  describe("ConsentClient") {

    it("on invoked cancellation cancels the request") {

      val consentCall = consentClient.postConsent(dummyConsent, uuid, timestamp)

      consentCall.enqueue(EmptyCallback)
      consentCall.cancel()

      val requestCount = server.requestCount

      requestCount shouldBe 0
    }

    it("creates valid url for getting consents") {
      consentClient.getConsent(consentId = uuid).enqueue(EmptyCallback)

      val request = server.takeRequest()
      request.requestUrl shouldBe baseUrl.newBuilder()
        .addPathSegment(uuid.toString())
        .addPathSegment("consent-data.json")
        .build()
    }

    it("creates valid url for posting consents") {
      consentClient.postConsent(dummyConsent, uuid, timestamp).enqueue(EmptyCallback)

      val request = server.takeRequest()
      request.requestUrl shouldBe baseUrl.newBuilder()
        .addPathSegment("consents")
        .build()
    }

    it("sends consent payload to server") {
      consentClient.postConsent(dummyConsent, uuid, timestamp).enqueue(EmptyCallback)

      val request = server.takeRequest()
      request.body.readUtf8() shouldBe consentRequest
    }
  }
})

internal object EmptyCallback : Callback {
  override fun onFailure(call: Call, e: IOException) = Unit

  override fun onResponse(call: Call, response: Response) = Unit
}

private val dummyConsent = Consent(
  consentSolutionId = UUID.fromString("843ddd4a-3eae-4286-a17b-0e8d3337e767"),
  consentSolutionVersionId = UUID.fromString("00000000-0000-4000-8000-000000000000"),
  processingPurposes = listOf(
    ProcessingPurpose(
      consentItemId = UUID.fromString("51473b60-94da-401e-90b3-8f42d8b233d6"),
      consentGiven = true,
      language = "PL"
    )
  ),
  customData = mapOf("email" to "test@test.com")
)
