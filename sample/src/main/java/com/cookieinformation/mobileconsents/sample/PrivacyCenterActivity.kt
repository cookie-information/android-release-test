package com.cookieinformation.mobileconsents.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.util.UUID

class PrivacyCenterActivity : AppCompatActivity(R.layout.activity_privacy_center) {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (savedInstanceState == null) {
      supportFragmentManager.beginTransaction()
        .add(
          R.id.fragment_container,
          PrivacyCenterFragment.newInstance(intent.extras!![ConsentSolutionIdExtra] as UUID),
          null
        )
        .commit()
    }
  }

  companion object {
    const val ConsentSolutionIdExtra = "ConsentSolutionIdExtra"
  }
}
