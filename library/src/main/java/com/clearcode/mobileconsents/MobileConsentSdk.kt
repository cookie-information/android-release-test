package com.clearcode.mobileconsents

import android.content.Context
import com.clearcode.mobileconsents.adapter.extension.parseFromResponseBody
import com.clearcode.mobileconsents.adapter.moshi
import com.clearcode.mobileconsents.networking.CallListener
import com.clearcode.mobileconsents.networking.ConsentClient
import com.clearcode.mobileconsents.networking.Subscription
import com.clearcode.mobileconsents.networking.extension.enqueueSuspending
import com.clearcode.mobileconsents.networking.response.ConsentSolutionResponseJsonAdapter
import com.clearcode.mobileconsents.networking.response.toDomain
import com.clearcode.mobileconsents.storage.ConsentStorage
import com.clearcode.mobileconsents.storage.MoshiFileHandler
import com.clearcode.mobileconsents.system.getApplicationProperties
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import okhttp3.Call
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import java.io.File
import java.io.IOException
import java.util.UUID

private const val storageFileName = "storage.txt"

public class MobileConsentSdk internal constructor(
  private val consentClient: ConsentClient,
  private val consentStorage: ConsentStorage,
  private val applicationProperties: ApplicationProperties,
  dispatcher: CoroutineDispatcher
) {

  @JvmSynthetic
  private val scope = CoroutineScope(dispatcher + SupervisorJob())

  public fun getConsent(consentId: UUID, listener: CallListener<ConsentSolution>): Subscription {
    val job = scope.launch {
      try {
        val call = consentClient.getConsent(consentId)
        val responseBody = call.enqueueSuspending()
        val adapter = ConsentSolutionResponseJsonAdapter(moshi)
        val result = adapter.parseFromResponseBody(responseBody)
        listener.onSuccess(result.toDomain())
      } catch (error: IOException) {
        listener.onFailure(error)
      }
    }

    return object : Subscription {
      override fun cancel() = job.cancel()
    }
  }

  public fun postConsentItem(consent: Consent, listener: CallListener<Unit>): Subscription {
    val job = scope.launch {
      try {
        val userId = consentStorage.getUserId()
        val call = consentClient.postConsent(consent, userId)
        call.enqueueSuspending()
        consentStorage.storeConsentChoices(consent.processingPurposes)
        listener.onSuccess(Unit)
      } catch (e: IOException) {
        listener.onFailure(e)
      }
    }

    return object : Subscription {
      override fun cancel() = job.cancel()
    }
  }

  public fun getConsentChoices(listener: CallListener<Map<UUID, Boolean>>): Subscription {
    val job = scope.launch {
      try {
        listener.onSuccess(consentStorage.getAllConsentChoices())
      } catch (e: IOException) {
        listener.onFailure(e)
      }
    }

    return object : Subscription {
      override fun cancel() = job.cancel()
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
        callFactory = factory,
        moshi = moshi
      )
      val consentStorage = ConsentStorage(Mutex, storageFile, MoshiFileHandler(moshi))

      return MobileConsentSdk(consentClient, consentStorage, applicationProperties, Dispatchers.IO)
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
