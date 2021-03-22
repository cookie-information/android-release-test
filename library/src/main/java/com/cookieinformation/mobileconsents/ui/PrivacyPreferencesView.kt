package com.cookieinformation.mobileconsents.ui

import android.app.AlertDialog
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.MainThread
import androidx.recyclerview.widget.RecyclerView
import com.cookieinformation.mobileconsents.R
import com.cookieinformation.mobileconsents.ui.PrivacyPreferencesViewData.ButtonState
import com.cookieinformation.mobileconsents.util.setTextFomHtml
import java.util.UUID

public class PrivacyPreferencesView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0,
  defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

  public enum class ButtonId {
    ReadMore,
    RejectAll,
    AcceptAll,
    AcceptSelected
  }

  @MainThread
  public interface IntentListener {

    public fun onPrivacyPreferenceChoiceChanged(id: UUID, accepted: Boolean)

    public fun onPrivacyPreferenceButtonClicked(buttonId: ButtonId)
  }

  private val intentListeners = mutableSetOf<IntentListener>()
  private val consentListAdapter = PrivacyPreferencesListAdapter(::onChoiceChanged)

  private val consentsView: View
  private val progressBar: View

  private val buttonIdsMap = mapOf(
    R.id.mobileconsents_btn_read_more to ButtonId.ReadMore,
    R.id.mobileconsents_btn_reject_all to ButtonId.RejectAll,
    R.id.mobileconsents_btn_accept_all to ButtonId.AcceptAll,
    R.id.mobileconsents_btn_accept_selected to ButtonId.AcceptSelected,
  )

  init {
    inflate(context, R.layout.mobileconsents_privacy_preferences, this)
    consentsView = findViewById(R.id.mobileconsents_privacy_preferences_layout)
    consentsView.visibility = View.INVISIBLE

    inflate(context, R.layout.mobileconsents_progressbar, this)
    progressBar = findViewById(R.id.mobileconsents_progressbar_layout)
    progressBar.visibility = View.VISIBLE

    consentsView.findViewById<RecyclerView>(R.id.mobileconsents_privacy_preferences_list).apply {
      adapter = consentListAdapter
    }

    setupButtons()
  }

  private fun setupButtons() {
    arrayOf(
      R.id.mobileconsents_btn_read_more,
      R.id.mobileconsents_btn_reject_all,
      R.id.mobileconsents_btn_accept_all,
      R.id.mobileconsents_btn_accept_selected
    ).forEach {
      findViewById<Button>(it).setOnClickListener(::onButtonClicked)
    }
  }

  private fun onButtonClicked(button: View) {
    for (listener in intentListeners) {
      listener.onPrivacyPreferenceButtonClicked(buttonIdsMap[button.id]!!)
    }
  }

  private fun onChoiceChanged(id: UUID, accepted: Boolean) {
    for (listener in intentListeners) {
      listener.onPrivacyPreferenceChoiceChanged(id, accepted)
    }
  }

  @MainThread
  public fun addIntentListener(intentListener: IntentListener) {
    require(!intentListeners.contains(intentListener))
    intentListeners.add(intentListener)
  }

  @MainThread
  public fun removeIntentListener(intentListener: IntentListener) {
    require(intentListeners.contains(intentListener))
    intentListeners.remove(intentListener)
  }

  internal fun showProgressBar() {
    progressBar.visibility = View.VISIBLE
  }

  internal fun hideProgressBar() {
    progressBar.visibility = View.GONE
  }

  internal fun showViewData(data: PrivacyPreferencesViewData) {
    findViewById<TextView>(R.id.mobileconsents_privacy_preferences_title).text = data.title
    updateHtmlText(R.id.mobileconsents_privacy_preferences_sub_title, data.subTitle, false)
    updateHtmlText(R.id.mobileconsents_privacy_preferences_description, data.description, true)
    updateButton(R.id.mobileconsents_btn_read_more, data.buttonReadMore)
    updateButton(R.id.mobileconsents_btn_reject_all, data.buttonRejectAll)
    updateButton(R.id.mobileconsents_btn_accept_all, data.buttonAcceptAll)
    updateButton(R.id.mobileconsents_btn_accept_selected, data.buttonAcceptSelected)
    updateConsentList(data)

    consentsView.visibility = View.VISIBLE
  }

  internal fun hideViewData() {
    consentsView.visibility = View.GONE
  }

  private fun updateConsentList(data: PrivacyPreferencesViewData) =
    consentListAdapter.submitList(data.items)

  private fun updateButton(@IdRes viewId: Int, buttonState: ButtonState) {
    findViewById<Button>(viewId).apply {
      text = buttonState.text
      isEnabled = buttonState.enabled
    }
  }

  private fun updateHtmlText(@IdRes viewId: Int, html: String, boldLinks: Boolean) {
    findViewById<TextView>(viewId).apply {
      setTextFomHtml(html, boldLinks)
    }
  }

  override fun onDetachedFromWindow() {
    removeCallbacks(null)
    super.onDetachedFromWindow()
  }

  internal fun showRetryDialog(onRetry: () -> Unit, onDismiss: () -> Unit) {
    // postDelayed is workaround for: If view is embedded in a DialogFragment, the below dialog is shown under the DialogFragment.
    postDelayed(
      {
        AlertDialog.Builder(context)
          .setCancelable(false)
          .setTitle(R.string.mobileconsents_privacy_preferences_title_error_fetch)
          .setMessage(R.string.mobileconsents_privacy_preferences_msg_error_fetch)
          .setPositiveButton(R.string.mobileconsents_privacy_preferences_btn_retry) { _, _ -> onRetry() }
          .setNegativeButton(android.R.string.cancel) { _, _ -> onDismiss() }
          .create()
          .show()
      },
      0
    )
  }

  internal fun showErrorDialog(onDismiss: () -> Unit) {
    // postDelayed is workaround for: If view is embedded in a DialogFragment, the below dialog is shown under the DialogFragment.
    postDelayed(
      {
        AlertDialog.Builder(context)
          .setCancelable(false)
          .setTitle(R.string.mobileconsents_privacy_preferences_title_error_send)
          .setMessage(R.string.mobileconsents_privacy_preferences_msg_error_send)
          .setPositiveButton(android.R.string.ok) { _, _ -> onDismiss() }
          .create()
          .show()
      },
      0
    )
  }
}
