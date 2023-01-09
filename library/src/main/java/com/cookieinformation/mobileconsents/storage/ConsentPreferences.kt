package com.cookieinformation.mobileconsents.storage

import android.content.Context
import android.content.SharedPreferences
import com.cookieinformation.mobileconsents.ConsentItem.Type

internal class ConsentPreferences(private val applicationContext: Context) {

  internal fun sharedPreferences(): SharedPreferences {
    return applicationContext.getSharedPreferences(CONSENT_PREFERENCES, Context.MODE_PRIVATE)
  }

  fun getAllConsentChoices(): Map<Type, Boolean>{
    return sharedPreferences().all.mapKeys {
      Type.findTypeByValue(it.key)
    }.mapValues { it.value as? Boolean ?: false }
  }

  companion object {
    private const val CONSENT_PREFERENCES = "consent_preference"
  }
}