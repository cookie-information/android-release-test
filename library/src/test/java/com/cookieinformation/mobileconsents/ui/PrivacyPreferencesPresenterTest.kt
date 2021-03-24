package com.cookieinformation.mobileconsents.ui

import com.cookieinformation.mobileconsents.ConsentItem
import com.cookieinformation.mobileconsents.ConsentItem.Type.Info
import com.cookieinformation.mobileconsents.ConsentItem.Type.Setting
import com.cookieinformation.mobileconsents.ConsentSolution
import com.cookieinformation.mobileconsents.MobileConsentSdk
import com.cookieinformation.mobileconsents.TextTranslation
import com.cookieinformation.mobileconsents.UiTexts
import com.cookieinformation.mobileconsents.ui.PrivacyPreferencesView.ButtonId.AcceptAll
import com.cookieinformation.mobileconsents.ui.PrivacyPreferencesView.ButtonId.AcceptSelected
import com.cookieinformation.mobileconsents.ui.PrivacyPreferencesView.ButtonId.ReadMore
import com.cookieinformation.mobileconsents.ui.PrivacyPreferencesView.ButtonId.RejectAll
import com.cookieinformation.mobileconsents.ui.PrivacyPreferencesViewData.ButtonState
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import java.io.IOException
import java.util.Locale
import java.util.UUID

