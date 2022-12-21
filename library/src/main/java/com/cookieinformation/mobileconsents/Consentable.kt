package com.cookieinformation.mobileconsents

import com.cookieinformation.mobileconsents.models.MobileConsentCredentials

public interface Consentable {
  public val sdk: MobileConsents
  public fun provideCredentials(): MobileConsentCredentials
}