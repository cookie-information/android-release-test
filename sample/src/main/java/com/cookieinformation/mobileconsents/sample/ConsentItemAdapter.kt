package com.cookieinformation.mobileconsents.sample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cookieinformation.mobileconsents.ConsentItem
import com.cookieinformation.mobileconsents.TextTranslation
import kotlinx.android.synthetic.main.item_consent.view.container
import kotlinx.android.synthetic.main.item_consent.view.switchConsent
import kotlinx.android.synthetic.main.item_consent.view.textConsentId
import kotlinx.android.synthetic.main.item_consent.view.textConsentLongText
import kotlinx.android.synthetic.main.item_consent.view.textConsentRequired
import kotlinx.android.synthetic.main.item_consent.view.textConsentShortText
import kotlinx.android.synthetic.main.item_consent.view.textConsentType
import java.util.Locale
import java.util.UUID

class ConsentItemAdapter(
  private val onConsentItemChoiceChanged: (UUID, Boolean) -> Unit
) : ListAdapter<ConsentChoice, ConsentItemViewHolder>(ConsentItemDiffCallback) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    ConsentItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_consent, parent, false))

  override fun onBindViewHolder(holder: ConsentItemViewHolder, position: Int) =
    holder.bind(getItem(position), onConsentItemChoiceChanged)

  fun submitList(consents: List<ConsentItem>?, preferredLanguage: String) {
    val consentChoices = consents?.mapToAdapterItem(preferredLanguage) ?: emptyList()
    super.submitList(consentChoices)
  }

  private fun List<ConsentItem>.mapToAdapterItem(preferredTranslation: String) = map {
    val preferredLocaleList = listOf(
      Locale(if (preferredTranslation.isBlank()) "en" else preferredTranslation.trim().toLowerCase())
    )
    ConsentChoice(
      shortText = TextTranslation.getTranslationFor(it.shortText, preferredLocaleList),
      longText = TextTranslation.getTranslationFor(it.longText, preferredLocaleList),
      choice = it.type == ConsentItem.Type.Info,
      enableChoice = it.type == ConsentItem.Type.Setting,
      itemId = it.consentItemId,
      type = it.type.toString(),
      required = it.required.toString()
    )
  }
}

class ConsentItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

  fun bind(
    consentItem: ConsentChoice,
    onConsentItemChoiceChanged: (UUID, Boolean) -> Unit
  ) {
    with(itemView) {
      textConsentId.text = consentItem.itemId.toString()
      textConsentShortText.text = consentItem.shortText
      textConsentLongText.text = consentItem.longText
      textConsentType.text = consentItem.type
      textConsentRequired.text = consentItem.required
      switchConsent.isChecked = consentItem.choice
      switchConsent.isEnabled = consentItem.enableChoice
      container.setOnClickListener {
        switchConsent.toggle()
      }
      switchConsent.setOnCheckedChangeListener { _, isChecked ->
        onConsentItemChoiceChanged.invoke(consentItem.itemId, isChecked)
      }
    }
  }
}

private object ConsentItemDiffCallback : DiffUtil.ItemCallback<ConsentChoice>() {

  override fun areItemsTheSame(oldItem: ConsentChoice, newItem: ConsentChoice) =
    oldItem.itemId == newItem.itemId

  override fun areContentsTheSame(oldItem: ConsentChoice, newItem: ConsentChoice) =
    oldItem == newItem
}

data class ConsentChoice(
  val shortText: String,
  val longText: String,
  val itemId: UUID,
  val choice: Boolean,
  val enableChoice: Boolean,
  val type: String,
  val required: String
)