internal class PrivacyPreferencesPresenterTest : DescribeSpec({

  lateinit var sdk: MobileConsentSdk

  lateinit var view: PrivacyPreferencesView

  lateinit var localeProvider: LocaleProvider

  lateinit var listener: PrivacyPreferencesListener

  lateinit var presenter: PrivacyPreferencesPresenter

  val sampleConsentSolution = createConsentSolution(
    listOf(sampleRequiredConsentItem, sampleOptionalConsentItem, sampleInfoConsentItem)
  )
  val consentSolutionId = sampleConsentSolution.consentSolutionId

  val initDefault: PrivacyPreferencesPresenter.() -> Unit = {
    initialize(sdk, consentSolutionId, localeProvider, listener)
    attachView(view)
    fetch()
  }

  beforeTest {
    sdk = mockk()
    view = mockk(relaxed = true)
    localeProvider = mockk()
    every { localeProvider.getLocales() } returns listOf(Locale("en"))
    listener = mockk(relaxed = true)

    presenter = PrivacyPreferencesPresenter(dispatcher = Dispatchers.Unconfined)
  }

  describe("PrivacyPreferencesPresenter: attach/detach view") {

    it("when attachView and detachView is called, expect add and remove view listener") {
      presenter.attachView(view)
      presenter.detachView()

      verify(exactly = 1) { view.addIntentListener(presenter) }
      verify(exactly = 1) { view.removeIntentListener(presenter) }
    }
  }

  describe("PrivacyPreferencesPresenter: fetch") {

    it("when sdk's getSavedConsents and fetchConsentSolution satisfy, expect view shows loading then data") {
      coEvery { sdk.getSavedConsents() } returns emptyMap()
      coEvery { sdk.fetchConsentSolution(consentSolutionId) } returns sampleConsentSolution

      presenter.initDefault()

      coVerify(exactly = 1) { sdk.getSavedConsents() }
      coVerify(exactly = 1) { sdk.fetchConsentSolution(consentSolutionId) }
      verify(exactly = 1) { view.showProgressBar() }
      verify(exactly = 1) { view.hideViewData() }
      verify(exactly = 1) { view.hideProgressBar() }
      verify(exactly = 1) { view.showViewData(any()) }
      verify(exactly = 0) { view.showRetryDialog(any(), any()) }
      verify(exactly = 0) { view.showErrorDialog(any()) }
    }

    it("when sdk's getSavedConsents fails and fetchConsentSolution satisfies, expect view shows loading then error") {
      coEvery { sdk.getSavedConsents() } throws IOException()
      coEvery { sdk.fetchConsentSolution(consentSolutionId) } returns sampleConsentSolution

      presenter.initDefault()

      coVerify(exactly = 1) { sdk.getSavedConsents() }
      coVerify(exactly = 0) { sdk.fetchConsentSolution(consentSolutionId) }
      verify(exactly = 1) { view.showProgressBar() }
      verify(exactly = 2) { view.hideViewData() }
      verify(exactly = 1) { view.hideProgressBar() }
      verify(exactly = 1) { view.showRetryDialog(any(), any()) }
      verify(exactly = 0) { view.showViewData(any()) }
      verify(exactly = 0) { view.showErrorDialog(any()) }
    }

    it("when sdk's getSavedConsents satisfies and fetchConsentSolution fails, expect view shows loading then error") {
      coEvery { sdk.getSavedConsents() } returns emptyMap()
      coEvery { sdk.fetchConsentSolution(consentSolutionId) } throws IOException()

      presenter.initDefault()

      coVerify(exactly = 1) { sdk.getSavedConsents() }
      coVerify(exactly = 1) { sdk.fetchConsentSolution(consentSolutionId) }
      verify(exactly = 1) { view.showProgressBar() }
      verify(exactly = 2) { view.hideViewData() }
      verify(exactly = 1) { view.hideProgressBar() }
      verify(exactly = 1) { view.showRetryDialog(any(), any()) }
      verify(exactly = 0) { view.showViewData(any()) }
      verify(exactly = 0) { view.showErrorDialog(any()) }
    }

    it("when sdk's getSavedConsents and fetchConsentSolution satisfy, attach view after fetch") {
      coEvery { sdk.getSavedConsents() } returns emptyMap()
      coEvery { sdk.fetchConsentSolution(consentSolutionId) } returns sampleConsentSolution

      presenter.initialize(sdk, consentSolutionId, localeProvider, listener)
      presenter.fetch()
      presenter.attachView(view)

      coVerify(exactly = 1) { sdk.getSavedConsents() }
      coVerify(exactly = 1) { sdk.fetchConsentSolution(consentSolutionId) }
      verify(exactly = 0) { view.showProgressBar() }
      verify(exactly = 0) { view.hideViewData() }
      verify(exactly = 1) { view.hideProgressBar() }
      verify(exactly = 0) { view.showRetryDialog(any(), any()) }
      verify(exactly = 1) { view.showViewData(any()) }
      verify(exactly = 0) { view.showErrorDialog(any()) }
    }
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
      verify(exactly = 0) { listener.onConsentsChosen(any()) }
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
      every { listener.onConsentsChosen(capture(consentsChosenSlot)) } returns Unit

      presenter.initDefault()
      presenter.onPrivacyPreferenceButtonClicked(AcceptAll)

      val expectedViewData = createViewData(
        items = listOf(sampleRequiredItem(true), sampleOptionalItem(true)),
        enabledRejectAll = false,
        enabledAcceptSelected = true,
      )

      viewDataSlot.captured shouldBe expectedViewData
      consentsChosenSlot.captured shouldBe userChoiceMap(requiredChosen = true, opticalChosen = true)
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
      every { listener.onConsentsChosen(capture(consentsChosenSlot)) } returns Unit

      presenter.initDefault()
      presenter.onPrivacyPreferenceButtonClicked(RejectAll)

      val expectedViewData = createViewData(
        items = listOf(sampleOptionalItem(false)),
        enabledRejectAll = true,
        enabledAcceptSelected = true,
      )

      viewDataSlot.captured shouldBe expectedViewData
      consentsChosenSlot.captured shouldBe userChoiceMap(opticalChosen = false)
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
      every { listener.onConsentsChosen(capture(consentsChosenSlot)) } returns Unit

      presenter.initDefault()
      presenter.onPrivacyPreferenceButtonClicked(AcceptSelected)

      val expectedViewData = createViewData(
        items = listOf(sampleRequiredItem(true), sampleOptionalItem(false)),
        enabledRejectAll = false,
        enabledAcceptSelected = true,
      )

      viewDataSlot.captured shouldBe expectedViewData
      consentsChosenSlot.captured shouldBe userChoiceMap(requiredChosen = true, opticalChosen = false)
      verify(exactly = 0) { listener.onReadMore() }
    }
  }

  describe("PrivacyPreferencesPresenter: send") {

    it("when sdk's postConsent satisfy, onConsentsChosen is called") {
      coEvery { sdk.getSavedConsents() } returns mapOf(sampleRequiredConsentItem.consentItemId to true)
      coEvery { sdk.fetchConsentSolution(consentSolutionId) } returns createConsentSolution(
        listOf(sampleRequiredConsentItem, sampleOptionalConsentItem, sampleInfoConsentItem)
      )
      coEvery { sdk.postConsent(any()) } returns Unit

      val viewDataSlot = slot<PrivacyPreferencesViewData>()
      every { view.showViewData(capture(viewDataSlot)) } returns Unit

      presenter.initialize(sdk, consentSolutionId, localeProvider, listener)
      presenter.fetch()
      presenter.attachView(view)
      presenter.onPrivacyPreferenceButtonClicked(AcceptSelected)

      val expectedViewData = createViewData(
        items = listOf(sampleRequiredItem(true), sampleOptionalItem(false)),
        enabledRejectAll = false,
        enabledAcceptSelected = true,
      )

      viewDataSlot.captured shouldBe expectedViewData

      verify(exactly = 1) { view.showProgressBar() }
      verify(exactly = 0) { view.hideViewData() }
      verify(exactly = 1) { view.hideProgressBar() }
      verify(exactly = 0) { view.showRetryDialog(any(), any()) }
      verify(exactly = 2) { view.showViewData(any()) }
      verify(exactly = 0) { view.showErrorDialog(any()) }

      verify(exactly = 1) { listener.onConsentsChosen(any()) }
      verify(exactly = 0) { listener.onReadMore() }
    }

    it("when sdk's postConsent fails, view show error dialog") {
      coEvery { sdk.getSavedConsents() } returns mapOf(sampleRequiredConsentItem.consentItemId to true)
      coEvery { sdk.fetchConsentSolution(consentSolutionId) } returns createConsentSolution(
        listOf(sampleRequiredConsentItem, sampleOptionalConsentItem, sampleInfoConsentItem)
      )
      coEvery { sdk.postConsent(any()) } throws IOException()

      val viewDataSlot = slot<PrivacyPreferencesViewData>()
      every { view.showViewData(capture(viewDataSlot)) } returns Unit

      presenter.initialize(sdk, consentSolutionId, localeProvider, listener)
      presenter.fetch()
      presenter.attachView(view)
      presenter.onPrivacyPreferenceButtonClicked(AcceptSelected)

      val expectedViewData = createViewData(
        items = listOf(sampleRequiredItem(true), sampleOptionalItem(false)),
        enabledRejectAll = false,
        enabledAcceptSelected = true,
      )

      viewDataSlot.captured shouldBe expectedViewData

      verify(exactly = 1) { view.showProgressBar() }
      verify(exactly = 0) { view.hideViewData() }
      verify(exactly = 2) { view.hideProgressBar() }
      verify(exactly = 0) { view.showRetryDialog(any(), any()) }
      verify(exactly = 3) { view.showViewData(any()) }
      verify(exactly = 1) { view.showErrorDialog(any()) }

      verify(exactly = 0) { listener.onConsentsChosen(any()) }
      verify(exactly = 0) { listener.onReadMore() }
    }
  }
})

