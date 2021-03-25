package com.cookieinformation.mobileconsents.ui

import androidx.lifecycle.ViewModel
import com.cookieinformation.mobileconsents.MobileConsentSdk
import java.util.UUID

internal class PrivacyCenterViewModel(
  mobileConsentSdk: MobileConsentSdk,
  consentSolutionId: UUID,
  localeProvider: LocaleProvider
) : ConsentSolutionViewModel<PrivacyCenterView, PrivacyCenterPresenter>(
  PrivacyCenterPresenter(),
  mobileConsentSdk,
  consentSolutionId,
  localeProvider
) {

  class Factory(
    private val mobileConsentSdk: MobileConsentSdk,
    private val consentSolutionId: UUID,
    private val localeProvider: LocaleProvider
  ) : androidx.lifecycle.ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
      @Suppress("UNCHECKED_CAST")
      return PrivacyCenterViewModel(mobileConsentSdk, consentSolutionId, localeProvider) as T
    }
  }
}
