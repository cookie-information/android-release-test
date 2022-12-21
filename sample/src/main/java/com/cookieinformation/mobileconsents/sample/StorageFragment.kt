package com.cookieinformation.mobileconsents.sample

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.cookieinformation.mobileconsents.CallListener
import com.cookieinformation.mobileconsents.MobileConsents
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_storage.recyclerStorage
import kotlinx.android.synthetic.main.fragment_storage.toolbar
import java.io.IOException
import java.util.UUID

class StorageFragment : Fragment(R.layout.fragment_storage) {

  private lateinit var sdk: MobileConsents
  private val storageAdapter = StorageAdapter()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    sdk = (requireContext().applicationContext as App).sdk

    toolbar.setNavigationOnClickListener {
      requireActivity().onBackPressed()
    }
    recyclerStorage.adapter = storageAdapter
    fetchStorageItems()
  }

  private fun fetchStorageItems() {
    sdk.getSavedConsents(
      listener = object : CallListener<Map<UUID, Boolean>> {
        override fun onSuccess(result: Map<UUID, Boolean>) {
          storageAdapter.submitList(result.map { StorageItem(it.key, it.value) })
        }

        override fun onFailure(error: IOException) {
          Snackbar.make(toolbar, error.message.toString(), Snackbar.LENGTH_SHORT).show()
        }
      }
    )
  }
}
