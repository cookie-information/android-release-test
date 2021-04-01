package com.cookieinformation.mobileconsents.ui

import androidx.lifecycle.ViewModel

/**
 * The view model class for the [BasePrivacyCenterFragment].
 */
internal class PrivacyCenterViewModel(binder: ConsentSolutionBinder) :
  ConsentSolutionViewModel<PrivacyCenterView, PrivacyCenterPresenter>(PrivacyCenterPresenter(), binder) {

  class Factory(private val binder: ConsentSolutionBinder) : androidx.lifecycle.ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
      @Suppress("UNCHECKED_CAST")
      return PrivacyCenterViewModel(binder) as T
    }
  }
}
