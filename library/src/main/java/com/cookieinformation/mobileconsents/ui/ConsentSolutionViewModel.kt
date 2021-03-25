package com.cookieinformation.mobileconsents.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookieinformation.mobileconsents.MobileConsentSdk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.UUID

internal open class ConsentSolutionViewModel<ViewType, PresenterType>(
  private val presenter: PresenterType,
  mobileConsentSdk: MobileConsentSdk,
  consentSolutionId: UUID,
  localeProvider: LocaleProvider
) : ViewModel(), ConsentSolutionListener where ViewType : ConsentSolutionView<*, *>,
                                               PresenterType : ConsentSolutionPresenter<ViewType, *, *> {

  private var isViewAttached = false
  private var pendingEvent: Event? = null

  sealed class Event {
    data class ConsentsChosen(val consents: Map<UUID, Boolean>) : Event()
    object ReadMore : Event()
    object Dismiss : Event()
  }

  private val mutableEvents = MutableSharedFlow<Event>()
  val events = mutableEvents.asSharedFlow()

  init {
    presenter.apply {
      initialize(mobileConsentSdk, consentSolutionId, localeProvider, this@ConsentSolutionViewModel)
      fetch()
    }
  }

  fun attachView(view: ViewType) {
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
}
