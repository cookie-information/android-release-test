package com.cookieinformation.mobileconsents.util

import android.text.Spannable
import android.text.TextPaint
import android.text.style.URLSpan
import android.view.View

internal fun Spannable.changeLinksStyle(
  underline: Boolean = false,
  bold: Boolean = true,
  doOnClick: (() -> Unit)? = null
): Boolean {
  val spans = getSpans(0, length, URLSpan::class.java)
  spans.forEach {
    setSpan(
      object : URLSpan(it.url) {

        override fun updateDrawState(ds: TextPaint) {
          super.updateDrawState(ds)
          ds.isUnderlineText = underline
          ds.isFakeBoldText = bold
        }

        override fun onClick(widget: View) {
          super.onClick(widget)
          doOnClick?.invoke()
        }
      },
      getSpanStart(it),
      getSpanEnd(it),
      0
    )
    removeSpan(it)
  }
  return spans.isNotEmpty()
}
