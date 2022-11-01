package com.cookieinformation.mobileconsents.sample

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor.Builder
import okhttp3.OkHttpClient

fun getOkHttpClient(context: Context) = OkHttpClient.Builder().addInterceptor(
  Builder(context)
    .collector(ChuckerCollector(context))
    .maxContentLength(250000L)
    .redactHeaders(emptySet())
    .alwaysReadResponseBody(false)
    .build()
).build()
