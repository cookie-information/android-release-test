package com.cookieinformation.mobileconsents.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import androidx.annotation.MainThread
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.cookieinformation.mobileconsents.R
import java.util.UUID

public class PrivacyCenterView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0,
  defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

  @MainThread
  public interface IntentListener {

    public fun onPrivacyCenterChoiceChanged(id: UUID, accepted: Boolean)

    public fun onPrivacyCenterDetailsToggle(id: UUID)

    public fun onPrivacyCenterAcceptClicked()

    public fun onPrivacyCenterDismissRequest()
  }

  private val intentListeners = mutableSetOf<IntentListener>()
  private val consentListAdapter = PrivacyCenterListAdapter(::onDetailsToggle, ::onChoiceChanged)

  private val contentView: View
  private val progressBar: View

  init {
    inflate(context, R.layout.mobileconsents_privacy_center, this)
    contentView = findViewById(R.id.mobileconsents_privacy_center_layout)
    contentView.visibility = View.GONE

    inflate(context, R.layout.mobileconsents_progressbar, this)
    progressBar = findViewById(R.id.mobileconsents_progressbar_layout)
    progressBar.visibility = View.VISIBLE

    contentView.findViewById<RecyclerView>(R.id.mobileconsents_privacy_center_list).apply {
      setHasFixedSize(true)
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      adapter = consentListAdapter
    }

    contentView.findViewById<Toolbar>(R.id.mobileconsents_privacy_center_toolbar).apply {
      setNavigationOnClickListener {
        onDismissRequest()
      }
    }

    findViewById<Button>(R.id.mobileconsents_privacy_center_btn_accept).setOnClickListener { onAcceptClicked() }

    // TODO For manual testing purpose - remove after presenter is implemented
    showViewData(PrivacyCenterViewIntentListener.ViewData)
    addIntentListener(PrivacyCenterViewIntentListener)
  }

  private fun onAcceptClicked() {
    for (listener in intentListeners) {
      listener.onPrivacyCenterAcceptClicked()
    }
  }

  private fun onChoiceChanged(id: UUID, accepted: Boolean) {
    for (listener in intentListeners) {
      listener.onPrivacyCenterChoiceChanged(id, accepted)
    }
  }

  private fun onDetailsToggle(id: UUID) {
    for (listener in intentListeners) {
      listener.onPrivacyCenterDetailsToggle(id)
    }
    // TODO For manual testing purpose - remove after presenter is implemented
    showViewData(PrivacyCenterViewIntentListener.ViewData)
  }

  private fun onDismissRequest() {
    for (listener in intentListeners) {
      listener.onPrivacyCenterDismissRequest()
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

  internal fun showViewData(data: PrivacyCenterViewData) {
    findViewById<Toolbar>(R.id.mobileconsents_privacy_center_toolbar).apply {
      title = data.title
    }
    findViewById<Button>(R.id.mobileconsents_privacy_center_btn_accept).apply {
      text = data.acceptButtonText
      isEnabled = data.acceptButtonEnabled
    }
    consentListAdapter.submitList(data.items)

    contentView.visibility = View.VISIBLE
  }

  internal fun hideViewData() {
    contentView.visibility = View.GONE
  }
}
