package com.clearcode.mobileconsents.sample

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.clearcode.mobileconsents.CallListener
import com.clearcode.mobileconsents.Consent
import com.clearcode.mobileconsents.ConsentSolution
import com.clearcode.mobileconsents.MobileConsentSdk
import com.clearcode.mobileconsents.ProcessingPurpose
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.buttonFetch
import kotlinx.android.synthetic.main.activity_main.buttonSend
import kotlinx.android.synthetic.main.activity_main.buttonStorage
import kotlinx.android.synthetic.main.activity_main.buttonUseSampleId
import kotlinx.android.synthetic.main.activity_main.layoutUuid
import kotlinx.android.synthetic.main.activity_main.recyclerConsents
import kotlinx.android.synthetic.main.activity_main.textError
import kotlinx.android.synthetic.main.activity_main.textLanguage
import kotlinx.android.synthetic.main.activity_main.textUuid
import java.io.IOException
import java.util.UUID

class MainActivity : AppCompatActivity(R.layout.activity_main) {

  private lateinit var sdk: MobileConsentSdk

  private val consentItemAdapter = ConsentItemAdapter { uuid, choice -> consentItemChoices[uuid] = choice }
  private var consentSolution: ConsentSolution? = null
  private val consentItemChoices: MutableMap<UUID, Boolean> = mutableMapOf()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setupForm()
    setupActionButtons()
    setupAdapter()

    sdk = MobileConsentSdk.Builder(this.applicationContext)
      .partnerUrl(getString(R.string.sample_partner_url))
      .callFactory(getOkHttpClient(this.applicationContext))
      .build()
  }

  private fun setupForm() {
    textUuid.addTextChangedListener { text: CharSequence? ->
      val valid = !text.isNullOrBlank()
      layoutUuid.error = if (valid) null else "UUID cannot be empty"
      buttonFetch.isEnabled = valid
    }
    buttonUseSampleId.setOnClickListener {
      textUuid.setText(getString(R.string.sample_uuid))
    }
    buttonFetch.setOnClickListener {
      val uuid = try {
        UUID.fromString(textUuid.text.toString())
      } catch (e: IllegalArgumentException) {
        Snackbar.make(buttonFetch, e.message.toString(), Snackbar.LENGTH_SHORT).show()
        return@setOnClickListener
      }
      fetchConsentSolution(uuid)
    }
  }

  private fun setupActionButtons() {
    buttonSend.setOnClickListener {
      consentSolution?.let { solution ->
        sendConsent(createConsent(solution))
      }
    }
    buttonStorage.setOnClickListener {
      startActivity(Intent(this, StorageActivity::class.java))
    }
  }

  private fun setupAdapter() {
    recyclerConsents.adapter = consentItemAdapter
  }

  private fun fetchConsentSolution(consentId: UUID) {
    setupData(null)
    sdk.getConsentSolution(
      consentSolutionId = consentId,
      listener = object : CallListener<ConsentSolution> {
        override fun onSuccess(result: ConsentSolution) {
          postOnMainThread {
            textError?.text = ""
            setupData(result)
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

  private fun sendConsent(consent: Consent) {
    setupData(null)
    sdk.postConsent(
      consent = consent,
      listener = object : CallListener<Unit> {
        override fun onSuccess(result: Unit) {
          postOnMainThread {
            Snackbar.make(buttonFetch, "Consents sent successfully", Snackbar.LENGTH_SHORT).show()
          }
        }

        override fun onFailure(error: IOException) {
          postOnMainThread {
            textError?.text = error.toString()
          }
        }
      }
    )
  }

  private fun setupData(fetchedConsent: ConsentSolution?) {
    consentSolution = fetchedConsent
    consentItemChoices.clear()
    fetchedConsent?.consentItems?.forEach {
      consentItemChoices[it.consentItemId] = false
    }
    buttonSend.isEnabled = fetchedConsent != null
    consentItemAdapter.submitList(fetchedConsent?.consentItems, textLanguage.text.toString())
  }

  private fun createConsent(consentSolution: ConsentSolution) = Consent(
    consentSolutionId = consentSolution.consentSolutionId,
    consentSolutionVersionId = consentSolution.consentSolutionVersionId,
    processingPurposes = consentItemChoices.map { (id, choice) ->
      ProcessingPurpose(
        consentItemId = id,
        consentGiven = choice,
        language = "EN"
      )
    },
    customData = emptyMap()
  )
}

private inline fun postOnMainThread(crossinline block: () -> Unit) = Handler(Looper.getMainLooper()).post {
  block()
}
