package com.cookieinformation.mobileconsents.util

import android.text.Spannable
import android.text.TextPaint
import android.text.style.URLSpan

internal fun Spannable.changeLinksStyle(underline: Boolean = false, bold: Boolean = true): Boolean {
  val spans = getSpans(0, length, URLSpan::class.java)
  spans.forEach {
    setSpan(
      object : URLSpan(it.url) {
        override fun updateDrawState(ds: TextPaint) {
          super.updateDrawState(ds)
          ds.isUnderlineText = underline
          ds.isFakeBoldText = bold
        }
      },
      getSpanStart(it),
      getSpanEnd(it),
      0
    )
  }
  return spans.isNotEmpty()
}
