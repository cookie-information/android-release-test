package com.cookieinformation.mobileconsents.ui

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cookieinformation.mobileconsents.R
import com.cookieinformation.mobileconsents.ui.PrivacyCenterItem.PrivacyCenterDetailsItem
import com.cookieinformation.mobileconsents.ui.PrivacyCenterItem.PrivacyCenterInfoItem
import com.cookieinformation.mobileconsents.ui.PrivacyCenterItem.PrivacyCenterPreferencesItem
import com.cookieinformation.mobileconsents.util.setTextFomHtml
import java.util.UUID

private const val itemTypeInfo = 1
private const val itemTypeDetails = 2
private const val itemTypePreferences = 3

internal class PrivacyCenterListAdapter(
  private val onConsentInfoExpandToggle: (UUID) -> Unit,
  private val onConsentItemChoiceToggle: (UUID, Boolean) -> Unit
) : ListAdapter<PrivacyCenterItem, PrivacyCenterListAdapter.ItemViewHolder>(AdapterConsentItemDiffCallback()) {

  override fun getItemViewType(position: Int): Int =
    when (getItem(position)) {
      is PrivacyCenterInfoItem -> itemTypeInfo
      is PrivacyCenterDetailsItem -> itemTypeDetails
      is PrivacyCenterPreferencesItem -> itemTypePreferences
    }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    when (viewType) {
      itemTypeInfo -> TextItemViewHolder(
        LayoutInflater.from(parent.context)
          .inflate(R.layout.mobileconsents_privacy_center_item_info, parent, false),
        onConsentInfoExpandToggle,
      )
      itemTypeDetails -> TextItemViewHolder(
        LayoutInflater.from(parent.context)
          .inflate(R.layout.mobileconsents_privacy_center_item_details, parent, false),
        onConsentInfoExpandToggle,
      )
      itemTypePreferences -> PreferencesItemViewHolder(
        LayoutInflater.from(parent.context)
          .inflate(R.layout.mobileconsents_privacy_center_item_preferences, parent, false),
        onConsentItemChoiceToggle,
      )
      else -> error("Unknown viewType: $viewType")
    }

  override fun onBindViewHolder(holder: ItemViewHolder, position: Int) = holder.bind(getItem(position))

  abstract class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(item: PrivacyCenterItem)
  }

  class TextItemViewHolder(itemView: View, val onConsentInfoExpandToggle: (UUID) -> Unit) : ItemViewHolder(itemView) {

    private val consentText = itemView.findViewById<TextView>(R.id.mobileconsents_privacy_center_item_text).apply {
      TextViewCompat.setCompoundDrawableTintList(this, ColorStateList.valueOf(currentTextColor))
    }

    override fun bind(item: PrivacyCenterItem) {
      when (item) {
        is PrivacyCenterInfoItem -> with(consentText) {
          setIndicatorDrawable(item.expanded)
          setTextFomHtml(item.text)
          setOnClickListener {
            onConsentInfoExpandToggle(item.id)
          }
        }
        is PrivacyCenterDetailsItem -> consentText.setTextFomHtml(item.details)
        else -> error("Invalid PrivacyCenterItem")
      }
    }

    private fun TextView.setIndicatorDrawable(expanded: Boolean) {
      val indicatorResId = if (expanded) R.drawable.mobileconsents_ic_colapse else R.drawable.mobileconsents_ic_expand
      setCompoundDrawablesWithIntrinsicBounds(0, 0, indicatorResId, 0)
    }
  }

  class PreferencesItemViewHolder(itemView: View, onConsentItemChoiceToggle: (UUID, Boolean) -> Unit) :
    ItemViewHolder(itemView) {

    private val title = itemView.findViewById<TextView>(R.id.mobileconsents_privacy_center_preferences_title)

    private val subTitle =
      itemView.findViewById<TextView>(R.id.mobileconsents_privacy_center_preferences_sub_title)

    private val preferencesAdapter = PrivacyPreferencesListAdapter(
      R.layout.mobileconsents_privacy_center_item_preferences_item,
      onConsentItemChoiceToggle
    )

    init {
      itemView.findViewById<RecyclerView>(R.id.mobileconsents_privacy_center_preferences_list).apply {
        adapter = preferencesAdapter
      }
    }

    override fun bind(item: PrivacyCenterItem) {
      val preferencesItem = item as PrivacyCenterPreferencesItem
      title.text = preferencesItem.title
      subTitle.setTextFomHtml(preferencesItem.subTitle, boldLinks = false, underline = true)
      preferencesAdapter.submitList(preferencesItem.items)
    }
  }

  class AdapterConsentItemDiffCallback : DiffUtil.ItemCallback<PrivacyCenterItem>() {

    override fun areItemsTheSame(oldItem: PrivacyCenterItem, newItem: PrivacyCenterItem) =
      when (oldItem) {
        is PrivacyCenterInfoItem -> if (newItem is PrivacyCenterInfoItem) oldItem.id == newItem.id else false
        is PrivacyCenterDetailsItem -> if (newItem is PrivacyCenterDetailsItem) oldItem.id == newItem.id else false
        is PrivacyCenterPreferencesItem ->
          if (newItem is PrivacyCenterPreferencesItem) oldItem.id == newItem.id else false
      }

    override fun areContentsTheSame(oldItem: PrivacyCenterItem, newItem: PrivacyCenterItem) =
      oldItem == newItem
  }
}
