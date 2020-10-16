package com.cookieinformation.mobileconsents.system

import android.content.Context
import android.os.Build.VERSION

internal fun Context.getApplicationProperties() = ApplicationProperties(
  operatingSystem = "Android ${VERSION.RELEASE}",
  applicationId = packageName,
  applicationName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0)).toString()
)
