package com.cookieinformation.mobileconsents.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cookieinformation.mobileconsents.CallListener
import com.cookieinformation.mobileconsents.MobileConsentSdk
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.buttonFetch
import kotlinx.android.synthetic.main.activity_storage.buttonMainPage
import kotlinx.android.synthetic.main.activity_storage.recyclerStorage
import java.io.IOException
import java.util.UUID

class StorageActivity : AppCompatActivity(R.layout.activity_storage) {

  private lateinit var sdk: MobileConsentSdk
  private val storageAdapter = StorageAdapter()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    sdk = MobileConsentSdk.Builder(this.applicationContext)
      .partnerUrl(getString(R.string.sample_partner_url))
      .callFactory(getOkHttpClient(this.applicationContext))
      .build()

    buttonMainPage.setOnClickListener {
      finish()
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
          Snackbar.make(buttonFetch, error.message.toString(), Snackbar.LENGTH_SHORT).show()
        }
      }
    )
  }
}
