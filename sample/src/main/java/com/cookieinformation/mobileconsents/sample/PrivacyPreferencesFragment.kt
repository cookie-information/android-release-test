package com.cookieinformation.mobileconsents.sample

import com.cookieinformation.mobileconsents.ConsentSolution
import com.cookieinformation.mobileconsents.ui.BasePrivacyPreferencesDialogFragment
import com.cookieinformation.mobileconsents.ui.ConsentSolutionBinder
import java.util.UUID

class PrivacyPreferencesFragment : BasePrivacyPreferencesDialogFragment() {

  override fun bindConsentSolution(builder: ConsentSolutionBinder.Builder): ConsentSolutionBinder {
    val app = requireContext().applicationContext as App
    val mobileConsentSdk = app.sdk

    return builder
      .setMobileConsentSdk(mobileConsentSdk.getMobileConsentSdk())
      .create()
  }

  override fun onConsentsChosen(consentSolution: ConsentSolution, consents: Map<UUID, Boolean>, external: Boolean) {
    dismiss()
  }

  override fun onReadMore() {
    requireActivity().showFragment(PrivacyCenterFragment.newInstance())
  }

  override fun onDismissed() {
    dismiss()
  }

  companion object {

    @JvmStatic
    fun newInstance() = PrivacyPreferencesFragment()
  }
}
