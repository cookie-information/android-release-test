package com.cookieinformation.mobileconsents.util

import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import androidx.core.text.HtmlCompat
import com.cookieinformation.mobileconsents.R

internal fun TextView.setTextFromHtml(html: String, boldLinks: Boolean = true, underline: Boolean = false) {
  val stringBuilder = SpannableStringBuilder(HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT)).apply {
    changeLinksStyle(underline = underline, bold = boldLinks) {
      setTag(R.id.mobileconsents_span_clicked_tag, true)
    }
  }
  text = stringBuilder
  movementMethod = LinkMovementMethod.getInstance()
}

internal fun TextView.setOnClickListenerButDoNotInvokeWhenSpanClicked(listener: (View) -> Unit) {
  setOnClickListener { v ->
    if (getTag(R.id.mobileconsents_span_clicked_tag) != true) {
      listener(v)
    }
    setTag(R.id.mobileconsents_span_clicked_tag, false)
  }
}
