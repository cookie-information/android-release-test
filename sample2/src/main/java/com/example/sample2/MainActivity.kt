package com.example.sample2

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.cookieinformation.mobileconsents.GetConsents

class MainActivity : AppCompatActivity() {

  private val listener = registerForActivityResult(
    GetConsents(this),
  ) {

  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    findViewById<Button>(R.id.display_always).setOnClickListener {
      (applicationContext as App).sdk.displayConsents(listener)
    }

    findViewById<Button>(R.id.display_if_needed).setOnClickListener {
      (applicationContext as App).sdk.displayConsentsIfNeeded(listener)
    }
  }
}