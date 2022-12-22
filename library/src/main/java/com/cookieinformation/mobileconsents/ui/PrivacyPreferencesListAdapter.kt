package com.cookieinformation.mobileconsents.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cookieinformation.mobileconsents.ConsentItem.Type
import com.cookieinformation.mobileconsents.R
import com.cookieinformation.mobileconsents.util.setOnClickListenerButDoNotInvokeWhenSpanClicked
import com.cookieinformation.mobileconsents.util.setTextFromHtml
import java.util.UUID

private const val requireIndicator = "<a href=\"\">*</a>"

/**
 * The RecyclerView's adapter for [PrivacyPreferencesItem] item model.
 */
internal class PrivacyPreferencesListAdapter(
  @LayoutRes private val itemLayoutId: Int,
  private val onConsentItemChoiceChanged: (Type, Boolean) -> Unit
) :
  ListAdapter<PrivacyPreferencesItem, PrivacyPreferencesListAdapter.ItemViewHolder>(AdapterConsentItemDiffCallback()) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    ItemViewHolder(
      LayoutInflater.from(parent.context).inflate(itemLayoutId, parent, false)
    )

  override fun onBindViewHolder(holder: ItemViewHolder, position: Int) =
    holder.bind(getItem(position), onConsentItemChoiceChanged)

  class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val consentSwitch =
      itemView.findViewById<CompoundButton>(R.id.mobileconsents_privacy_preferences_item_checkbox)

    private val consentText =
      itemView.findViewById<TextView>(R.id.mobileconsents_privacy_preferences_item_text)

    private val consentDetails =
      itemView.findViewById<TextView?>(R.id.mobileconsents_privacy_preferences_item_details)

    fun bind(
      consentItem: PrivacyPreferencesItem,
      onConsentItemChanged: (Type, Boolean) -> Unit
    ) {
      consentText.apply {
        setTextFromHtml(if (consentItem.required) "${consentItem.text}$requireIndicator" else consentItem.text)
      }
      consentSwitch.apply {
        isChecked = consentItem.accepted || consentItem.required
        if (consentItem.required) {
          onConsentItemChanged.invoke(consentItem.type, isChecked)
        }
        isClickable = !consentItem.required
        setOnCheckedChangeListener { buttonView, isChecked ->
          if (buttonView.isPressed) {
            // Detect only user action
            onConsentItemChanged.invoke(consentItem.type, isChecked)
          }
        }
      }
      consentDetails?.apply {
        setTextFromHtml(consentItem.details)
        visibility = if (consentItem.details.isBlank()) View.GONE else View.VISIBLE
      }
    }
  }

  class AdapterConsentItemDiffCallback : DiffUtil.ItemCallback<PrivacyPreferencesItem>() {

    override fun areItemsTheSame(oldItem: PrivacyPreferencesItem, newItem: PrivacyPreferencesItem) =
      oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: PrivacyPreferencesItem, newItem: PrivacyPreferencesItem) =
      oldItem == newItem
  }
}