private val sampleRequiredConsentItem = ConsentItem(
  consentItemId = UUID.fromString("a10853b5-85b8-4541-a9ab-fd203176bdce"),
  shortText = listOf(TextTranslation("EN", "Required consent")),
  longText = listOf(TextTranslation("EN", "Required consent description")),
  required = true,
  type = Setting
)

private val sampleOptionalConsentItem = ConsentItem(
  consentItemId = UUID.fromString("ef7d8f35-fc1a-4369-ada2-c00cc0eecc4b"),
  shortText = listOf(TextTranslation("EN", "Optional consent")),
  longText = listOf(TextTranslation("EN", "Optional consent description")),
  required = false,
  type = Setting
)

private val sampleInfoConsentItem = ConsentItem(
  consentItemId = UUID.fromString("1d5920c7-c5d1-4c08-93cc-4238457d7a1f"),
  shortText = listOf(TextTranslation("EN", "Info")),
  longText = listOf(TextTranslation("EN", "Information")),
  required = true,
  type = Info
)

private val sampleUiTexts = UiTexts(
  privacyPreferencesTitle = listOf(TextTranslation("EN", "privacyPreferencesTitle")),
  privacyPreferencesDescription = listOf(TextTranslation("EN", "privacyPreferencesDescription")),
  privacyPreferencesTabLabel = listOf(TextTranslation("EN", "privacyPreferencesTabLabel")),
  privacyCenterButton = listOf(TextTranslation("EN", "privacyCenterButton")),
  acceptAllButton = listOf(TextTranslation("EN", "acceptAllButton")),
  rejectAllButton = listOf(TextTranslation("EN", "rejectAllButton")),
  acceptSelectedButton = listOf(TextTranslation("EN", "acceptSelectedButton")),
  savePreferencesButton = listOf(TextTranslation("EN", "savePreferencesButton")),
  privacyCenterTitle = listOf(TextTranslation("EN", "privacyCenterTitle")),
  poweredByLabel = listOf(TextTranslation("EN", "poweredByLabel")),
  consentPreferencesLabel = listOf(TextTranslation("EN", "consentPreferencesLabel")),
)

private fun createConsentSolution(consentItems: List<ConsentItem>): ConsentSolution =
  ConsentSolution(
    consentItems = consentItems,
    consentSolutionId = UUID.fromString("843ddd4a-3eae-4286-a17b-0e8d3337e767"),
    consentSolutionVersionId = UUID.fromString("00000000-0000-0000-0000-000000000000"),
    uiTexts = sampleUiTexts
  )

private fun sampleRequiredItem(checked: Boolean) = PrivacyPreferencesItem(
  id = UUID.fromString("a10853b5-85b8-4541-a9ab-fd203176bdce"),
  required = true,
  accepted = checked,
  text = "Required consent",
  details = "",
  language = "EN",
)

private fun sampleOptionalItem(checked: Boolean) = PrivacyPreferencesItem(
  id = UUID.fromString("ef7d8f35-fc1a-4369-ada2-c00cc0eecc4b"),
  required = false,
  accepted = checked,
  text = "Optional consent",
  details = "",
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

private fun userChoiceMap(requiredChosen: Boolean, opticalChosen: Boolean): Map<UUID, Boolean> = mapOf(
  sampleRequiredConsentItem.consentItemId to requiredChosen,
  sampleOptionalConsentItem.consentItemId to opticalChosen,
  sampleInfoConsentItem.consentItemId to true
)

private fun userChoiceMap(opticalChosen: Boolean): Map<UUID, Boolean> = mapOf(
  sampleOptionalConsentItem.consentItemId to opticalChosen,
  sampleInfoConsentItem.consentItemId to true
)
