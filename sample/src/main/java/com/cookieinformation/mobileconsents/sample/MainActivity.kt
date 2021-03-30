package com.cookieinformation.mobileconsents.sample

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

class MainActivity : AppCompatActivity(R.layout.activity_main)

fun FragmentActivity.showFragment(fragment: Fragment) {
  supportFragmentManager.beginTransaction()
    .replace(R.id.fragmentContainer, fragment, fragment::class.java.name)
    .addToBackStack(null)
    .commit()
}
