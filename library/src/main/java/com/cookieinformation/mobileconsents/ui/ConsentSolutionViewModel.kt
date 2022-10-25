package com.cookieinformation.mobileconsents.ui

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookieinformation.mobileconsents.ConsentSolution
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * The generic view moder for [BasePrivacyPreferencesDialogFragment] and [BasePrivacyCenterFragment].
 */
internal open class ConsentSolutionViewModel<ViewType, PresenterType>(
  private val presenter: PresenterType,
  binder: ConsentSolutionBinder
) : ViewModel(), ConsentSolutionListener where ViewType : ConsentSolutionView<*, *>,
                                               PresenterType : ConsentSolutionPresenter<ViewType, *, *> {

  private var isViewAttached = false
  private var pendingEvent: Event? = null

  sealed class Event {

    data class ConsentsChosen(
      val consentSolution: ConsentSolution,
      val consents: Map<UUID, Boolean>,
      val external: Boolean
    ) : Event()

    object ReadMore : Event()

    object Dismiss : Event()
  }

  private val mutableEvents = MutableSharedFlow<Event>()
  val events = mutableEvents.asSharedFlow()

  init {
    presenter.apply {
      initialize(
        binder.mobileConsentSdk,
        binder.consentSolutionId,
        binder.localeProvider,
        this@ConsentSolutionViewModel
      )
      fetch()
      //authenticate()
    }
  }

  /**
   * Attaches the view to view model.
   * The method should be called in [androidx.fragment.app.Fragment.onViewCreated]
   */
  fun attachView(view: ViewType) {
    isViewAttached = true
    presenter.attachView(view)
    pendingEvent?.let {
      pendingEvent = null
      viewModelScope.launch { mutableEvents.emit(it) }
    }
  }

  /**
   * Detaches curently attached view from view model.
   * The method should be called in [androidx.fragment.app.Fragment.onDestroyView]
   */
  fun detachView() {
    presenter.detachView()
    isViewAttached = false
  }

  /**
   * If method is overridden, the super must be called.
   */
  @CallSuper
  override fun onConsentsChosen(consentSolution: ConsentSolution, consents: Map<UUID, Boolean>, external: Boolean) =
    emitEvent(Event.ConsentsChosen(consentSolution, consents, external))

  /**
   * If method is overridden, the super must be called.
   */
  @CallSuper
  override fun onReadMore() = emitEvent(Event.ReadMore)

  /**
   * If method is overridden, the super must be called.
   */
  @CallSuper
  override fun onDismissed() = emitEvent(Event.Dismiss)

  private fun emitEvent(event: Event) {
    if (isViewAttached) {
      viewModelScope.launch { mutableEvents.emit(event) }
    } else {
      pendingEvent = event
    }
  }

  /**
   * If method is overridden, the super must be called.
   */
  @CallSuper
  override fun onCleared() {
    pendingEvent = null
    presenter.dispose()
    super.onCleared()
  }
}
