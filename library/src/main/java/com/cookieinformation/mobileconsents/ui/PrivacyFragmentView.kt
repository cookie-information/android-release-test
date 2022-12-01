package com.cookieinformation.mobileconsents.ui

import android.content.Context
import android.os.Build
import android.text.Html
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.MainThread
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.cookieinformation.mobileconsents.R
import com.cookieinformation.mobileconsents.ui.PrivacyFragmentView.IntentListener2
import com.google.android.material.button.MaterialButton
import java.util.UUID

/**
 * The Privacy view implementation. The view is used in [BasePrivacyFragment] and should not be used directly
 * (except for ex. capturing events for analytics by [PrivacyFragmentView.IntentListener2]).
 */
public class PrivacyFragmentView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0,
  defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes),
  ConsentSolutionView<PrivacyFragmentViewData, IntentListener2> {

  /**
   * A listener for events that can be triggered by the user.
   */
  @MainThread
  public interface IntentListener2 {

    /**
     * Called when the user wants to change the choice for the consent.
     *
     * @param id [UUID] of the consents.
     * @param accepted user's choice.
     */
    public fun onPrivacyChoiceChanged(id: UUID, accepted: Boolean)

    /**
     * Called when the user wants toggle visibility of the details information.
     *
     * @param id [UUID] of the information.
     */
    //public fun onPrivacyCenterDetailsToggle(id: UUID)

    /**
     * Called when the user accepts selected consents. It is called only if all required consents are chosen by the user.
     */
    public fun onPrivacyAcceptSelectedClicked()

    /**
     * Called when the user accepts all consents.
     */
    public fun onPrivacyAcceptAllClicked()

    /**
     * Called when the user wants to close the view.
     */
    public fun onPrivacyCenterDismissRequest()
  }

  private val intentListeners = mutableSetOf<IntentListener2>()
  private val consentListAdapter = PrivacyFragmentListAdapter(/*::onDetailsToggle, */::onChoiceChanged)

  private val contentView: View
  private val infoView: View
  private val progressBar: View

  init {
    inflate(context, R.layout.mobileconsents_privacy, this)
    contentView = findViewById(R.id.mobileconsents_privacy_layout)
    contentView.visibility = View.GONE

    inflate(context, R.layout.mobileconsents_privacy_info, this)
    infoView = findViewById(R.id.mobileconsents_privacy_info_layout)
    infoView.visibility = View.GONE

    inflate(context, R.layout.mobileconsents_progressbar, this)
    progressBar = findViewById(R.id.mobileconsents_progressbar_layout)
    progressBar.visibility = View.VISIBLE

    contentView.findViewById<RecyclerView>(R.id.mobileconsents_privacy_list).apply {
      setHasFixedSize(true)
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      adapter = consentListAdapter
    }

    contentView.findViewById<Toolbar>(R.id.mobileconsents_privacy_toolbar).apply {
      setNavigationOnClickListener {
        onDismissRequest()
      }
    }

    infoView.findViewById<Toolbar>(R.id.mobileconsents_privacy_toolbar).apply {
      setNavigationOnClickListener {
        showContentViewData()
      }
    }

    findViewById<TextView>(R.id.mobileconsents_privacy_info_read_more).setOnClickListener { onReadMoreClicked() }

    findViewById<MaterialButton>(R.id.mobileconsents_privacy_accept_selected_button).setOnClickListener { onAcceptSelectedClicked() }

    findViewById<MaterialButton>(R.id.mobileconsents_privacy_accept_all_button).setOnClickListener { onAcceptAllClicked() }
  }

  private fun onReadMoreClicked() {
    showInfoViewData()
  }

  private fun onAcceptSelectedClicked() {
    for (listener in intentListeners) {
      listener.onPrivacyAcceptSelectedClicked()
    }
  }

  private fun onAcceptAllClicked() {
    for (listener in intentListeners) {
      listener.onPrivacyAcceptAllClicked()
    }
  }

  private fun onChoiceChanged(id: UUID, accepted: Boolean) {
    for (listener in intentListeners) {
      listener.onPrivacyChoiceChanged(id, accepted)
    }
  }

/*
  private fun onDetailsToggle(id: UUID) {
    for (listener in intentListeners) {
      listener.onPrivacyCenterDetailsToggle(id)
    }
  }
*/

  private fun onDismissRequest() {
    for (listener in intentListeners) {
      listener.onPrivacyCenterDismissRequest()
    }
  }

  public override fun addIntentListener(listener: IntentListener2) {
    require(!intentListeners.contains(listener))
    intentListeners.add(listener)
  }

  public override fun removeIntentListener(listener: IntentListener2) {
    require(intentListeners.contains(listener))
    intentListeners.remove(listener)
  }

  override fun showProgressBar() {
    progressBar.visibility = View.VISIBLE
  }

  override fun hideProgressBar() {
    progressBar.visibility = View.GONE
  }

  override fun showViewData(data: PrivacyFragmentViewData) {
    findViewById<TextView>(R.id.mobileconsents_privacy_info_title).apply {
      text = data.privacyTitleText
    }
    findViewById<TextView>(R.id.mobileconsents_privacy_info_short_description).apply {
      text = data.privacyDescriptionShortText
    }
    findViewById<TextView>(R.id.mobileconsents_privacy_info_read_more).apply {
      text = data.privacyReadMoreText
    }
    findViewById<TextView>(R.id.mobileconsents_privacy_info_long_description).apply {
      text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(data.privacyDescriptionLongText, Html.FROM_HTML_MODE_COMPACT)
      } else {
        Html.fromHtml(data.privacyDescriptionLongText)
      }
    }
    findViewById<MaterialButton>(R.id.mobileconsents_privacy_accept_selected_button).apply {
      text = data.acceptSelectedButtonText
      isEnabled = data.acceptSelectedButtonEnabled
    }
    findViewById<MaterialButton>(R.id.mobileconsents_privacy_accept_all_button).apply {
      text = data.acceptAllButtonText
    }
    contentView.findViewById<TextView>(R.id.powered_by_label).apply {
      text = data.poweredByLabelText
    }
    infoView.findViewById<TextView>(R.id.powered_by_label).apply {
      text = data.poweredByLabelText
    }
    consentListAdapter.submitList(data.items)
    showContentViewData()
  }

  override fun hideViewData() {
    contentView.visibility = View.GONE
    infoView.visibility = View.GONE
  }

  private fun showContentViewData() {
    contentView.visibility = View.VISIBLE
    infoView.visibility = View.GONE
  }

  private fun showInfoViewData() {
    contentView.visibility = View.GONE
    infoView.visibility = View.VISIBLE
  }

  override fun showRetryDialog(onRetry: () -> Unit, onDismiss: () -> Unit, title: String, message: String) {
    // postDelayed is workaround for: If view is embedded in a DialogFragment, the below dialog is shown under the DialogFragment.
    postDelayed({ createRetryDialog(context, onRetry, onDismiss, title, message).show() }, 0)
  }

  override fun showErrorDialog(onDismiss: () -> Unit) {
    // postDelayed is workaround for: If view is embedded in a DialogFragment, the below dialog is shown under the DialogFragment.
    postDelayed({ createErrorDialog(context, onDismiss).show() }, 0)
  }
}
