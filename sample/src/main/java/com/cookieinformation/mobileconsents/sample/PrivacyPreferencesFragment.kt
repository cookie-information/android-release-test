package com.cookieinformation.mobileconsents.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import java.util.UUID

private const val consentIdKey = "mobileconsents_consents_id"

class PrivacyPreferencesFragment : DialogFragment() {

  private lateinit var consentsId: UUID

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    consentsId = UUID.fromString(requireArguments().getString(consentIdKey))
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
    inflater.inflate(R.layout.fragment_privacy_preferences, container, false)

  companion object {

    @JvmStatic
    fun newInstance(consentsId: UUID) =
      PrivacyPreferencesFragment().apply {
        arguments = Bundle().apply {
          putString(consentIdKey, consentsId.toString())
        }
      }
  }
}
