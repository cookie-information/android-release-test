package com.cookieinformation.mobileconsents.sample

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_main.buttonFetchSend
import kotlinx.android.synthetic.main.fragment_main.buttonPrivacyCenter
import kotlinx.android.synthetic.main.fragment_main.buttonPrivacyPreferences
import kotlinx.android.synthetic.main.fragment_main.buttonStorage
import kotlinx.android.synthetic.main.fragment_main.buttonUseSampleId
import kotlinx.android.synthetic.main.fragment_main.layoutUuid
import kotlinx.android.synthetic.main.fragment_main.textUuid
import java.util.UUID

class MainFragment : Fragment(R.layout.fragment_main) {

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupForm()
    setupActionButtons()
  }

  private fun setupForm() {
    textUuid.addTextChangedListener { text: CharSequence? ->
      val local = text.isNullOrBlank()
      layoutUuid.hint = if (local) getString(R.string.text_consent_id) else "Using UUID from this text field"
    }
    buttonUseSampleId.setOnClickListener {
      textUuid.setText(getString(R.string.sample_uuid))
    }
  }

  private fun setupActionButtons() {
    buttonFetchSend.setOnClickListener {
      showFetchSend()
    }
    buttonStorage.setOnClickListener {
      requireActivity().showFragment(StorageFragment())
    }
    buttonPrivacyPreferences.setOnClickListener {
      showPrivacyPreferences()
    }
    buttonPrivacyCenter.setOnClickListener {
      showPrivacyCenter()
    }
  }

  private fun showFetchSend() {
    requireActivity().showFragment(FetchSendFragment.newInstance())
  }

  private fun showPrivacyPreferences() {
    val privacyPreferences = PrivacyPreferencesFragment.newInstance()
    privacyPreferences.show(childFragmentManager, PrivacyPreferencesFragment::javaClass.name)
  }

  private fun showPrivacyCenter() {
    requireActivity().showFragment(PrivacyCenterFragment.newInstance())
  }
}
