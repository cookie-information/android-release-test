package com.cookieinformation.mobileconsents.ui

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.cookieinformation.mobileconsents.R

internal fun createRetryDialog(context: Context, onRetry: () -> Unit, onDismiss: () -> Unit) =
  AlertDialog.Builder(context)
    .setCancelable(false)
    .setTitle(R.string.mobileconsents_privacy_preferences_title_error_fetch)
    .setMessage(R.string.mobileconsents_privacy_preferences_msg_error_fetch)
    .setPositiveButton(R.string.mobileconsents_privacy_preferences_btn_retry) { _, _ -> onRetry() }
    .setNegativeButton(android.R.string.cancel) { _, _ -> onDismiss() }
    .create()

internal fun createErrorDialog(context: Context, onDismiss: () -> Unit) =
  AlertDialog.Builder(context)
    .setCancelable(false)
    .setTitle(R.string.mobileconsents_privacy_preferences_title_error_send)
    .setMessage(R.string.mobileconsents_privacy_preferences_msg_error_send)
    .setPositiveButton(android.R.string.ok) { _, _ -> onDismiss() }
    .create()
