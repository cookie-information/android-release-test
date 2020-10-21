package com.cookieinformation.mobileconsents

import android.content.Context
import com.cookieinformation.mobileconsents.adapter.extension.parseFromResponseBody
import com.cookieinformation.mobileconsents.adapter.moshi
import com.cookieinformation.mobileconsents.networking.ConsentClient
import com.cookieinformation.mobileconsents.networking.extension.closeQuietly
import com.cookieinformation.mobileconsents.networking.extension.enqueueSuspending
import com.cookieinformation.mobileconsents.networking.response.ConsentSolutionResponseJsonAdapter
import com.cookieinformation.mobileconsents.networking.response.toDomain
import com.cookieinformation.mobileconsents.storage.ConsentStorage
import com.cookieinformation.mobileconsents.system.ApplicationProperties
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID

/**
 * SDK allowing easy management of user consents.
 * @param consentClient wrapper over [OkHttpClient], enabling for fetching and posting consents.
 * @param consentStorage storage saving all consent choices to local file.
 * @param applicationProperties information about app using SDK, required for sending consent to server.
 * @param dispatcher used for all async operations.
 *
 * For SDK instantiating use [MobileConsentSdk.Builder] static function.
 */
@Suppress("UnusedPrivateMember")
public class MobileConsentSdk internal constructor(
  private val consentClient: ConsentClient,
  private val consentStorage: ConsentStorage,
  private val applicationProperties: ApplicationProperties,
  dispatcher: CoroutineDispatcher
) {

  private val scope = CoroutineScope(dispatcher + SupervisorJob())

  /**
   * Obtain [ConsentSolution] from CDN server.
   * @param consentSolutionId UUID identifier of consent.
   * @param listener listener for success/failure of operation.
   * @returns [Subscription] an object allowing for call cancellation.
   */
  public fun fetchConsentSolution(consentSolutionId: UUID, listener: CallListener<ConsentSolution>): Subscription {
    val job = scope.launch {
      try {
        val call = consentClient.getConsentSolution(consentSolutionId)
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

  /**
   * Post [Consent] to server specified by url in SDK's builder.
   * @param consent consent object.
   * @param listener listener for success/failure of operation.
   * @returns [Subscription] object allowing for call cancellation.
   */
  public fun postConsent(consent: Consent, listener: CallListener<Unit>): Subscription {
    val job = scope.launch {
      try {
        val userId = consentStorage.getUserId()
        val call = consentClient.postConsent(consent, userId, applicationProperties)
        call.enqueueSuspending().closeQuietly()
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

  /**
   * Obtain past consent choices stored on device memory. Returns Map of ConsentItem id and choice in a form of Boolean.
   * @param listener listener for success/failure of operation.
   * @return [Subscription] object allowing for call cancellation.
   */
  public fun getSavedConsents(listener: CallListener<Map<UUID, Boolean>>): Subscription {
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

  /**
   * Obtain specific Consent choice stored on device memory. If choice is not stored in memory, this will return `false`.
   * @param listener listener for success/failure of operation.
   * @return [Subscription] object allowing for call cancellation.
   */
  public fun getSavedConsent(consentItemId: UUID, listener: CallListener<Boolean>): Subscription {
    val job = scope.launch {
      try {
        listener.onSuccess(consentStorage.getConsentChoice(consentItemId))
      } catch (e: IOException) {
        listener.onFailure(e)
      }
    }

    return object : Subscription {
      override fun cancel() = job.cancel()
    }
  }

  public companion object {
    /**
     * Use to instantiate SDK with all necessary parameters.
     * @return SDK builder instance.
     */
    @Suppress("FunctionNaming")
    @JvmStatic
    public fun Builder(context: Context): PartnerUrl = MobileConsentSdkBuilder(context)
  }
}
