package com.cookieinformation.mobileconsents.ui

import android.text.Html
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.text.util.LinkifyCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cookieinformation.mobileconsents.R
import com.cookieinformation.mobileconsents.util.changeLinksStyle
import java.util.UUID

private const val requireIndicator = "<a href=\"\">*</a>"

internal class PrivacyPreferencesListAdapter(
  private val onConsentItemChoiceChanged: (UUID, Boolean) -> Unit
) :
  ListAdapter<PrivacyPreferencesItem, PrivacyPreferencesListAdapter.ItemViewHolder>(AdapterConsentItemDiffCallback()) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    ItemViewHolder(
      LayoutInflater.from(parent.context).inflate(R.layout.mobileconsents_privacy_preferences_item, parent, false)
    )

  override fun onBindViewHolder(holder: ItemViewHolder, position: Int) =
    holder.bind(getItem(position), onConsentItemChoiceChanged)

  class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val consentSwitch = itemView.findViewById<CheckBox>(R.id.mobileconsents_privacy_preferences_item_checkbox)
    private val consentText = itemView.findViewById<TextView>(R.id.mobileconsents_privacy_preferences_item_text).apply {
      setOnClickListener {
        consentSwitch.apply {
          // Pretend user action
          isPressed = true
          toggle()
          isPressed = false
        }
      }
    }

    fun bind(
      consentItem: PrivacyPreferencesItem,
      onConsentItemChanged: (UUID, Boolean) -> Unit
    ) {
      consentText.apply {
        setTextFomHtml(if (consentItem.required) "consentItem.text$requireIndicator" else consentItem.text)
      }
      consentSwitch.apply {
        isChecked = consentItem.accepted
        setOnCheckedChangeListener { buttonView, isChecked ->
          if (buttonView.isPressed) {
            // Detect only user action
            onConsentItemChanged.invoke(consentItem.id, isChecked)
          }
        }
      }
    }

    private fun TextView.setTextFomHtml(html: String) {
      @Suppress("DEPRECATION")
      val stringBuilder = SpannableStringBuilder(Html.fromHtml(html)).apply {
        changeLinksStyle()
      }
      LinkifyCompat.addLinks(this, Linkify.WEB_URLS)
      text = stringBuilder
      movementMethod = LinkMovementMethod.getInstance()
    }
  }

  class AdapterConsentItemDiffCallback : DiffUtil.ItemCallback<PrivacyPreferencesItem>() {

    override fun areItemsTheSame(oldItem: PrivacyPreferencesItem, newItem: PrivacyPreferencesItem) =
      oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: PrivacyPreferencesItem, newItem: PrivacyPreferencesItem) =
      oldItem == newItem
  }
}
