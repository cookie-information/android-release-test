package com.clearcode.mobileconsents.sdk

import com.clearcode.mobileconsents.BuildConfig
import com.clearcode.mobileconsents.adapter.extension.parseResponseBody
import com.clearcode.mobileconsents.adapter.moshi
import com.clearcode.mobileconsents.domain.Consent
import com.clearcode.mobileconsents.networking.CallListener
import com.clearcode.mobileconsents.networking.ConsentClient
import com.clearcode.mobileconsents.networking.Subscription
import com.clearcode.mobileconsents.networking.extension.bodyOrThrow
import com.clearcode.mobileconsents.networking.response.ConsentResponseJsonAdapter
import com.clearcode.mobileconsents.networking.response.toDomain
import com.clearcode.mobileconsents.storage.ConsentStorage
import com.squareup.moshi.Moshi
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Response
import java.io.IOException
import java.util.UUID

public class MobileConsentSdk internal constructor(
  private val consentClient: ConsentClient,
  private val consentStorage: ConsentStorage,
  private val moshi: Moshi
) {

  public fun getConsent(consentId: UUID, listener: CallListener<Consent>): Subscription {
    val call = consentClient.getConsent(consentId)

    call.enqueue(
      object : Callback {
        override fun onFailure(call: Call, e: IOException) {
          listener.onFailure(e)
        }

        override fun onResponse(call: Call, response: Response) {
          try {
            val body = response.bodyOrThrow()
            val adapter = ConsentResponseJsonAdapter(moshi)
            val result = adapter.parseResponseBody(body)
            listener.onSuccess(result.toDomain())
          } catch (error: IOException) {
            listener.onFailure(error)
          }
        }
      }
    )

    return object : Subscription {
      override fun cancel() = call.cancel()
    }
  }

  // TODO [CLEAR-10] Add error handling, domain objects and parsing
  public fun postConsentItem(
    consentItem: String,
    listener: CallListener<Boolean>
  ): Subscription {
    val call = consentClient.postConsent(consentItem)

    call.enqueue(
      object : Callback {
        override fun onFailure(call: Call, e: IOException) {
          listener.onFailure(e)
        }

        override fun onResponse(call: Call, response: Response) {
          // TODO [CLEAR-11] map response, save to storage
          listener.onSuccess(true)
        }
      }
    )

    return object : Subscription {
      override fun cancel() = call.cancel()
    }
  }

  public class Builder {

    private var postUrl: HttpUrl? = null

    public fun postUrl(url: HttpUrl): Builder = apply {
      postUrl = url
    }

    public fun postUrl(url: String): Builder = apply {
      postUrl = try {
        url.toHttpUrl()
      } catch (e: Exception) {
        throw IllegalArgumentException("$url is not a valid url", e)
      }
    }

    public fun build(): MobileConsentSdk {
      val basePostUrl = postUrl ?: error("Use postUrl() method to specify url for posting consents")

      val consentClient = ConsentClient(
        getUrl = BuildConfig.BASE_URL.toHttpUrl(),
        postUrl = basePostUrl
      )

      val consentStorage = ConsentStorage()

      return MobileConsentSdk(consentClient, consentStorage, moshi)
    }
  }
}
