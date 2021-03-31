package com.cookieinformation.mobileconsents.ui

import com.cookieinformation.mobileconsents.ConsentSolution
import java.util.UUID

public interface ConsentSolutionListener {

  public fun onConsentsChosen(consentSolution: ConsentSolution, consents: Map<UUID, Boolean>, external: Boolean)

  public fun onDismissed()

  public fun onReadMore()
}
