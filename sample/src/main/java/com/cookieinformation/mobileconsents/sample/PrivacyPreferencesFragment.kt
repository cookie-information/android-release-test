package com.cookieinformation.mobileconsents.sample

import android.os.Bundle
import com.cookieinformation.mobileconsents.ui.BasePrivacyPreferencesDialogFragment
import com.cookieinformation.mobileconsents.ui.ConsentSolutionBinder
import java.util.UUID

private const val mobileConsentSdkSolutionIdKey = "mobileConsentSdkSolutionIdKey"

class PrivacyPreferencesFragment : BasePrivacyPreferencesDialogFragment() {

  override fun bindConsentSolution(builder: ConsentSolutionBinder.Builder): ConsentSolutionBinder {
    val app = requireContext().applicationContext as App
    val mobileConsentSdk = app.sdk
    val consentSolutionId = UUID.fromString(requireArguments().getString(mobileConsentSdkSolutionIdKey))

    return builder
      .setMobileConsentSdk(mobileConsentSdk.getMobileConsentSdk())
      .setConsentSolutionId(consentSolutionId)
      .create()
  }

  override fun onConsentsChosen(consents: Map<UUID, Boolean>) {
    // Handle user choice
    dismiss()
  }

  override fun onReadMore() {
    // Not yet implemented
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
