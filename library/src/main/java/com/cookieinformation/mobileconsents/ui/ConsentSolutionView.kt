package com.cookieinformation.mobileconsents.ui

import androidx.annotation.MainThread

/**
 * The interfere of [ConsentSolutionPresenter]'s view.
 */
@MainThread
public interface ConsentSolutionView<ViewDataType, ViewIntentListenerType> {

  /**
   * Shows a progress bar and should prevent the user from doing any action (except "back" navigation).
   */
  public fun showProgressBar()

  /**
   * Hides the progress bar.
   */
  public fun hideProgressBar()

  /**
   * Shows the view data.
   *
   * @param data the data to show
   */
  public fun showViewData(data: ViewDataType)

  /**
   * Shows retry dialog.
   *
   * @param onRetry should be invoked if user wants to retry.
   * @param onDismiss should be invoked when user wants to cancel.
   * @param title show the title of the dialog.
   * @param message show the message of the dialog.
   */
  public fun showRetryDialog(onRetry: () -> Unit, onDismiss: () -> Unit, title: String = "", message: String = "")

  /**
   * Shows error dialog.
   *
   * @param onDismiss should be invoked when user closes the dialog.
   */
  public fun showErrorDialog(onDismiss: () -> Unit)

  /**
   * Adds the view's intent listener.
   *
   * @param listener the view intent listener.
   */
  public fun addIntentListener(listener: ViewIntentListenerType)

  /**
   * Removes the view's intent listener.
   *
   * @param listener previously added instance of view intent listener.
   */
  public fun removeIntentListener(listener: ViewIntentListenerType)
}
