package com.clearcode.mobileconsents.sdk

import com.clearcode.mobileconsents.networking.CallListener
import com.clearcode.mobileconsents.networking.ConsentClient
import com.clearcode.mobileconsents.networking.Subscription
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.util.UUID

// TODO implement Builder for this class
public class ConsentSdk private constructor(private val consentClient: ConsentClient) {

  public fun getConsent(consentId: UUID, listener: CallListener<String>): Subscription {
    val call = consentClient.getConsent(consentId)

    call.enqueue(
      object : Callback {
        override fun onFailure(call: Call, e: IOException) {
          listener.onFailure(e)
        }

        override fun onResponse(call: Call, response: Response) {
          // TODO map response
          listener.onSuccess("success")
        }
      }
    )

    return object : Subscription {
      override fun cancel() = call.cancel()
    }
  }

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
          // TODO map response, save to storage
          listener.onSuccess(true)
        }
      }
    )

    return object : Subscription {
      override fun cancel() = call.cancel()
    }
  }
}
