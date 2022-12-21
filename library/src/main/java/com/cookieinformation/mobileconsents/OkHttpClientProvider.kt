package com.cookieinformation.mobileconsents

import android.content.Context
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

private const val useHttpLoggingInterceptor = true

internal fun getOkHttpClient(context: Context): Call.Factory = getHttpLoggingInterceptor()// else getChuckerCollectorInterceptor(context)
//public fun getOkHttpClient(context: Context): Call.Factory = if (useHttpLoggingInterceptor) getHttpLoggingInterceptor()// else getChuckerCollectorInterceptor(context)

//fun getChuckerCollectorInterceptor(context: Context) = OkHttpClient.Builder().addInterceptor(
//  Builder(context)
//    .collector(ChuckerCollector(context))
//    .maxContentLength(250000L)
//    .redactHeaders(emptySet())
//    .alwaysReadResponseBody(false)
//    .build()
//).build()

public fun getHttpLoggingInterceptor(): OkHttpClient = OkHttpClient.Builder().addInterceptor(
  httpLoggingInterceptor
).build()

private val httpLoggingInterceptor : HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
  this.level = HttpLoggingInterceptor.Level.BODY
}


