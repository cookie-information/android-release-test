package com.cookieinformation.mobileconsents

import android.content.Context
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor


internal fun getOkHttpClient(context: Context): Call.Factory = getHttpLoggingInterceptor()
public fun getHttpLoggingInterceptor(): OkHttpClient = OkHttpClient.Builder().addInterceptor(
  httpLoggingInterceptor
).build()

private val httpLoggingInterceptor : HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
  this.level = HttpLoggingInterceptor.Level.BODY
}


