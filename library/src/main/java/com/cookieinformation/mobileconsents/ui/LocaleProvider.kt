package com.cookieinformation.mobileconsents.ui

import java.util.Locale

/**
 * Interface that provides a list of [Locale]s, which are used to select the correct translation of the consent text.
 */
public interface LocaleProvider {

  /**
   * Returns list of [Locale]s, which are used to select the correct translation of the consent text.
   * Must return at least one locale.
   */
  public fun getLocales(): List<Locale>
}
