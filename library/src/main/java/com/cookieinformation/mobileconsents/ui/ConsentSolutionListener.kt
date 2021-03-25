package com.cookieinformation.mobileconsents.ui

import java.util.UUID

public interface ConsentSolutionListener {

  public fun onConsentsChosen(consents: Map<UUID, Boolean>)

  public fun onDismissed()

  public fun onReadMore()
}
