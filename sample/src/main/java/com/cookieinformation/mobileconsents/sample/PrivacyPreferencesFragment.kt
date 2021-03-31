package com.cookieinformation.mobileconsents.sample

import android.os.Bundle
import com.cookieinformation.mobileconsents.ConsentSolution
import com.cookieinformation.mobileconsents.ui.BasePrivacyPreferencesDialogFragment
import com.cookieinformation.mobileconsents.ui.ConsentSolutionBinder
import java.util.UUID

private const val mobileConsentSdkSolutionIdKey = "mobileConsentSdkSolutionIdKey"

class PrivacyPreferencesFragment : BasePrivacyPreferencesDialogFragment() {

  private val consentSolutionId: UUID
    get() = UUID.fromString(requireArguments().getString(mobileConsentSdkSolutionIdKey))

  override fun bindConsentSolution(builder: ConsentSolutionBinder.Builder): ConsentSolutionBinder {
    val app = requireContext().applicationContext as App
    val mobileConsentSdk = app.sdk

    return builder
      .setMobileConsentSdk(mobileConsentSdk.getMobileConsentSdk())
      .setConsentSolutionId(consentSolutionId)
      .create()
  }

  override fun onConsentsChosen(consentSolution: ConsentSolution, consents: Map<UUID, Boolean>, external: Boolean) {
    dismiss()
  }

  override fun onReadMore() {
    requireActivity().showFragment(PrivacyCenterFragment.newInstance(consentSolutionId))
  }

  override fun onDismissed() {
    dismiss()
  }

  companion object {

    @JvmStatic
    fun newInstance(consentsId: UUID) = PrivacyPreferencesFragment().apply {
      arguments = Bundle().apply {
        putString(mobileConsentSdkSolutionIdKey, consentsId.toString())
      }
    }
  }
}
