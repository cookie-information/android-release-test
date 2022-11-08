package com.cookieinformation.mobileconsents.ui

import com.cookieinformation.mobileconsents.Consent
import com.cookieinformation.mobileconsents.MobileConsentSdk
import com.cookieinformation.mobileconsents.ui.PrivacyPreferencesView.ButtonId.AcceptAll
import com.cookieinformation.mobileconsents.ui.PrivacyPreferencesView.ButtonId.AcceptSelected
import com.cookieinformation.mobileconsents.ui.PrivacyPreferencesView.ButtonId.ReadMore
import com.cookieinformation.mobileconsents.ui.PrivacyPreferencesView.ButtonId.RejectAll
import com.cookieinformation.mobileconsents.ui.PrivacyPreferencesViewData.ButtonState
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import java.io.IOException
import java.util.Locale
import java.util.UUID

internal class PrivacyPreferencesPresenterTest : DescribeSpec({

  lateinit var sdk: MobileConsentSdk

  lateinit var view: PrivacyPreferencesView

  lateinit var localeProvider: LocaleProvider

  lateinit var listener: ConsentSolutionListener

  lateinit var presenter: PrivacyPreferencesPresenter

  lateinit var saveConsentsFlow: MutableSharedFlow<Map<UUID, Boolean>>

  val sampleConsentSolution = createConsentSolution(
    listOf(sampleRequiredConsentItem, sampleOptionalConsentItem, sampleInfoConsentItem)
  )
  val consentSolutionId = sampleConsentSolution.consentSolutionId

  val initDefault: PrivacyPreferencesPresenter.() -> Unit = {
    initialize(sdk, consentSolutionId, localeProvider, listener)
    attachView(view)
    fetchConsentSolution()
  }

  beforeTest {
    sdk = mockk()
    view = mockk(relaxed = true)
    localeProvider = mockk()
    every { localeProvider.getLocales() } returns listOf(Locale("en"))
    listener = mockk(relaxed = true)

    saveConsentsFlow = MutableSharedFlow()
    every { sdk.saveConsentsFlow } returns saveConsentsFlow

    presenter = PrivacyPreferencesPresenter(dispatcher = Dispatchers.Unconfined)
  }

  describe("PrivacyPreferencesPresenter: view data") {

    it("when no consent is saved, expect correct data and no item checked") {
      coEvery { sdk.getSavedConsents() } returns emptyMap()
      coEvery { sdk.fetchConsentSolution(consentSolutionId) } returns createConsentSolution(
        listOf(sampleOptionalConsentItem, sampleInfoConsentItem)
      )

      val viewDataSlot = slot<PrivacyPreferencesViewData>()
      every { view.showViewData(capture(viewDataSlot)) } returns Unit

      presenter.initDefault()

      val expectedViewData = createViewData(
        items = listOf(sampleOptionalItem(false)),
        enabledRejectAll = true,
        enabledAcceptSelected = true,
      )

      viewDataSlot.captured shouldBe expectedViewData
    }

    it("when consent is saved, expect correct data and item checked") {
      coEvery { sdk.getSavedConsents() } returns mapOf(sampleOptionalConsentItem.consentItemId to true)
      coEvery { sdk.fetchConsentSolution(consentSolutionId) } returns createConsentSolution(
        listOf(sampleOptionalConsentItem, sampleInfoConsentItem)
      )

      val viewDataSlot = slot<PrivacyPreferencesViewData>()
      every { view.showViewData(capture(viewDataSlot)) } returns Unit

      presenter.initDefault()

      val expectedViewData = createViewData(
        items = listOf(sampleOptionalItem(true)),
        enabledRejectAll = true,
        enabledAcceptSelected = true,
      )

      viewDataSlot.captured shouldBe expectedViewData
    }

    it("when there is not checked required consent, expect RejectAll and AcceptSelected buttons are disabled") {
      coEvery { sdk.getSavedConsents() } returns emptyMap()
      coEvery { sdk.fetchConsentSolution(consentSolutionId) } returns createConsentSolution(
        listOf(sampleRequiredConsentItem, sampleOptionalConsentItem, sampleInfoConsentItem)
      )

      val viewDataSlot = slot<PrivacyPreferencesViewData>()
      every { view.showViewData(capture(viewDataSlot)) } returns Unit

      presenter.initDefault()

      val expectedViewData = createViewData(
        items = listOf(sampleRequiredItem(false), sampleOptionalItem(false)),
        enabledRejectAll = false,
        enabledAcceptSelected = false,
      )

      viewDataSlot.captured shouldBe expectedViewData
    }

    it("when there is checked required consent, expect RejectAll button is disabled") {
      coEvery { sdk.getSavedConsents() } returns mapOf(sampleRequiredConsentItem.consentItemId to true)
      coEvery { sdk.fetchConsentSolution(consentSolutionId) } returns createConsentSolution(
        listOf(sampleRequiredConsentItem, sampleOptionalConsentItem, sampleInfoConsentItem)
      )

      val viewDataSlot = slot<PrivacyPreferencesViewData>()
      every { view.showViewData(capture(viewDataSlot)) } returns Unit

      presenter.initDefault()

      val expectedViewData = createViewData(
        items = listOf(sampleRequiredItem(true), sampleOptionalItem(false)),
        enabledRejectAll = false,
        enabledAcceptSelected = true,
      )

      viewDataSlot.captured shouldBe expectedViewData
    }
  }

  describe("PrivacyPreferencesPresenter: user choice") {

    it("when user checks an item, expect correct view data") {
      coEvery { sdk.getSavedConsents() } returns emptyMap()
      coEvery { sdk.fetchConsentSolution(consentSolutionId) } returns createConsentSolution(
        listOf(sampleOptionalConsentItem, sampleInfoConsentItem)
      )

      val viewDataSlot = slot<PrivacyPreferencesViewData>()
      every { view.showViewData(capture(viewDataSlot)) } returns Unit

      presenter.initDefault()
      presenter.onPrivacyPreferenceChoiceChanged(sampleOptionalConsentItem.consentItemId, true)

      val expectedViewData = createViewData(
        items = listOf(sampleOptionalItem(true)),
        enabledRejectAll = true,
        enabledAcceptSelected = true,
      )

      viewDataSlot.captured shouldBe expectedViewData
    }

    it("when user checks required item, expect AcceptSelected button is active") {
      coEvery { sdk.getSavedConsents() } returns emptyMap()
      coEvery { sdk.fetchConsentSolution(consentSolutionId) } returns createConsentSolution(
        listOf(sampleRequiredConsentItem, sampleOptionalConsentItem, sampleInfoConsentItem)
      )

      val viewDataSlot = slot<PrivacyPreferencesViewData>()
      every { view.showViewData(capture(viewDataSlot)) } returns Unit

      presenter.initDefault()
      presenter.onPrivacyPreferenceChoiceChanged(sampleRequiredConsentItem.consentItemId, true)

      val expectedViewData = createViewData(
        items = listOf(sampleRequiredItem(true), sampleOptionalItem(false)),
        enabledRejectAll = false,
        enabledAcceptSelected = true,
      )

      viewDataSlot.captured shouldBe expectedViewData
    }
  }

  describe("PrivacyPreferencesPresenter: buttons actions") {

    it("when ReadMore button is clicked, expect no consent is changed and onReadMoreRequested is called") {
      coEvery { sdk.getSavedConsents() } returns emptyMap()
      coEvery { sdk.fetchConsentSolution(consentSolutionId) } returns createConsentSolution(
        listOf(sampleRequiredConsentItem, sampleOptionalConsentItem, sampleInfoConsentItem)
      )
      coEvery { sdk.postConsent(any()) } returns Unit

      val viewDataSlot = slot<PrivacyPreferencesViewData>()
      every { view.showViewData(capture(viewDataSlot)) } returns Unit

      presenter.initDefault()
      presenter.onPrivacyPreferenceButtonClicked(ReadMore)

      val expectedViewData = createViewData(
        items = listOf(sampleRequiredItem(false), sampleOptionalItem(false)),
        enabledRejectAll = false,
        enabledAcceptSelected = false,
      )

      viewDataSlot.captured shouldBe expectedViewData
      verify(exactly = 0) { listener.onConsentsChosen(any(), any(), false) }
      verify(exactly = 0) { listener.onDismissed() }
      verify(exactly = 1) { listener.onReadMore() }
    }

    it("when AcceptAll button is clicked, expect all item are checked onConsentsChosen is called") {
      coEvery { sdk.getSavedConsents() } returns mapOf(
        sampleRequiredConsentItem.consentItemId to true,
        sampleOptionalConsentItem.consentItemId to true,
        sampleInfoConsentItem.consentItemId to true
      )
      coEvery { sdk.fetchConsentSolution(consentSolutionId) } returns createConsentSolution(
        listOf(sampleRequiredConsentItem, sampleOptionalConsentItem, sampleInfoConsentItem)
      )
      coEvery { sdk.postConsent(any()) } returns Unit

      val viewDataSlot = slot<PrivacyPreferencesViewData>()
      every { view.showViewData(capture(viewDataSlot)) } returns Unit

      val consentsChosenSlot = slot<Map<UUID, Boolean>>()
      every { listener.onConsentsChosen(any(), capture(consentsChosenSlot), false) } returns Unit

      presenter.initDefault()
      presenter.onPrivacyPreferenceButtonClicked(AcceptAll)

      val expectedViewData = createViewData(
        items = listOf(sampleRequiredItem(true), sampleOptionalItem(true)),
        enabledRejectAll = false,
        enabledAcceptSelected = true,
      )

      viewDataSlot.captured shouldBe expectedViewData
      consentsChosenSlot.captured shouldBe userChoiceMap(requiredChosen = true, optionalChosen = true)
      verify(exactly = 1) { listener.onConsentsChosen(any(), any(), false) }
      verify(exactly = 0) { listener.onDismissed() }
      verify(exactly = 0) { listener.onReadMore() }
    }

    it("when RejectAll button is clicked, expect no item is checked and onConsentsChosen is called") {
      coEvery { sdk.getSavedConsents() } returns mapOf(
        sampleOptionalConsentItem.consentItemId to false,
        sampleInfoConsentItem.consentItemId to true
      )
      coEvery { sdk.fetchConsentSolution(consentSolutionId) } returns createConsentSolution(
        listOf(sampleOptionalConsentItem, sampleInfoConsentItem)
      )
      coEvery { sdk.postConsent(any()) } returns Unit

      val viewDataSlot = slot<PrivacyPreferencesViewData>()
      every { view.showViewData(capture(viewDataSlot)) } returns Unit

      val consentsChosenSlot = slot<Map<UUID, Boolean>>()
      every { listener.onConsentsChosen(any(), capture(consentsChosenSlot), false) } returns Unit

      presenter.initDefault()
      presenter.onPrivacyPreferenceButtonClicked(RejectAll)

      val expectedViewData = createViewData(
        items = listOf(sampleOptionalItem(false)),
        enabledRejectAll = true,
        enabledAcceptSelected = true,
      )

      viewDataSlot.captured shouldBe expectedViewData
      consentsChosenSlot.captured shouldBe userChoiceMap(optionalChosen = false)
      verify(exactly = 1) { listener.onConsentsChosen(any(), any(), false) }
      verify(exactly = 0) { listener.onDismissed() }
      verify(exactly = 0) { listener.onReadMore() }
    }

    it("when AcceptSelected button is clicked, expect onConsentChosen is called") {
      coEvery { sdk.getSavedConsents() } returns mapOf(
        sampleRequiredConsentItem.consentItemId to true,
        sampleOptionalConsentItem.consentItemId to false,
        sampleInfoConsentItem.consentItemId to true
      )
      coEvery { sdk.fetchConsentSolution(consentSolutionId) } returns createConsentSolution(
        listOf(sampleRequiredConsentItem, sampleOptionalConsentItem, sampleInfoConsentItem)
      )
      coEvery { sdk.postConsent(any()) } returns Unit

      val viewDataSlot = slot<PrivacyPreferencesViewData>()
      coEvery { view.showViewData(capture(viewDataSlot)) } returns Unit

      val consentsChosenSlot = slot<Map<UUID, Boolean>>()
      every { listener.onConsentsChosen(any(), capture(consentsChosenSlot), false) } returns Unit

      presenter.initDefault()
      presenter.onPrivacyPreferenceButtonClicked(AcceptSelected)

      val expectedViewData = createViewData(
        items = listOf(sampleRequiredItem(true), sampleOptionalItem(false)),
        enabledRejectAll = false,
        enabledAcceptSelected = true,
      )

      viewDataSlot.captured shouldBe expectedViewData
      consentsChosenSlot.captured shouldBe userChoiceMap(requiredChosen = true, optionalChosen = false)
      verify(exactly = 1) { listener.onConsentsChosen(any(), any(), false) }
      verify(exactly = 0) { listener.onDismissed() }
      verify(exactly = 0) { listener.onReadMore() }
    }
  }

  describe("PrivacyPreferencesPresenter: send correct data") {

    it("when sdk's postConsent satisfy, onConsentsChosen is called") {
      coEvery { sdk.getSavedConsents() } returns mapOf(
        sampleRequiredConsentItem.consentItemId to true,
        sampleOptionalConsentItem.consentItemId to false,
      )
      coEvery { sdk.fetchConsentSolution(consentSolutionId) } returns createConsentSolution(
        listOf(sampleRequiredConsentItem, sampleOptionalConsentItem, sampleInfoConsentItem)
      )
      val consentsSlot = slot<Consent>()
      coEvery { sdk.postConsent(capture(consentsSlot)) } returns Unit

      presenter.initDefault()
      presenter.sendConsent()

      consentsSlot.captured shouldBe sampleConsent(requiredChosen = true, optionalChosen = false)
    }
  }

  describe("ConsentSolutionPresenter: consents changed externally") {

    it("when external save is triggered while has fetched consents, expect update data") {
      val consentSolution = createConsentSolution(
        listOf(sampleRequiredConsentItem, sampleOptionalConsentItem, sampleInfoConsentItem)
      )
      coEvery { sdk.getSavedConsents() } returns emptyMap()
      coEvery { sdk.fetchConsentSolution(consentSolutionId) } returns consentSolution

      val viewDataSlot = slot<PrivacyPreferencesViewData>()
      every { view.showViewData(capture(viewDataSlot)) } returns Unit

      presenter.initDefault()

      saveConsentsFlow.emit(mapOf(sampleOptionalConsentItem.consentItemId to true))

      val expectedViewData = createViewData(
        items = listOf(sampleRequiredItem(false), sampleOptionalItem(true)),
        enabledRejectAll = false,
        enabledAcceptSelected = false,
      )

      viewDataSlot.captured shouldBe expectedViewData
    }

    it("when external save is triggered while showing send error, expect update data") {
      val consentSolution = createConsentSolution(
        listOf(sampleRequiredConsentItem, sampleOptionalConsentItem, sampleInfoConsentItem)
      )
      coEvery { sdk.getSavedConsents() } returns emptyMap()
      coEvery { sdk.fetchConsentSolution(consentSolutionId) } returns consentSolution
      coEvery { sdk.postConsent(any()) } throws IOException()

      val viewDataSlot = slot<PrivacyPreferencesViewData>()
      every { view.showViewData(capture(viewDataSlot)) } returns Unit

      presenter.initDefault()
      presenter.sendConsent()

      saveConsentsFlow.emit(mapOf(sampleRequiredConsentItem.consentItemId to true))

      val expectedViewData = createViewData(
        items = listOf(sampleRequiredItem(true), sampleOptionalItem(false)),
        enabledRejectAll = false,
        enabledAcceptSelected = true,
      )

      viewDataSlot.captured shouldBe expectedViewData
    }
  }
})

