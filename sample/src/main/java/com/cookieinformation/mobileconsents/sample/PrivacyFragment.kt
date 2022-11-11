package com.cookieinformation.mobileconsents.sample

import com.cookieinformation.mobileconsents.ConsentSolution
import com.cookieinformation.mobileconsents.ui.BasePrivacyFragment
import com.cookieinformation.mobileconsents.ui.ConsentSolutionBinder
import java.util.UUID

class PrivacyFragment : BasePrivacyFragment() {

  override fun bindConsentSolution(builder: ConsentSolutionBinder.Builder): ConsentSolutionBinder {
    val app = requireContext().applicationContext as App
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

  override fun onReadMore() {
    // Should be NOT called for Privacy Center
  }

  companion object {

    @JvmStatic
    fun newInstance() = PrivacyFragment()
  }
}
