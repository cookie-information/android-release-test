package com.cookieinformation.mobileconsents.ui

import java.util.UUID

public interface PrivacyPreferencesListener {

  public fun onConsentsChosen(consents: Map<UUID, Boolean>)

  public fun onReadMore()

  public fun onDismissed()
}
