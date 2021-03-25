package com.cookieinformation.mobileconsents.sample

import android.os.Bundle
import com.cookieinformation.mobileconsents.ui.BasePrivacyCenterFragment
import com.cookieinformation.mobileconsents.ui.DefaultLocaleProvider
import java.util.UUID

private const val mobileConsentSdkSolutionIdKey = "mobileConsentSdkSolutionIdKey"

class PrivacyCenterFragment : BasePrivacyCenterFragment() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val app = requireContext().applicationContext as App
    val mobileConsentSdk = app.sdk
    val consentsId = UUID.fromString(requireArguments().getString(mobileConsentSdkSolutionIdKey))
    initialize(
      mobileConsentSdk = mobileConsentSdk.getMobileConsentSdk(),
      consentSolutionId = consentsId,
      localeProvider = DefaultLocaleProvider(app)
    )
  }

  override fun onConsentsChosen(consents: Map<UUID, Boolean>) {
    requireActivity().onBackPressed()
  }

  override fun onDismissed() {
    requireActivity().onBackPressed()
  }

  override fun onReadMore() {
    // Should be called for Privacy Center
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
