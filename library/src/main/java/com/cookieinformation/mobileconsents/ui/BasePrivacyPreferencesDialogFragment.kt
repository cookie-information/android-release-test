package com.cookieinformation.mobileconsents.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.cookieinformation.mobileconsents.R
import com.cookieinformation.mobileconsents.ui.ConsentSolutionViewModel.Event
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * The base fragment for "Privacy Preferences" dialog.
 */
public abstract class BasePrivacyPreferencesDialogFragment : DialogFragment(), ConsentSolutionListener {

  private val viewModel: PrivacyPreferencesViewModel by viewModels {
    createViewModelFactory(bindConsentSolution(ConsentSolutionBinder.InternalBuilder(requireContext())))
  }

  /**
   * The developer must implement this method to bind the fragment with the instance of
   * [com.cookieinformation.mobileconsents.MobileConsentSdk] and [UUID] of the consent solution.
   *
   * The developer should provide the same instance of
   * [com.cookieinformation.mobileconsents.MobileConsentSdk] after configuration has changed.
   *
   * @param builder fluent builder that allows the developer to set up an instance of
   * [com.cookieinformation.mobileconsents.MobileConsentSdk], [UUID] of the consent solution and
   * optionally [LocaleProvider].
   */
  protected abstract fun bindConsentSolution(builder: ConsentSolutionBinder.Builder): ConsentSolutionBinder

  /**
   * If method is overridden, the super must be called.
   */
  @CallSuper
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
    inflater.inflate(R.layout.mobileconsents_privacy_preferences_dialog_fragment, container, false)

  /**
   * If method is overridden, the super must be called.
   */
  @CallSuper
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.events
      .onEach(::handleEvent)
      .launchIn(viewLifecycleOwner.lifecycleScope)
    viewModel.attachView(requireView().findViewById(R.id.mobileconsents_privacy_preferences_view))
  }

  /**
   * If method is overridden, the super must be called.
   */
  @CallSuper
  override fun onDestroyView() {
    viewModel.detachView()
    super.onDestroyView()
  }

  private fun handleEvent(event: Event) =
    when (event) {
      is Event.ConsentsChosen -> with(event) { onConsentsChosen(consentSolution, consents, external) }
      Event.ReadMore -> onReadMore()
      Event.Dismiss -> onDismissed()
    }

  private fun createViewModelFactory(binder: ConsentSolutionBinder) = PrivacyPreferencesViewModel.Factory(binder)
}
