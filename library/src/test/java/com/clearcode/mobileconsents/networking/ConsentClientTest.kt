package com.clearcode.mobileconsents.networking

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

  val uuid = UUID.fromString("312d021c-e3e5-4af2-a031-76de65d2c72c")
  val okHttpClient = OkHttpClient()
  lateinit var server: MockWebServer
  lateinit var baseUrl: HttpUrl
  lateinit var consentClient: ConsentClient

  beforeTest {
    server = MockWebServer()
    server.start()

    baseUrl = server.url("/api/test")

    consentClient = ConsentClient(baseUrl, baseUrl, okHttpClient)
  }

  afterTest {
    server.shutdown()
  }

  describe("ConsentClient") {

    it("on invoked cancellation cancels the request") {
      val itemPayload = "{name: \"name\"}"

      val consentCall = consentClient.postConsent(itemPayload)

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
      consentClient.postConsent("{name: \"name\"}").enqueue(EmptyCallback)

      val request = server.takeRequest()
      request.requestUrl shouldBe baseUrl
    }

    it("sends consent payload to server") {
      val itemPayload = "{name: \"name\"}"

      consentClient.postConsent(itemPayload).enqueue(EmptyCallback)

      val request = server.takeRequest()
      request.body.readUtf8() shouldBe itemPayload
    }
  }
})

internal object EmptyCallback : Callback {
  override fun onFailure(call: Call, e: IOException) = Unit

  override fun onResponse(call: Call, response: Response) = Unit
}
