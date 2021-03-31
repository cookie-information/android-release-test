package com.cookieinformation.mobileconsents.sample

import android.os.Bundle
import com.cookieinformation.mobileconsents.ConsentSolution
import com.cookieinformation.mobileconsents.ui.BasePrivacyCenterFragment
import com.cookieinformation.mobileconsents.ui.ConsentSolutionBinder
import java.util.UUID

private const val mobileConsentSdkSolutionIdKey = "mobileConsentSdkSolutionIdKey"

class PrivacyCenterFragment : BasePrivacyCenterFragment() {

  override fun bindConsentSolution(builder: ConsentSolutionBinder.Builder): ConsentSolutionBinder {
    val app = requireContext().applicationContext as App
    val mobileConsentSdk = app.sdk
    val consentSolutionId = UUID.fromString(requireArguments().getString(mobileConsentSdkSolutionIdKey))

    return builder
      .setMobileConsentSdk(mobileConsentSdk.getMobileConsentSdk())
      .setConsentSolutionId(consentSolutionId)
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
    fun newInstance(consentsId: UUID) = PrivacyCenterFragment().apply {
      arguments = Bundle().apply {
        putString(mobileConsentSdkSolutionIdKey, consentsId.toString())
      }
    }
  }
}
