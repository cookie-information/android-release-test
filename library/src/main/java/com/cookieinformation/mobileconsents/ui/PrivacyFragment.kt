package com.cookieinformation.mobileconsents.ui

import com.cookieinformation.mobileconsents.ConsentSolution
import com.cookieinformation.mobileconsents.Consentable
import java.util.UUID

internal class PrivacyFragment : BasePrivacyFragment() {

  override fun bindConsentSolution(builder: ConsentSolutionBinder.Builder): ConsentSolutionBinder {
    val app = requireContext().applicationContext as Consentable
    val mobileConsentSdk = app.sdk

    return builder
      .setMobileConsentSdk(mobileConsentSdk.getMobileConsentSdk())
      .create()
  }

  override fun onConsentsChosen(consentSolution: ConsentSolution, consents: Map<UUID, Boolean>, external: Boolean) {
    requireActivity().onBackPressed()
  }

  override fun onDismissed() {
    requireActivity().onBackPressed()
  }

  companion object {

    @JvmStatic
    fun newInstance() = PrivacyFragment()
  }
}
