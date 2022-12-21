package com.cookieinformation.mobileconsents.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.commit
import com.cookieinformation.mobileconsents.R

internal class PrivacyActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_privacy)

    supportFragmentManager.commit {
      replace(R.id.fragment_container, PrivacyFragment.newInstance())
    }
  }
}