package com.cookieinformation.mobileconsents.sample

import android.os.Bundle
import com.cookieinformation.mobileconsents.ui.DefaultLocaleProvider
import com.cookieinformation.mobileconsents.ui.PrivacyPreferencesDialogFragment
import java.util.UUID

private const val mobileConsentSdkSolutionIdKey = "mobileConsentSdkSolutionIdKey"

class PrivacyPreferencesFragment : PrivacyPreferencesDialogFragment() {

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
    // Not yet implemented
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
