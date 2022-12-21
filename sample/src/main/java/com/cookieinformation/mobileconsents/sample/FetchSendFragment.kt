package com.cookieinformation.mobileconsents.sample

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.cookieinformation.mobileconsents.CallListener
import com.cookieinformation.mobileconsents.MobileConsents
import com.cookieinformation.mobileconsents.Consent
import com.cookieinformation.mobileconsents.ConsentItem
import com.cookieinformation.mobileconsents.ConsentSolution
import com.cookieinformation.mobileconsents.ProcessingPurpose
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_fetch_send.buttonFetch
import kotlinx.android.synthetic.main.fragment_fetch_send.buttonSend
import kotlinx.android.synthetic.main.fragment_fetch_send.recyclerConsents
import kotlinx.android.synthetic.main.fragment_fetch_send.textError
import kotlinx.android.synthetic.main.fragment_fetch_send.textLanguage
import kotlinx.android.synthetic.main.fragment_fetch_send.toolbar
import java.io.IOException
import java.util.UUID

class FetchSendFragment : Fragment(R.layout.fragment_fetch_send) {

  private lateinit var consentSdk: MobileConsents

  private val consentItemAdapter = ConsentItemAdapter { uuid, choice -> consentItemChoices[uuid] = choice }
  private var consentSolution: ConsentSolution? = null
  private val consentItemChoices: MutableMap<UUID, Boolean> = mutableMapOf()
  // TODO Implement restoring state when configuration changes (using ViewModel).

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val app = requireContext().applicationContext as App
    consentSdk = app.sdk
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupForm()
    setupActionButtons()
    setupAdapter()
  }

  private fun setupForm() {
    buttonFetch.setOnClickListener {
      fetchConsentSolution()
    }
  }

  private fun setupActionButtons() {
    toolbar.setNavigationOnClickListener {
      requireActivity().onBackPressed()
    }
    buttonSend.setOnClickListener {
      consentSolution?.let { solution ->
        sendConsent(createConsent(solution))
      }
    }
  }

  private fun setupAdapter() {
    recyclerConsents.adapter = consentItemAdapter
  }

  private fun fetchConsentSolution() {
    setupData(null)
    consentSdk.fetchConsentSolution(
      listener = object : CallListener<ConsentSolution> {
        override fun onSuccess(result: ConsentSolution) {
          textError.text = ""
          setupData(result)
        }

        override fun onFailure(error: IOException) {
          consentItemAdapter.submitList(emptyList())
          textError.text = error.toString() // TODO provide default text to prevent literal "null"
        }
      }
    )
  }

  private fun sendConsent(consent: Consent) {
    setupData(null)
    consentSdk.postConsent(
      consent = consent,
      listener = object : CallListener<Unit> {
        override fun onSuccess(result: Unit) {
          Snackbar.make(buttonSend, "Consents sent successfully", Snackbar.LENGTH_SHORT).show()
        }

        override fun onFailure(error: IOException) {
          textError.text = error.toString() // TODO provide default text to prevent literal "null"
        }
      }
    )
  }

  private fun setupData(fetchedConsent: ConsentSolution?) {
    consentSolution = fetchedConsent
    consentItemChoices.clear()
    fetchedConsent?.consentItems?.forEach {
      consentItemChoices[it.consentItemId] = it.type == ConsentItem.Type.Info
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

  companion object {

    @JvmStatic
    fun newInstance() = FetchSendFragment()
  }
}
