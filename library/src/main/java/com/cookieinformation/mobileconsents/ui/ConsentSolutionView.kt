package com.cookieinformation.mobileconsents.ui

import androidx.annotation.MainThread

@MainThread
public interface ConsentSolutionView<ViewDataType, ViewIntentListenerType> {

  public fun showProgressBar()

  public fun hideProgressBar()

  public fun hideViewData()

  public fun showViewData(data: ViewDataType)

  public fun showRetryDialog(onRetry: () -> Unit, onDismiss: () -> Unit)

  public fun showErrorDialog(onDismiss: () -> Unit)

  public fun addIntentListener(listener: ViewIntentListenerType)

  public fun removeIntentListener(listener: ViewIntentListenerType)
}
