package com.clearcode.mobileconsents.sdk

import android.content.Context
import com.clearcode.mobileconsents.BuildConfig
import com.clearcode.mobileconsents.adapter.extension.parseResponseBody
import com.clearcode.mobileconsents.adapter.moshi
import com.clearcode.mobileconsents.domain.ApplicationProperties
import com.clearcode.mobileconsents.domain.Consent
import com.clearcode.mobileconsents.networking.CallListener
import com.clearcode.mobileconsents.networking.ConsentClient
import com.clearcode.mobileconsents.networking.Subscription
import com.clearcode.mobileconsents.networking.extension.bodyOrThrow
import com.clearcode.mobileconsents.networking.response.ConsentResponseJsonAdapter
import com.clearcode.mobileconsents.networking.response.toDomain
import com.clearcode.mobileconsents.storage.ConsentStorage
import com.clearcode.mobileconsents.storage.MoshiFileHandler
import com.clearcode.mobileconsents.system.getApplicationProperties
import com.squareup.moshi.Moshi
import kotlinx.coroutines.sync.Mutex
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.util.UUID

private const val storageFileName = "storage.txt"

public class MobileConsentSdk internal constructor(
  private val consentClient: ConsentClient,
  private val consentStorage: ConsentStorage,
  private val applicationProperties: ApplicationProperties,
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
    private var context: Context? = null
    private var postUrl: HttpUrl? = null
    private var callFactory: Call.Factory? = null

    public fun partnerUrl(url: HttpUrl): Builder = apply {
      postUrl = url
    }

    public fun partnerUrl(url: String): Builder = apply {
      postUrl = try {
        url.toHttpUrl()
      } catch (e: Exception) {
        throw IllegalArgumentException("$url is not a valid url", e)
      }
    }

    public fun callFactory(factory: Call.Factory): Builder = apply {
      callFactory = factory
    }

    public fun applicationContext(applicationContext: Context): Builder = apply {
      context = applicationContext
    }

    public fun build(): MobileConsentSdk {
      val basePostUrl = requireNotNull(postUrl) { "Use postUrl() method to specify url for posting consents." }
      val androidContext = requireNotNull(context) {
        "Use applicationContext() method to specify your application Context."
      }
      val factory = callFactory ?: OkHttpClient()

      val applicationProperties = androidContext.getApplicationProperties()
      val storageFile = File(androidContext.filesDir, storageFileName)
      val consentClient = ConsentClient(
        getUrl = BuildConfig.BASE_URL.toHttpUrl(),
        postUrl = basePostUrl,
        callFactory = factory
      )
      val consentStorage = ConsentStorage(Mutex, storageFile, MoshiFileHandler(moshi))

      return MobileConsentSdk(consentClient, consentStorage, applicationProperties, moshi)
    }

    /**
     *  Global Mutex is required in case when there are more then one instance of SDK. When couple of threads access
     *  storage at the same time we can lose some data when overwriting file.
     */
    private companion object {
      private val Mutex = Mutex()
    }
  }
}
