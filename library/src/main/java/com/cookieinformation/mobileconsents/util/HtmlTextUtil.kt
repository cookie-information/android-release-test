package com.cookieinformation.mobileconsents.util

import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.core.text.util.LinkifyCompat

internal fun TextView.setTextFomHtml(html: String, boldLinks: Boolean = true) {
  val stringBuilder = SpannableStringBuilder(HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT)).apply {
    changeLinksStyle(bold = boldLinks)
  }
  LinkifyCompat.addLinks(this, Linkify.WEB_URLS)
  text = stringBuilder
  movementMethod = LinkMovementMethod.getInstance()
}
