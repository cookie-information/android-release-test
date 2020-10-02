package com.clearcode.mobileconsents.networking.extension

import okhttp3.Response
import okhttp3.ResponseBody
import java.io.IOException

internal fun Response.bodyOrThrow(): ResponseBody =
  if (isSuccessful) {
    body ?: throw IOException("Response body cannot be null")
  } else {
    val message =
      """
      |Url: ${request.url}
      |Code: $code
      |Message: ${body?.string()}
      """.trimMargin()
    throw IOException(message)
  }
