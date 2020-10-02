package com.clearcode.mobileconsents.sample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.clearcode.mobileconsents.ConsentItem
import kotlinx.android.synthetic.main.item_consent.view.textConsentId
import kotlinx.android.synthetic.main.item_consent.view.textConsentLongText
import kotlinx.android.synthetic.main.item_consent.view.textConsentShortText

class ConsentItemAdapter : ListAdapter<ConsentItem, ConsentItemViewHolder>(ConsentItemDiffCallback) {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    ConsentItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_consent, parent, false))

  override fun onBindViewHolder(holder: ConsentItemViewHolder, position: Int) =
    holder.bind(getItem(position))
}

class ConsentItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

  fun bind(consentItem: ConsentItem) {
    val firstTranslation = consentItem.translations.first()

    with(itemView) {
      textConsentId.text = consentItem.consentItemId.toString()
      textConsentShortText.text = firstTranslation.shortText
      textConsentLongText.text = firstTranslation.longText
    }
  }
}

private object ConsentItemDiffCallback : DiffUtil.ItemCallback<ConsentItem>() {

  override fun areItemsTheSame(oldItem: ConsentItem, newItem: ConsentItem) =
    oldItem.consentItemId == newItem.consentItemId

  override fun areContentsTheSame(oldItem: ConsentItem, newItem: ConsentItem) =
    oldItem.translations == newItem.translations
}
