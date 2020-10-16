package com.cookieinformation.mobileconsents

import android.content.Context
import com.cookieinformation.mobileconsents.adapter.moshi
import com.cookieinformation.mobileconsents.networking.ConsentClient
import com.cookieinformation.mobileconsents.storage.ConsentStorage
import com.cookieinformation.mobileconsents.storage.MoshiFileHandler
import com.cookieinformation.mobileconsents.system.getApplicationProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import okhttp3.Call
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import java.io.File

private const val storageFileName = "storage.txt"

/**
 * Builder for SDK instances. The builder is implementation of Fluent Builder Patternt,
 * thus all parameters must be provided in a valid order. You can get instance of this builder
 * via [MobileConsentSdk.Builder] static function.
 */
public class MobileConsentSdkBuilder internal constructor(
  private val context: Context
) : PartnerUrl, CallFactory, SdkBuilder {
  private lateinit var postUrl: HttpUrl
  private var callFactory: Call.Factory? = null

  /**
   * Provide URL of partner server, where all consent choices should be sent.
   * Example [url]: `https://consents-gathering.com`.
   */
  override fun partnerUrl(url: HttpUrl): CallFactory = apply {
    postUrl = url
  }

  /**
   * Provide string representation of url of partner server, where all consent choices should be sent.
   * @throws [IllegalArgumentException] thrown when invalid [url] is provided.
   * Example [url]: `https://consents-gathering.com`.
   */
  override fun partnerUrl(url: String): CallFactory = apply {
    postUrl = try {
      url.toHttpUrl()
    } catch (e: Exception) {
      throw IllegalArgumentException("$url is not a valid url", e)
    }
  }

  /**
   * Provide your own [Call.Factory] for SDK usage. If no call factory is provided, SDK will instantiate it's own OkHttpClient.
   * Note that instantiating OkHttpClient can be expensive operation and will be performed on caller's thread,
   * thus providing your own factory is more optimal.
   */
  override fun callFactory(factory: Call.Factory): SdkBuilder = apply {
    callFactory = factory
  }

  override fun build(): MobileConsentSdk {
    val factory = callFactory ?: OkHttpClient()

    val applicationProperties = context.getApplicationProperties()
    val storageFile = File(context.filesDir, storageFileName)
    val consentClient = ConsentClient(
      getUrl = BuildConfig.BASE_URL.toHttpUrl(),
      postUrl = postUrl,
      callFactory = factory,
      moshi = moshi
    )
    val consentStorage = ConsentStorage(Mutex, storageFile, MoshiFileHandler(moshi))

    return MobileConsentSdk(consentClient, consentStorage, applicationProperties, Dispatchers.IO)
  }
  private companion object {
    /**
     *  Global Mutex, used for synchronization of writing data to the storage, shared across all SDK instances.
     */
    private val Mutex = Mutex()
  }
}

/**
 * Fluent Builder [MobileConsentSdkBuilder] interface.
 */
public interface PartnerUrl {
  public fun partnerUrl(url: HttpUrl): CallFactory
  public fun partnerUrl(url: String): CallFactory
}

/**
 * Fluent Builder [MobileConsentSdkBuilder] interface.
 */
public interface CallFactory {
  public fun callFactory(factory: Call.Factory): SdkBuilder
  public fun build(): MobileConsentSdk
}

/**
 * Fluent Builder [MobileConsentSdkBuilder] interface.
 */
public interface SdkBuilder {
  public fun build(): MobileConsentSdk
}