private fun sampleRequiredItem(checked: Boolean) = PrivacyPreferencesItem(
  id = UUID.fromString("a10853b5-85b8-4541-a9ab-fd203176bdce"),
  required = true,
  accepted = checked,
  text = "Required consent",
  details = "Required consent description",
  language = "EN",
)

private fun sampleOptionalItem(checked: Boolean) = PrivacyPreferencesItem(
  id = UUID.fromString("ef7d8f35-fc1a-4369-ada2-c00cc0eecc4b"),
  required = false,
  accepted = checked,
  text = "Optional consent",
  details = "Optional consent description",
  language = "EN",
)

private fun createViewData(
  items: List<PrivacyPreferencesItem>,
  enabledRejectAll: Boolean,
  enabledAcceptSelected: Boolean,
) =
  PrivacyPreferencesViewData(
    title = "privacyPreferencesTitle",
    subTitle = "<a href=\"https://cookieinformation.com\">poweredByLabel</a>",
    description = "privacyPreferencesDescription",
    items = items,

    buttonReadMore = ButtonState("privacyCenterButton", true),
    buttonAcceptAll = ButtonState("acceptAllButton", true),
    buttonRejectAll = ButtonState("rejectAllButton", enabledRejectAll),
    buttonAcceptSelected = ButtonState("acceptSelectedButton", enabledAcceptSelected),
  )

private fun userChoiceMap(requiredChosen: Boolean, optionalChosen: Boolean): Map<UUID, Boolean> = mapOf(
  sampleRequiredConsentItem.consentItemId to requiredChosen,
  sampleOptionalConsentItem.consentItemId to optionalChosen,
  sampleInfoConsentItem.consentItemId to true
)

private fun userChoiceMap(optionalChosen: Boolean): Map<UUID, Boolean> = mapOf(
  sampleOptionalConsentItem.consentItemId to optionalChosen,
  sampleInfoConsentItem.consentItemId to true
)
