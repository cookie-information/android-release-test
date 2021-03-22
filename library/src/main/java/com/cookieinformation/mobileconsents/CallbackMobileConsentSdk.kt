package com.cookieinformation.mobileconsents

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID

/**
 * SDK allowing easy management of user consents.
 * @param mobileConsentSdk instance of coroutine version of the SDK
 * @param dispatcher used for all async operations.
 *
 * For SDK instantiating use [CallbackMobileConsentSdk.from] static function.
 */
public class CallbackMobileConsentSdk internal constructor(
  private val mobileConsentSdk: MobileConsentSdk,
  dispatcher: CoroutineDispatcher
) {

  private val scope = CoroutineScope(dispatcher)

  /**
   * Returns associated instance of MobileConsentSdk.
   *
   * @return associated instance of MobileConsentSdk.
   */
  public fun getMobileConsentSdk(): MobileConsentSdk = mobileConsentSdk

  /**
   * Obtain [ConsentSolution] from CDN server.
   * @param consentSolutionId UUID identifier of consent.
   * @param listener listener for success/failure of operation.
   * @returns [Subscription] an object allowing for call cancellation.
   */
  public fun fetchConsentSolution(consentSolutionId: UUID, listener: CallListener<ConsentSolution>): Subscription {
    val job = scope.launch {
      try {
        val result = mobileConsentSdk.fetchConsentSolution(consentSolutionId)
        listener.onSuccess(result)
      } catch (error: IOException) {
        listener.onFailure(error)
      }
    }

    return JobSubscription(job)
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
        mobileConsentSdk.postConsent(consent)
        listener.onSuccess(Unit)
      } catch (e: IOException) {
        listener.onFailure(e)
      }
    }

    return JobSubscription(job)
  }

  /**
   * Obtain past consent choices stored on device memory. Returns Map of ConsentItem id and choice in a form of Boolean.
   * @param listener listener for success/failure of operation.
   * @return [Subscription] object allowing for call cancellation.
   */
  public fun getSavedConsents(listener: CallListener<Map<UUID, Boolean>>): Subscription {
    val job = scope.launch {
      try {
        listener.onSuccess(mobileConsentSdk.getSavedConsents())
      } catch (e: IOException) {
        listener.onFailure(e)
      }
    }

    return JobSubscription(job)
  }

  /**
   * Obtain specific Consent choice stored on device memory. If choice is not stored in memory, this will return `false`.
   * @param listener listener for success/failure of operation.
   * @return [Subscription] object allowing for call cancellation.
   */
  public fun getSavedConsent(consentItemId: UUID, listener: CallListener<Boolean>): Subscription {
    val job = scope.launch {
      try {
        listener.onSuccess(mobileConsentSdk.getSavedConsent(consentItemId))
      } catch (e: IOException) {
        listener.onFailure(e)
      }
    }

    return JobSubscription(job)
  }

  private class JobSubscription(val job: Job) : Subscription {
    override fun cancel() = job.cancel()
  }

  public companion object {
    /**
     * Use to instantiate SDK with all necessary parameters.
     * @return SDK builder instance.
     */
    @Suppress("FunctionNaming")
    @JvmStatic
    public fun from(mobileConsentSdk: MobileConsentSdk): CallbackMobileConsentSdk =
      CallbackMobileConsentSdk(mobileConsentSdk, Dispatchers.Main)
  }
}

/**
 * Interface returned from every async operation of SDK, use it for cancellation of background operations.
 */
public interface Subscription {
  /**
   * Cancel ongoing background operation.
   */
  public fun cancel()
}
