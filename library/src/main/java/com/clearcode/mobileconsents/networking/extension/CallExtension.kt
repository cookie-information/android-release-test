package com.clearcode.mobileconsents.networking.extension

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal suspend fun Call.enqueueSuspending() = suspendCancellableCoroutine<ResponseBody> { continuation ->
  enqueue(
    object : Callback {
      override fun onFailure(call: Call, e: IOException) {
        continuation.resumeWithException(e)
      }

      override fun onResponse(call: Call, response: Response) {
        try {
          val body = response.bodyOrThrow()
          continuation.resume(body)
        } catch (e: IOException) {
          continuation.resumeWithException(e)
        }
      }
    }
  )

  continuation.invokeOnCancellation { this.cancel() }
}
