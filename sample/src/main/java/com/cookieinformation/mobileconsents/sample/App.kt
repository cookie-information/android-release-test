package com.cookieinformation.mobileconsents.sample

import android.app.Application
import com.cookieinformation.mobileconsents.MobileConsents

class App : Application() {

  internal lateinit var sdk: MobileConsents

  override fun onCreate() {
    super.onCreate()
//    sdk = MobileConsents.from(
//       MobileConsentSdk.Builder(this)
////           TODO("Can this be implemented in sdk?, user should not need to add this")
////        .callFactory(getOkHttpClient(this))
//        .setClientId("40dbe5a7-1c01-463a-bb08-a76970c0efa0")
//         .setClientSecret("68cbf024407a20b8df4aecc3d9937f43c6e83169dafcb38b8d18296b515cc0d5f8bca8165d615caa4d12e236192851e9c5852a07319428562af8f920293bc1db")
//         .setSolutionId("4113ab88-4980-4429-b2d1-3454cc81197b")
//        .build()
//    )

//    sdk.displayConsents(this, {
//
//    })
  }
}
