package com.cookieinformation.mobileconsents.sample

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_main.buttonFetchSend
import kotlinx.android.synthetic.main.fragment_main.buttonPrivacyCenter
import kotlinx.android.synthetic.main.fragment_main.buttonPrivacyFragment
import kotlinx.android.synthetic.main.fragment_main.buttonPrivacyPreferences
import kotlinx.android.synthetic.main.fragment_main.buttonStorage

class MainFragment : Fragment(R.layout.fragment_main) {

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupActionButtons()
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
    buttonPrivacyFragment.setOnClickListener {
      requireActivity().showFragment(PrivacyFragment.newInstance())
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
