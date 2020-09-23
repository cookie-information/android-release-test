package com.clearcode.mobileconsents.networking

import okhttp3.Call
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID
import kotlin.LazyThreadSafetyMode.NONE

private const val jsonMediaType = "application/json"
private const val consentJsonFileName = "consent-data.json"

internal class ConsentClient(
  private val getUrl: HttpUrl,
  private val postUrl: HttpUrl
) {

  private val okHttpClient by lazy(NONE) { OkHttpClient() }

  fun getConsent(consentId: UUID): Call {
    val url = getUrl.newBuilder()
      .addPathSegment(consentId.toString())
      .addPathSegment(consentJsonFileName)
      .build()
    val request = Request.Builder().url(url).build()

    return okHttpClient.newCall(request)
  }

  // TODO change consent item to class when domain will be defined
  fun postConsent(consentItem: String): Call {
    val request =
      Request.Builder().url(postUrl).post(consentItem.toRequestBody(jsonMediaType.toMediaType())).build()

    return okHttpClient.newCall(request)
  }
}
