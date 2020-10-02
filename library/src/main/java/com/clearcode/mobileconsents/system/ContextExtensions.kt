package com.clearcode.mobileconsents.system

import android.content.Context
import android.os.Build.VERSION
import com.clearcode.mobileconsents.ApplicationProperties

internal fun Context.getApplicationProperties() = ApplicationProperties(
  osVersion = VERSION.RELEASE,
  packageName = packageName,
  appName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0)).toString()
)
