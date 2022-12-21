package com.cookieinformation.mobileconsents.interfaces

import com.cookieinformation.mobileconsents.MobileConsents
import com.cookieinformation.mobileconsents.models.MobileConsentCredentials
import com.cookieinformation.mobileconsents.models.MobileConsentCustomUI

/**
 * Fluent Builder [MobileConsentSdkBuilder] interface.
 */
public interface MobileConsentsFactory {
  public fun setClientCredentials(credentials: MobileConsentCredentials): MobileConsentsFactory
  public fun setMobileConsentCustomUI(customUI: MobileConsentCustomUI): MobileConsentsFactory
  public fun build(): MobileConsents
}