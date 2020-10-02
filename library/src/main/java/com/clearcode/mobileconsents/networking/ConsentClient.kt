package com.clearcode.mobileconsents.networking

import com.clearcode.mobileconsents.Consent
import com.clearcode.mobileconsents.adapter.extension.parseToRequestBody
import com.clearcode.mobileconsents.networking.request.ConsentRequestJsonAdapter
import com.clearcode.mobileconsents.toRequest
import com.clearcode.mobileconsents.util.getUtcTimestamp
import com.squareup.moshi.Moshi
import okhttp3.Call
import okhttp3.HttpUrl
import okhttp3.Request
import java.util.UUID

private const val consentJsonFileName = "consent-data.json"

internal class ConsentClient(
  private val getUrl: HttpUrl,
  private val postUrl: HttpUrl,
  private val callFactory: Call.Factory,
  private val moshi: Moshi
) {

  fun getConsent(consentId: UUID): Call {
    val url = getUrl.newBuilder()
      .addPathSegment(consentId.toString())
      .addPathSegment(consentJsonFileName)
      .build()
    val request = Request.Builder().url(url).build()

    return callFactory.newCall(request)
  }

  fun postConsent(
    consent: Consent,
    userId: UUID,
    timestamp: String = getUtcTimestamp()
  ): Call {
    val adapter = ConsentRequestJsonAdapter(moshi)
    val requestBody = adapter.parseToRequestBody(consent.toRequest(userId, timestamp))

    val url = postUrl.newBuilder()
      .addPathSegment("consents")
      .build()

    val request = Request.Builder()
      .url(url)
      .post(requestBody)
      .build()

    return callFactory.newCall(request)
  }
}
