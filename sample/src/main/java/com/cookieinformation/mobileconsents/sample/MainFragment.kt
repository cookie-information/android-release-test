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
      val valid = !text.isNullOrBlank()
      layoutUuid.error = if (valid) null else "UUID cannot be empty"
      buttonFetchSend.isEnabled = valid
      buttonPrivacyPreferences.isEnabled = valid
      buttonPrivacyCenter.isEnabled = valid
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
    try {
      val uuid = UUID.fromString(textUuid.text.toString())
      requireActivity().showFragment(FetchSendFragment.newInstance(uuid))
    } catch (e: IllegalArgumentException) {
      Snackbar.make(buttonFetchSend, e.message.toString(), Snackbar.LENGTH_SHORT).show()
    }
  }

  private fun showPrivacyPreferences() {
    try {
      val uuid = UUID.fromString(textUuid.text.toString())
      val privacyPreferences = PrivacyPreferencesFragment.newInstance(uuid)
      privacyPreferences.show(childFragmentManager, PrivacyPreferencesFragment::javaClass.name)
    } catch (e: IllegalArgumentException) {
      Snackbar.make(buttonPrivacyPreferences, e.message.toString(), Snackbar.LENGTH_SHORT).show()
    }
  }

  private fun showPrivacyCenter() {
    try {
      val uuid = UUID.fromString(textUuid.text.toString())
      requireActivity().showFragment(PrivacyCenterFragment.newInstance(uuid))
    } catch (e: IllegalArgumentException) {
      Snackbar.make(buttonPrivacyCenter, e.message.toString(), Snackbar.LENGTH_SHORT).show()
    }
  }
}
