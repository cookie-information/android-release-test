package com.clearcode.mobileconsents.sample

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.clearcode.mobileconsents.domain.Consent
import com.clearcode.mobileconsents.networking.CallListener
import com.clearcode.mobileconsents.sdk.MobileConsentSdk
import java.util.UUID

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val sdk = MobileConsentSdk.Builder().postUrl(BuildConfig.BASE_URL).build()

    sdk.getConsent(
      consentId = UUID.fromString("843ddd4a-3eae-4286-a17b-0e8d3337e767"),
      listener = object : CallListener<Consent> {
        override fun onSuccess(result: Consent) {
          Log.e("MainActivity", result.toString())
        }

        override fun onFailure(error: Throwable) {
          error.printStackTrace()
        }
      }
    )
  }
}
