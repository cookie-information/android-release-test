package com.example.sample2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class  MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    findViewById<Button>(R.id.btn).setOnClickListener {
      (applicationContext as App).sdk.displayConsents(this){

      }
    }
  }
}