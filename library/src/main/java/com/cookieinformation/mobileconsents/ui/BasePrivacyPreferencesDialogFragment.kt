package com.cookieinformation.mobileconsents.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.cookieinformation.mobileconsents.R
import com.cookieinformation.mobileconsents.ui.ConsentSolutionViewModel.Event
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

public abstract class BasePrivacyPreferencesDialogFragment : DialogFragment(), ConsentSolutionListener {

  private lateinit var binder: ConsentSolutionBinder

  private val viewModel: PrivacyPreferencesViewModel by viewModels { createViewModelFactory(binder) }

  protected abstract fun bindConsentSolution(builder: ConsentSolutionBinder.Builder): ConsentSolutionBinder

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binder = bindConsentSolution(ConsentSolutionBinder.InternalBuilder(requireContext()))
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
    inflater.inflate(R.layout.mobileconsents_privacy_preferences_dialog_fragment, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.events
      .onEach(::handleEvent)
      .launchIn(viewLifecycleOwner.lifecycleScope)
    viewModel.attachView(requireView().findViewById(R.id.mobileconsents_privacy_preferences_view))
  }

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
