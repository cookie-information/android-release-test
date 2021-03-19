package com.cookieinformation.mobileconsents.ui

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.MainThread
import androidx.core.text.HtmlCompat
import androidx.core.text.util.LinkifyCompat
import androidx.recyclerview.widget.RecyclerView
import com.cookieinformation.mobileconsents.R
import com.cookieinformation.mobileconsents.ui.PrivacyPreferencesViewData.ButtonState
import com.cookieinformation.mobileconsents.util.changeLinksStyle
import java.util.UUID

// TODO remove this when presenter is ready
internal val fakeViewData = PrivacyPreferencesViewData(
  title = "Privacy settings",
  subTitle = "<a href=\"https://cookieinformation.com\">Powered by Cookie Information</a>",
  description = "Some are used for statistical purposes and others are set up by third party services." +
    "<a href=\"https://cookieinformation.com/\">Test link</a>",
  items = listOf(
    PrivacyPreferencesItem(
      id = UUID.randomUUID(),
      required = true,
      accepted = true,
      text = "I agree to the Terms of Services.",
    ),
    PrivacyPreferencesItem(
      id = UUID.randomUUID(),
      required = false,
      accepted = false,
      text = "I consent to the use of my personal data. <a href=\"https://cookieinformation.com\">Example link</a>",
    ),
    PrivacyPreferencesItem(
      id = UUID.randomUUID(),
      required = false,
      accepted = false,
      text = "Personalised Experience",
    ),
  ),
  buttonReadMore = ButtonState("Read More", true),
  buttonAcceptAll = ButtonState("Accept All", true),
  buttonRejectAll = ButtonState("Reject All", false),
  buttonAcceptSelected = ButtonState("Accept Selected", true),
)

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

    // TODO remove this when presenter is ready
    showData(fakeViewData)
    showData(fakeViewData)
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
    consentsView.visibility = View.INVISIBLE
    progressBar.visibility = View.VISIBLE
  }

  internal fun showData(data: PrivacyPreferencesViewData) {
    findViewById<TextView>(R.id.mobileconsents_privacy_preferences_title).text = data.title
    updateHtmlText(R.id.mobileconsents_privacy_preferences_sub_title, data.subTitle, false)
    updateHtmlText(R.id.mobileconsents_privacy_preferences_description, data.description, true)
    updateButton(R.id.mobileconsents_btn_read_more, data.buttonReadMore)
    updateButton(R.id.mobileconsents_btn_reject_all, data.buttonRejectAll)
    updateButton(R.id.mobileconsents_btn_accept_all, data.buttonAcceptAll)
    updateButton(R.id.mobileconsents_btn_accept_selected, data.buttonAcceptSelected)
    updateConsentList(data)

    progressBar.visibility = View.GONE
    consentsView.visibility = View.VISIBLE
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
      val stringBuilder = SpannableStringBuilder(HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT)).apply {
        changeLinksStyle(bold = boldLinks)
      }
      LinkifyCompat.addLinks(this, Linkify.WEB_URLS)
      text = stringBuilder
      movementMethod = LinkMovementMethod.getInstance()
    }
  }

  internal fun showError() {
    progressBar.visibility = View.GONE
    consentsView.visibility = View.INVISIBLE
    consentListAdapter.submitList(null)
  }
}
