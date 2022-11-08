package com.cookieinformation.mobileconsents.util

import com.cookieinformation.mobileconsents.BuildConfig
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private const val isoPattern = "yyyy-MM-dd'T'HH:mm:ss'Z'"
private const val utcTimeZoneId = "UTC"
private const val delayInTime = 5000L

internal fun getUtcDate(): String {
  val format = SimpleDateFormat(isoPattern, Locale.getDefault()).apply {
    timeZone = TimeZone.getTimeZone(utcTimeZoneId)
  }
  val date = System.currentTimeMillis() - if (BuildConfig.DEBUG) delayInTime else 0L
  // TODO: remove delay when fixed on backend

  return format.format(date)
}
