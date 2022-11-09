package com.cookieinformation.mobileconsents

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import com.cookieinformation.mobileconsents.adapter.moshi
import com.cookieinformation.mobileconsents.networking.ConsentClient
import com.cookieinformation.mobileconsents.storage.ConsentStorage
import com.cookieinformation.mobileconsents.storage.MoshiFileHandler
import com.cookieinformation.mobileconsents.storage.Preferences
import com.cookieinformation.mobileconsents.system.getApplicationProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.sync.Mutex
import okhttp3.Call
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import java.io.File
import java.lang.ref.WeakReference
import java.util.UUID

private const val storageFileName = "mobileconsents_storage.txt"

/**
 * Builder for SDK instances. The builder is implementation of Fluent Builder Pattern,
 * thus all parameters must be provided in a valid order. You can get instance of this builder
 * via [MobileConsentSdk.Builder] static function.
 */
public class MobileConsentSdkBuilder internal constructor(
  private val context: Context
) : CallFactory, SdkBuilder {
  private var callFactory: Call.Factory? = null

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

    val storageFile = File(context.filesDir, storageFileName)
    val preferences = Preferences(context.applicationContext)
    lateinit var consentClient: ConsentClient
    try {
      val uuid = UUID.fromString(BuildConfig.SOLUTION_ID)
      consentClient = ConsentClient(
        uuid,
        getUrl = BuildConfig.BASE_URL_CONSENT_SOLUTION.toHttpUrl(),
        postUrl = BuildConfig.BASE_URL_CONSENT.toHttpUrl(),
        callFactory = factory,
        moshi = moshi,
        preferences = preferences
      )
    } catch (e: IllegalArgumentException) {
      Toast.makeText(context.applicationContext, e.message.toString(), Toast.LENGTH_SHORT).show()
    }
    val consentStorage =
      ConsentStorage(Mutex, storageFile, MoshiFileHandler(moshi), getSaveConsentsMutableFlow(), Dispatchers.IO)
    return MobileConsentSdk(
      consentClient = consentClient,
      consentStorage = consentStorage,
      applicationProperties = context.getApplicationProperties(),
      dispatcher = Dispatchers.IO,
      saveConsentsFlow = consentStorage.saveConsentsFlow
    )
  }

  private companion object {
    /**
     *  Global Mutex, used for synchronization of writing data to the storage, shared across all SDK instances.
     */
    private val Mutex = Mutex()

    /**
     * Returns global flow for observing end emitting "save consents" events.
     */
    @SuppressLint("SyntheticAccessor")
    fun getSaveConsentsMutableFlow(): MutableSharedFlow<Map<UUID, Boolean>> = synchronized(this) {
      var eventsEmitter = SaveConsentsMutableFlowReference.get()
      if (eventsEmitter == null) {
        eventsEmitter = MutableSharedFlow()
        SaveConsentsMutableFlowReference = WeakReference(eventsEmitter)
      }
      eventsEmitter
    }

    /**
     * Reference to global flow for observing end emitting "save consents" events, shared across all SDK instances.
     * Warning: Do not use this field directly. Use [getSaveConsentsMutableFlow].
     */
    private var SaveConsentsMutableFlowReference = WeakReference<MutableSharedFlow<Map<UUID, Boolean>>>(null)
  }
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
