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
//import com.cookieinformation.mobileconsents.ui.PrivacyFragmentItem.PrivacyFragmentDetailsItem
import com.cookieinformation.mobileconsents.ui.PrivacyFragmentItem.PrivacyFragmentInfoItem
//import com.cookieinformation.mobileconsents.ui.PrivacyFragmentItem.PrivacyFragmentPreferencesItem // Should be removed
import com.cookieinformation.mobileconsents.util.setOnClickListenerButDoNotInvokeWhenSpanClicked
import com.cookieinformation.mobileconsents.util.setTextFromHtml
import java.util.UUID

private const val itemTypeInfo = 1
private const val itemTypeDetails = 2
private const val itemTypePreferences = 3

/**
 * RecyclerView's adapter for [PrivacyFragmentPreferencesItem] item model.
 */
internal class PrivacyFragmentListAdapter(
  //private val onConsentInfoExpandToggle: (UUID) -> Unit,
  private val onConsentItemChoiceToggle: (UUID, Boolean) -> Unit
) : ListAdapter<PrivacyFragmentPreferencesItem, PrivacyFragmentListAdapter.ItemViewHolder>(AdapterConsentItemDiffCallback()) {

/*
  override fun getItemViewType(position: Int): Int =
    when (getItem(position)) {
      is PrivacyFragmentInfoItem -> itemTypeInfo
      is PrivacyFragmentDetailsItem -> itemTypeDetails
      is PrivacyFragmentPreferencesItem -> itemTypePreferences
    }
*/

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    PreferencesItemViewHolder(
      LayoutInflater.from(parent.context)
        .inflate(R.layout.mobileconsents_privacy_item_preferences, parent, false),
      onConsentItemChoiceToggle,
    )
/*
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
          .inflate(R.layout.mobileconsents_privacy_item_preferences, parent, false),
        onConsentItemChoiceToggle,
      )
      else -> error("Unknown viewType: $viewType")
    }
*/

  override fun onBindViewHolder(holder: ItemViewHolder, position: Int) = holder.bind(getItem(position))

  abstract class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(item: PrivacyFragmentPreferencesItem)
  }

/*
  class TextItemViewHolder(itemView: View, val onConsentInfoExpandToggle: (UUID) -> Unit) : ItemViewHolder(itemView) {

    private val consentText = itemView.findViewById<TextView>(R.id.mobileconsents_privacy_center_item_text).apply {
      TextViewCompat.setCompoundDrawableTintList(this, ColorStateList.valueOf(currentTextColor))
    }

    override fun bind(item: PrivacyFragmentItem) {
      when (item) {
        is PrivacyFragmentInfoItem -> with(consentText) {
          setIndicatorDrawable(item.expanded)
          setTextFromHtml(item.text)
          setOnClickListenerButDoNotInvokeWhenSpanClicked {
            onConsentInfoExpandToggle(item.id)
          }
        }
        is PrivacyFragmentDetailsItem -> consentText.setTextFromHtml(item.details)
        else -> error("Invalid PrivacyCenterItem")
      }
    }

    private fun TextView.setIndicatorDrawable(expanded: Boolean) {
      val indicatorResId = if (expanded) R.drawable.mobileconsents_ic_colapse else R.drawable.mobileconsents_ic_expand
      setCompoundDrawablesWithIntrinsicBounds(0, 0, indicatorResId, 0)
    }
  }
*/

  class PreferencesItemViewHolder(itemView: View, onConsentItemChoiceToggle: (UUID, Boolean) -> Unit) :
    ItemViewHolder(itemView) {

//    private val title = itemView.findViewById<TextView>(R.id.mobileconsents_privacy_preferences_title)

/*
    private val subTitle =
      itemView.findViewById<TextView>(R.id.mobileconsents_privacy_center_preferences_sub_title)
*/

    private val preferencesAdapter = PrivacyPreferencesListAdapter(
      R.layout.mobileconsents_privacy_item_preferences_item,
      onConsentItemChoiceToggle
    )

    init {
      itemView.findViewById<RecyclerView>(R.id.mobileconsents_privacy_preferences_list).apply {
        adapter = preferencesAdapter
      }
    }

    override fun bind(item: PrivacyFragmentPreferencesItem) {
      //val preferencesItem = item as PrivacyFragmentPreferencesItem
      //title.text = preferencesItem.title
      //subTitle.setTextFromHtml(preferencesItem.subTitle, boldLinks = false, underline = true)
      val sort = item.items.sortedBy { !it.required }
      preferencesAdapter.submitList(sort)
    }
  }

  class AdapterConsentItemDiffCallback : DiffUtil.ItemCallback<PrivacyFragmentPreferencesItem>() {

    override fun areItemsTheSame(oldItem: PrivacyFragmentPreferencesItem, newItem: PrivacyFragmentPreferencesItem) =
      oldItem.id == newItem.id
/*
      when (oldItem) {
        is PrivacyFragmentInfoItem -> if (newItem is PrivacyFragmentInfoItem) oldItem.id == newItem.id else false
        is PrivacyFragmentDetailsItem -> if (newItem is PrivacyFragmentDetailsItem) oldItem.id == newItem.id else false
        is PrivacyFragmentPreferencesItem ->
          if (newItem is PrivacyFragmentPreferencesItem) oldItem.id == newItem.id else false
      }
*/

    override fun areContentsTheSame(oldItem: PrivacyFragmentPreferencesItem, newItem: PrivacyFragmentPreferencesItem) =
      oldItem == newItem
  }
}
