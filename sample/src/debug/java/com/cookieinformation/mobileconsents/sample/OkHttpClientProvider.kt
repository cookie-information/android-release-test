package com.cookieinformation.mobileconsents.sample

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor.Builder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

private const val useHttpLoggingInterceptor = true

fun getOkHttpClient(context: Context) = if (useHttpLoggingInterceptor) getHttpLoggingInterceptor() else getChuckerCollectorInterceptor(context)

fun getChuckerCollectorInterceptor(context: Context) = OkHttpClient.Builder().addInterceptor(
  Builder(context)
    .collector(ChuckerCollector(context))
    .maxContentLength(250000L)
    .redactHeaders(emptySet())
    .alwaysReadResponseBody(false)
    .build()
).build()

fun getHttpLoggingInterceptor() = OkHttpClient.Builder().addInterceptor(
  httpLoggingInterceptor
).build()

private val httpLoggingInterceptor : HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
  this.level = HttpLoggingInterceptor.Level.BODY
}


