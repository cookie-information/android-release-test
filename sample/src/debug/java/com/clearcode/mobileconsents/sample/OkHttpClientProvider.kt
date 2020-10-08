package com.clearcode.mobileconsents.sample

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import okhttp3.OkHttpClient

fun getOkHttpClient(context: Context) = OkHttpClient.Builder().addInterceptor(ChuckerInterceptor(context)).build()
