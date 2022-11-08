package com.cookieinformation.mobileconsents.sample

import android.app.Application
import com.cookieinformation.mobileconsents.BuildConfig
import com.cookieinformation.mobileconsents.CallbackMobileConsentSdk
import com.cookieinformation.mobileconsents.MobileConsentSdk
import okhttp3.HttpUrl.Companion.toHttpUrl

class App : Application() {

  internal lateinit var sdk: CallbackMobileConsentSdk

  override fun onCreate() {
    super.onCreate()
    sdk = CallbackMobileConsentSdk.from(
      MobileConsentSdk.Builder(this)
        .partnerUrl(BuildConfig.BASE_URL_CONSENT.toHttpUrl()) //TODO remove this
        .callFactory(getOkHttpClient(this))
        .build()
    )
  }
}
