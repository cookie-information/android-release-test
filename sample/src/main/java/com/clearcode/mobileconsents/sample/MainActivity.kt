package com.clearcode.mobileconsents.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.clearcode.mobileconsents.Dummy

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    Dummy.initialize()
  }
}
