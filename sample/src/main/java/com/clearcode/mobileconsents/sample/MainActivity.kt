package com.clearcode.mobileconsents.sample

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.clearcode.mobileconsents.domain.Consent
import com.clearcode.mobileconsents.networking.CallListener
import com.clearcode.mobileconsents.sdk.MobileConsentSdk
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.buttonFetch
import kotlinx.android.synthetic.main.activity_main.buttonUseSampleId
import kotlinx.android.synthetic.main.activity_main.layoutUuid
import kotlinx.android.synthetic.main.activity_main.recyclerConsents
import kotlinx.android.synthetic.main.activity_main.textError
import kotlinx.android.synthetic.main.activity_main.textUuid
import okhttp3.OkHttpClient
import java.io.IOException
import java.util.UUID

class MainActivity : AppCompatActivity() {

  lateinit var sdk: MobileConsentSdk

  private val consentItemAdapter = ConsentItemAdapter()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    setupForm()
    setupAdapter()

    sdk = MobileConsentSdk.Builder()
      .applicationContext(this.applicationContext)
      .partnerUrl(BuildConfig.BASE_URL)
      .callFactory(OkHttpClient())
      .build()
  }

  private fun setupForm() {
    buttonUseSampleId.setOnClickListener {
      textUuid.setText(getString(R.string.sample_uuid))
    }

    textUuid.addTextChangedListener { text: CharSequence? ->
      val valid = !text.isNullOrBlank()
      layoutUuid.error = if (valid) null else "UUID cannot be empty"
      buttonFetch.isEnabled = valid
    }

    buttonFetch.setOnClickListener {
      val uuid = try {
        UUID.fromString(textUuid.text.toString())
      } catch (e: IllegalArgumentException) {
        Snackbar.make(buttonFetch, e.message.toString(), Snackbar.LENGTH_LONG).show()
        return@setOnClickListener
      }
      fetchConsentSolution(uuid)
    }
  }

  private fun setupAdapter() {
    recyclerConsents.adapter = consentItemAdapter
  }

  private fun fetchConsentSolution(consentId: UUID) =
    sdk.getConsent(
      consentId = consentId,
      listener = object : CallListener<Consent> {
        override fun onSuccess(result: Consent) {
          postOnMainThread {
            consentItemAdapter.submitList(result.consentItems)
            textError?.text = ""
          }
        }

        override fun onFailure(error: IOException) {
          postOnMainThread {
            consentItemAdapter.submitList(emptyList())
            textError?.text = error.toString()
          }
        }
      }
    )
}

private inline fun postOnMainThread(crossinline block: () -> Unit) = Handler(Looper.getMainLooper()).post {
  block()
}
