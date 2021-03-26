package com.cookieinformation.mobileconsents.ui

import android.content.Context
import com.cookieinformation.mobileconsents.MobileConsentSdk
import java.util.UUID

public class ConsentSolutionBinder internal constructor(
  public val mobileConsentSdk: MobileConsentSdk,
  public val consentSolutionId: UUID,
  public val localeProvider: LocaleProvider
) {

  public interface Builder {
    public fun setMobileConsentSdk(mobileConsentSdk: MobileConsentSdk): BuilderSetConsentSolutionId
  }

  public interface BuilderSetConsentSolutionId {
    public fun setConsentSolutionId(consentSolutionId: UUID): BuilderLocaleProvider
  }

  public interface BuilderLocaleProvider : BuilderCreate {
    public fun setLocaleProvider(localeProvider: LocaleProvider): BuilderCreate
  }

  public interface BuilderCreate {
    public fun create(): ConsentSolutionBinder
  }

  internal class InternalBuilder(context: Context) : Builder, BuilderSetConsentSolutionId, BuilderLocaleProvider {

    private val applicationContext = context.applicationContext
    private lateinit var mobileConsentSdk: MobileConsentSdk
    private lateinit var consentSolutionId: UUID
    private var localeProvider: LocaleProvider? = null

    override fun setMobileConsentSdk(mobileConsentSdk: MobileConsentSdk): BuilderSetConsentSolutionId {
      this.mobileConsentSdk = mobileConsentSdk
      return this
    }

    override fun setConsentSolutionId(consentSolutionId: UUID): BuilderLocaleProvider {
      this.consentSolutionId = consentSolutionId
      return this
    }

    override fun setLocaleProvider(localeProvider: LocaleProvider): BuilderCreate {
      this.localeProvider = localeProvider
      return this
    }

    override fun create(): ConsentSolutionBinder = ConsentSolutionBinder(
      mobileConsentSdk = mobileConsentSdk,
      consentSolutionId = consentSolutionId,
      localeProvider = localeProvider ?: DefaultLocaleProvider(applicationContext)
    )
  }
}
