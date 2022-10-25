package com.cookieinformation.mobileconsents.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.cookieinformation.mobileconsents.networking.response.TokenResponse

internal class Preferences(private val applicationContext: Context) {

  /**
   * Set [TokenResponse] in [SharedPreferences].
   */
  fun setTokenResponse(tokenResponse: TokenResponse) {
    sharedPreferences().edit(commit = true) {
      tokenResponse.let {
        putString(KEY__ACCESS_TOKEN, it.accessToken)
        putLong(KEY__ACCESS_TOKEN_EXPIRES_IN, System.currentTimeMillis() + (it.expiresIn * 1000))
      }
    }
  }

  fun getAccessToken(): String? {
    val now = System.currentTimeMillis()
    return when {
      !sharedPreferences().contains(KEY__ACCESS_TOKEN) -> null
      sharedPreferences().getLong(KEY__ACCESS_TOKEN_EXPIRES_IN, 0L) < now -> null
      else -> sharedPreferences().getString(KEY__ACCESS_TOKEN, "")
    }
  }

  private fun sharedPreferences(): SharedPreferences {
    return applicationContext.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
  }

  companion object {
    private const val SHARED_PREFERENCES_NAME = "mobile_consents"
    private const val KEY__ACCESS_TOKEN = "keyAccessToken"
    private const val KEY__ACCESS_TOKEN_EXPIRES_IN = "keyAccessTokenExpiresIn"
  }
}