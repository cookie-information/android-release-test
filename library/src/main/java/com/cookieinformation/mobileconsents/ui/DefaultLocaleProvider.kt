package com.cookieinformation.mobileconsents.ui

import android.content.Context
import android.os.Build
import java.util.Locale

/**
 * Default implementation of the [LocaleProvider].
 */
public class DefaultLocaleProvider(private val applicationContext: Context) : LocaleProvider {

  /**
   * Returns the list of [Locale]s provided by system.
   */
  public override fun getLocales(): List<Locale> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      mutableListOf<Locale>().apply {
        applicationContext.resources.configuration.locales.also {
          for (i in 0 until it.size()) add(it[i])
        }
      }
    } else {
      @Suppress("DEPRECATION")
      listOf(applicationContext.resources.configuration.locale)
    }
  }
}
