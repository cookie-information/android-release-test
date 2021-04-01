package com.cookieinformation.mobileconsents.ui

import androidx.lifecycle.ViewModel

/**
 * The view model class for the [BasePrivacyPreferencesDialogFragment].
 */
internal class PrivacyPreferencesViewModel(binder: ConsentSolutionBinder) :
  ConsentSolutionViewModel<PrivacyPreferencesView, PrivacyPreferencesPresenter>(PrivacyPreferencesPresenter(), binder) {

  class Factory(private val binder: ConsentSolutionBinder) : androidx.lifecycle.ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
      @Suppress("UNCHECKED_CAST")
      return PrivacyPreferencesViewModel(binder) as T
    }
  }
}
