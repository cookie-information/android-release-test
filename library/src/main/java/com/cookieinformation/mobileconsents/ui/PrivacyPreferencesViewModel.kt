package com.cookieinformation.mobileconsents.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookieinformation.mobileconsents.MobileConsentSdk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.UUID

internal class PrivacyPreferencesViewModel(
  mobileConsentSdk: MobileConsentSdk,
  consentSolutionId: UUID,
  localeProvider: LocaleProvider
) : ViewModel(), PrivacyPreferencesListener {

  private var isViewAttached = false
  private var pendingEvent: Event? = null

  sealed class Event {
    data class ConsentsChosen(val consents: Map<UUID, Boolean>) : Event()
    object ReadMore : Event()
    object Dismiss : Event()
  }

  private val presenter = PrivacyPreferencesPresenter().apply {
    initialize(mobileConsentSdk, consentSolutionId, localeProvider, this@PrivacyPreferencesViewModel)
    fetch()
  }

  private val mutableEvents = MutableSharedFlow<Event>()
  val events = mutableEvents.asSharedFlow()

  fun attachView(view: PrivacyPreferencesView) {
    isViewAttached = true
    presenter.attachView(view)
    pendingEvent?.let {
      pendingEvent = null
      viewModelScope.launch { mutableEvents.emit(it) }
    }
  }

  fun detachView() {
    presenter.detachView()
    isViewAttached = false
  }

  override fun onConsentsChosen(consents: Map<UUID, Boolean>) = emitEvent(Event.ConsentsChosen(consents))

  override fun onReadMore() = emitEvent(Event.ReadMore)

  override fun onDismissed() = emitEvent(Event.Dismiss)

  private fun emitEvent(event: Event) {
    if (isViewAttached) {
      viewModelScope.launch { mutableEvents.emit(event) }
    } else {
      pendingEvent = event
    }
  }

  override fun onCleared() {
    pendingEvent = null
    presenter.dispose()
    super.onCleared()
  }

  class Factory(
    private val mobileConsentSdk: MobileConsentSdk,
    private val consentSolutionId: UUID,
    private val localeProvider: LocaleProvider
  ) : androidx.lifecycle.ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
      @Suppress("UNCHECKED_CAST")
      return PrivacyPreferencesViewModel(mobileConsentSdk, consentSolutionId, localeProvider) as T
    }
  }
}
