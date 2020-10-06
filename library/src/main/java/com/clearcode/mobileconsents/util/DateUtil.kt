package com.clearcode.mobileconsents.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

private const val isoPattern = "yyyy-MM-dd'T'HH:mm:ss'Z'"
private const val utcTimeZoneId = "UTC"

internal fun getUtcDate(): String {
  val format = SimpleDateFormat(isoPattern, Locale.getDefault()).apply {
    timeZone = TimeZone.getTimeZone(utcTimeZoneId)
  }
  val date = Calendar.getInstance().time

  return format.format(date)
}
