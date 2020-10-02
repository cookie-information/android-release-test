package com.clearcode.mobileconsents.sample

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.clearcode.mobileconsents.Consent
import com.clearcode.mobileconsents.ConsentSolution
import com.clearcode.mobileconsents.MobileConsentSdk
import com.clearcode.mobileconsents.ProcessingPurpose
import com.clearcode.mobileconsents.networking.CallListener
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.buttonFetch
import kotlinx.android.synthetic.main.activity_main.buttonSend
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

    setupTexts()
    setupButtons()
    setupAdapter()

    sdk = MobileConsentSdk.Builder()
      .applicationContext(this.applicationContext)
      .partnerUrl(getString(R.string.sample_partner_url))
      .callFactory(OkHttpClient())
      .build()
  }

  private fun setupTexts() {
    textUuid.addTextChangedListener { text: CharSequence? ->
      val valid = !text.isNullOrBlank()
      layoutUuid.error = if (valid) null else "UUID cannot be empty"
      buttonFetch.isEnabled = valid
    }
  }

  private fun setupButtons() {
    buttonUseSampleId.setOnClickListener {
      textUuid.setText(getString(R.string.sample_uuid))
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
    buttonSend.setOnClickListener {
      sendConsent(dummyConsent)
    }
  }

  private fun setupAdapter() {
    recyclerConsents.adapter = consentItemAdapter
  }

  private fun fetchConsentSolution(consentId: UUID) =
    sdk.getConsent(
      consentId = consentId,
      listener = object : CallListener<ConsentSolution> {
        override fun onSuccess(result: ConsentSolution) {
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

  private fun sendConsent(consent: Consent) {
    val subscription = sdk.postConsentItem(
      consent = consent,
      listener = object : CallListener<Unit> {
        override fun onSuccess(result: Unit) {
          postOnMainThread {
            Snackbar.make(buttonFetch, "Consents sent successfully", Snackbar.LENGTH_LONG).show()
          }
        }

        override fun onFailure(error: IOException) {
          postOnMainThread {
            textError?.text = error.toString()
          }
        }
      }
    )
    subscription.cancel()
  }
}

// TODO [CLEAR-49] remove after providing real implementation of sending in app
val dummyConsent = Consent(
  consentSolutionId = UUID.fromString("843ddd4a-3eae-4286-a17b-0e8d3337e767"),
  consentSolutionVersionId = UUID.fromString("00000000-0000-4000-8000-000000000000"),
  processingPurposes = listOf(
    ProcessingPurpose(
      consentItemId = UUID.fromString("66b12655-02e9-4d8c-a4bf-445f3b444831"),
      consentGiven = true,
      language = "EN"
    )
  ),
  customData = mapOf("email" to "example@gmail.com")
)

private inline fun postOnMainThread(crossinline block: () -> Unit) = Handler(Looper.getMainLooper()).post {
  block()
}
