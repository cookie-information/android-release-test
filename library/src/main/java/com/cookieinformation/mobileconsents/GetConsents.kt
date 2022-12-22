package com.cookieinformation.mobileconsents

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import com.cookieinformation.mobileconsents.ConsentItem.Type
import com.cookieinformation.mobileconsents.storage.ConsentPreferences
import com.cookieinformation.mobileconsents.ui.PrivacyActivity

public class GetConsents(private val applicationContext: Context) :
  ActivityResultContract<Bundle?, Map<Type, Boolean>>() {
  public override fun createIntent(context: Context, input: Bundle?): Intent =
    Intent(context, PrivacyActivity::class.java)

  override fun parseResult(resultCode: Int, intent: Intent?): Map<Type, Boolean> {
    return ConsentPreferences(applicationContext).getAllConsentChoices()
  }
}