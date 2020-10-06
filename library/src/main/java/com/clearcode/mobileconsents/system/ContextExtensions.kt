package com.clearcode.mobileconsents.system

import android.content.Context
import android.os.Build.VERSION
import com.clearcode.mobileconsents.ApplicationProperties

internal fun Context.getApplicationProperties() = ApplicationProperties(
  operatingSystem = "Android ${VERSION.RELEASE}",
  applicationId = packageName,
  applicationName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0)).toString()
)
