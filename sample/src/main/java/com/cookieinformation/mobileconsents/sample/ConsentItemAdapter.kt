package com.cookieinformation.mobileconsents.sample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cookieinformation.mobileconsents.ConsentItem
import com.cookieinformation.mobileconsents.ConsentTranslation
import kotlinx.android.synthetic.main.item_consent.view.container
import kotlinx.android.synthetic.main.item_consent.view.switchConsent
import kotlinx.android.synthetic.main.item_consent.view.textConsentId
import kotlinx.android.synthetic.main.item_consent.view.textConsentLongText
import kotlinx.android.synthetic.main.item_consent.view.textConsentRequired
import kotlinx.android.synthetic.main.item_consent.view.textConsentShortText
import kotlinx.android.synthetic.main.item_consent.view.textConsentType
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
    val preferredLanguageCode = preferredTranslation.toUpperCase()
    val translation =
      it.translations.firstOrNull { it.languageCode == preferredLanguageCode } ?: it.translations.first()
    ConsentChoice(
      translation = translation,
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
      textConsentShortText.text = consentItem.translation.shortText
      textConsentLongText.text = consentItem.translation.longText
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
    oldItem.translation == newItem.translation && oldItem.choice == newItem.choice
}

data class ConsentChoice(
  val translation: ConsentTranslation,
  val itemId: UUID,
  val choice: Boolean,
  val enableChoice: Boolean,
  val type: String,
  val required: String
)
