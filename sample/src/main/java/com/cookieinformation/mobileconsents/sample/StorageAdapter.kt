package com.cookieinformation.mobileconsents.sample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_storage.view.textKey
import kotlinx.android.synthetic.main.item_storage.view.textValue
import java.util.UUID

class StorageAdapter : ListAdapter<StorageItem, StorageItemViewHolder>(StorageItemCallback) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    StorageItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_storage, parent, false))

  override fun onBindViewHolder(holder: StorageItemViewHolder, position: Int) =
    holder.bind(getItem(position))
}

class StorageItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

  fun bind(storageItem: StorageItem) =
    with(itemView) {
      textKey.text = storageItem.key.toString()
      textValue.text = storageItem.value.toString()
    }
}

private object StorageItemCallback : DiffUtil.ItemCallback<StorageItem>() {

  override fun areItemsTheSame(oldItem: StorageItem, newItem: StorageItem) =
    oldItem.key == newItem.key

  override fun areContentsTheSame(oldItem: StorageItem, newItem: StorageItem) =
    oldItem.value == newItem.value
}

data class StorageItem(
  val key: UUID,
  val value: Boolean
)
