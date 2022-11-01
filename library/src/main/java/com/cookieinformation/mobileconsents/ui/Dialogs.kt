package com.cookieinformation.mobileconsents.ui

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.cookieinformation.mobileconsents.R

/**
 * Creates a dialog which is shown when fetching fails.
 */
internal fun createRetryDialog(context: Context, onRetry: () -> Unit, onDismiss: () -> Unit, title: String, message: String) =
  AlertDialog.Builder(context)
    .setCancelable(false)
    .setTitle(title)
    .setMessage(message)
    .setPositiveButton(R.string.mobileconsents_privacy_preferences_btn_retry) { _, _ -> onRetry() }
    .setNegativeButton(R.string.mobileconsents_privacy_preferences_btn_cancel) { _, _ -> onDismiss() }
    .create()

/**
 * Creates a dialog which is shown when sending user choice fails.
 */
internal fun createErrorDialog(context: Context, onDismiss: () -> Unit) =
  AlertDialog.Builder(context)
    .setCancelable(false)
    .setTitle(R.string.mobileconsents_privacy_preferences_title_error_send)
    .setMessage(R.string.mobileconsents_privacy_preferences_msg_error_send)
    .setPositiveButton(android.R.string.ok) { _, _ -> onDismiss() }
    .create()
