package com.cookieinformation.mobileconsents.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView
import com.cookieinformation.mobileconsents.R
import com.cookieinformation.mobileconsents.ui.PrivacyPreferencesView.IntentListener
import com.cookieinformation.mobileconsents.ui.PrivacyPreferencesViewData.ButtonState
import com.cookieinformation.mobileconsents.util.setTextFromHtml
import java.util.UUID

/**
 * The Privacy Preferences view implementation. The view is used in [BasePrivacyPreferencesDialogFragment] and should not be used directly
 * (except capturing events for analytics by [PrivacyPreferencesView.IntentListener]).
 */
public class PrivacyPreferencesView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0,
  defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes),
  ConsentSolutionView<PrivacyPreferencesViewData, IntentListener> {

  /**
   * An enumeration that represents all the possible types of buttons in the view.
   */
  public enum class ButtonId {
    /**
     * Context: user wants to read more about consents
     */
    ReadMore,

    /**
     * Context: user wants to reject all consents. It is possible only if there are no required consents.
     */
    RejectAll,

    /**
     * Context: user wants to accept all consents.
     */
    AcceptAll,

    /**
     * Context: user wants to accept only selected consents. It is possible only if all required consents are chosen.
     */
    AcceptSelected,
  }

  public interface IntentListener {

    /**
     * Called when the user wants to change the choice for the consent.
     *
     * @param id [UUID] of the consents.
     * @param accepted user's choice.
     */
    public fun onPrivacyPreferenceChoiceChanged(id: UUID, accepted: Boolean)

    /**
     * Called when the user clicks one of the buttons on the view.
     * If the buttonId is [ButtonId.AcceptSelected] all required consents are chosen by the user.
     *
     * @param buttonId id of the clicked button. See [ButtonId]
     */
    public fun onPrivacyPreferenceButtonClicked(buttonId: ButtonId)
  }

  private val intentListeners = mutableSetOf<IntentListener>()
  private val consentListAdapter = PrivacyPreferencesListAdapter(
    R.layout.mobileconsents_privacy_preferences_item, ::onChoiceChanged
  )

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

  public override fun addIntentListener(listener: IntentListener) {
    require(!intentListeners.contains(listener))
    intentListeners.add(listener)
  }

  public override fun removeIntentListener(listener: IntentListener) {
    require(intentListeners.contains(listener))
    intentListeners.remove(listener)
  }

  override fun showProgressBar() {
    progressBar.visibility = View.VISIBLE
  }

  override fun hideProgressBar() {
    progressBar.visibility = View.GONE
  }

  override fun showViewData(data: PrivacyPreferencesViewData) {
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

  override fun hideViewData() {
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
      setTextFromHtml(html, boldLinks)
    }
  }

  override fun onDetachedFromWindow() {
    removeCallbacks(null)
    super.onDetachedFromWindow()
  }

  override fun showRetryDialog(onRetry: () -> Unit, onDismiss: () -> Unit) {
    // postDelayed is workaround for: If view is embedded in a DialogFragment, the below dialog is shown under the DialogFragment.
    postDelayed({ createRetryDialog(context, onRetry, onDismiss).show() }, 0)
  }

  override fun showErrorDialog(onDismiss: () -> Unit) {
    // postDelayed is workaround for: If view is embedded in a DialogFragment, the below dialog is shown under the DialogFragment.
    postDelayed({ createErrorDialog(context, onDismiss).show() }, 0)
  }
}
