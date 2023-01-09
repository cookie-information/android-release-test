package com.cookieinformation.mobileconsents.interfaces

import com.cookieinformation.mobileconsents.MobileConsentSdk
import com.cookieinformation.mobileconsents.models.MobileConsentCredentials
import com.cookieinformation.mobileconsents.models.MobileConsentCustomUI

/**
 * Fluent Builder [MobileConsentSdkBuilder] interface.
 */
public interface CallFactory {
  public fun setClientCredentials(credentials: MobileConsentCredentials): CallFactory
  public fun setMobileConsentCustomUI(customUI: MobileConsentCustomUI): CallFactory
  public fun build(): MobileConsentSdk
}