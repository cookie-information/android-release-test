package com.cookieinformation.mobileconsents

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import com.cookieinformation.mobileconsents.ConsentItem.Type
import com.cookieinformation.mobileconsents.ui.PrivacyActivity
import kotlinx.coroutines.CoroutineScope

public class GetConsents(public val context: Context, public val scope: CoroutineScope) :
  ActivityResultContract<Bundle, Map<Type, Boolean>>() {
  public override fun createIntent(context: Context, bundle: Bundle): Intent =
    Intent(context, PrivacyActivity::class.java)

  override fun parseResult(resultCode: Int, result: Intent?): Map<Type, Boolean> {
    val items = context.applicationContext.getSharedPreferences(
      "consent_preference",
      Context.MODE_PRIVATE
    ).all.filter { it.value is Boolean }.mapValues { it.value as Boolean }.mapKeys {
      Type.findTypeByValue(it.key)
    }
    return items
  }
}