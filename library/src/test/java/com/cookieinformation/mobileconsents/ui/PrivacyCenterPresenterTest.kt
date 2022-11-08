package com.cookieinformation.mobileconsents.ui

import com.cookieinformation.mobileconsents.Consent
import com.cookieinformation.mobileconsents.MobileConsentSdk
import com.cookieinformation.mobileconsents.ui.PrivacyCenterItem.PrivacyCenterDetailsItem
import com.cookieinformation.mobileconsents.ui.PrivacyCenterItem.PrivacyCenterInfoItem
import com.cookieinformation.mobileconsents.ui.PrivacyCenterItem.PrivacyCenterPreferencesItem
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

internal class PrivacyCenterPresenterTest : DescribeSpec({

  lateinit var sdk: MobileConsentSdk

  lateinit var view: PrivacyCenterView

  lateinit var localeProvider: LocaleProvider

  lateinit var listener: ConsentSolutionListener

  lateinit var presenter: PrivacyCenterPresenter

  lateinit var saveConsentsFlow: MutableSharedFlow<Map<UUID, Boolean>>

  val sampleConsentSolution = createConsentSolution(
    listOf(sampleRequiredConsentItem, sampleOptionalConsentItem, sampleInfoConsentItem)
  )
  val consentSolutionId = sampleConsentSolution.consentSolutionId

  val initDefault: PrivacyCenterPresenter.() -> Unit = {
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

    presenter = PrivacyCenterPresenter(dispatcher = Dispatchers.Unconfined)
  }

  describe("PrivacyCenterPresenterTest: view data") {

    it("when no consent is saved, expect correct data (without required consent)") {
      coEvery { sdk.getSavedConsents() } returns emptyMap()
      coEvery { sdk.fetchConsentSolution(consentSolutionId) } returns createConsentSolution(
        listOf(sampleOptionalConsentItem, sampleInfoConsentItem)
      )

      val viewDataSlot = slot<PrivacyCenterViewData>()
      every { view.showViewData(capture(viewDataSlot)) } returns Unit

      presenter.initDefault()

      val expectedViewData = createViewData(
        items = listOf(
          sampleInfoItem(false),
          sampleInfoPreferencesItem(true),
          samplePreferencesItem(listOf(sampleOptionalItem(false)))
        ),
        enabledAccept = true,
      )

      viewDataSlot.captured shouldBe expectedViewData
    }

    it("when no consent is saved, expect correct data (with required consent)") {
      coEvery { sdk.getSavedConsents() } returns emptyMap()
      coEvery { sdk.fetchConsentSolution(consentSolutionId) } returns createConsentSolution(
        listOf(sampleRequiredConsentItem, sampleOptionalConsentItem, sampleInfoConsentItem)
      )

      val viewDataSlot = slot<PrivacyCenterViewData>()
      every { view.showViewData(capture(viewDataSlot)) } returns Unit

      presenter.initDefault()

      val expectedViewData = createViewData(
        items = listOf(
          sampleInfoItem(false),
          sampleInfoPreferencesItem(true),
          samplePreferencesItem(listOf(sampleRequiredItem(false), sampleOptionalItem(false)))
        ),
        enabledAccept = false,
      )

      viewDataSlot.captured shouldBe expectedViewData
    }

    it("when consents are saved, expect correct data (with required consent)") {
      coEvery { sdk.getSavedConsents() } returns mapOf(sampleRequiredConsentItem.consentItemId to true)
      coEvery { sdk.fetchConsentSolution(consentSolutionId) } returns createConsentSolution(
        listOf(sampleRequiredConsentItem, sampleOptionalConsentItem, sampleInfoConsentItem)
      )

      val viewDataSlot = slot<PrivacyCenterViewData>()
      every { view.showViewData(capture(viewDataSlot)) } returns Unit

      presenter.initDefault()

      val expectedViewData = createViewData(
        items = listOf(
          sampleInfoItem(false),
          sampleInfoPreferencesItem(true),
          samplePreferencesItem(listOf(sampleRequiredItem(true), sampleOptionalItem(false)))
        ),
        enabledAccept = true,
      )

      viewDataSlot.captured shouldBe expectedViewData
    }

    describe("PrivacyCenterPresenterTest: user choice") {

      it("when user checks an item, expect correct view data") {
        coEvery { sdk.getSavedConsents() } returns emptyMap()
        coEvery { sdk.fetchConsentSolution(consentSolutionId) } returns createConsentSolution(
          listOf(sampleRequiredConsentItem, sampleOptionalConsentItem, sampleInfoConsentItem)
        )

        val viewDataSlot = slot<PrivacyCenterViewData>()
        every { view.showViewData(capture(viewDataSlot)) } returns Unit

        presenter.initDefault()
        presenter.onPrivacyCenterChoiceChanged(sampleOptionalConsentItem.consentItemId, true)

        val expectedViewData = createViewData(
          items = listOf(
            sampleInfoItem(false),
            sampleInfoPreferencesItem(true),
            samplePreferencesItem(listOf(sampleRequiredItem(false), sampleOptionalItem(true)))
          ),
          enabledAccept = false,
        )

        viewDataSlot.captured shouldBe expectedViewData
      }
    }

    describe("PrivacyCenterPresenterTest: expand details") {

      it("when user clicks collapsed item then the same one again, expect details are shown then hidden") {
        coEvery { sdk.getSavedConsents() } returns emptyMap()
        coEvery { sdk.fetchConsentSolution(consentSolutionId) } returns createConsentSolution(
          listOf(sampleRequiredConsentItem, sampleOptionalConsentItem, sampleInfoConsentItem)
        )

        val viewDataSlot = slot<PrivacyCenterViewData>()
        every { view.showViewData(capture(viewDataSlot)) } returns Unit

        presenter.initDefault()

        val expectedViewDataExpanded = createViewData(
          items = listOf(
            sampleInfoItem(true),
            sampleDetailsItem,
            sampleInfoPreferencesItem(true),
            samplePreferencesItem(listOf(sampleRequiredItem(false), sampleOptionalItem(false)))
          ),
          enabledAccept = false,
        )
        val expectedViewDataCollapsed = createViewData(
          items = listOf(
            sampleInfoItem(false),
            sampleInfoPreferencesItem(true),
            samplePreferencesItem(listOf(sampleRequiredItem(false), sampleOptionalItem(false)))
          ),
          enabledAccept = false,
        )

        presenter.onPrivacyCenterDetailsToggle(sampleInfoConsentItem.consentItemId)
        viewDataSlot.captured shouldBe expectedViewDataExpanded

        presenter.onPrivacyCenterDetailsToggle(sampleInfoConsentItem.consentItemId)
        viewDataSlot.captured shouldBe expectedViewDataCollapsed
      }

      it("when user clicks expanded prefs. item then the same one again, expect prefs are hidden then shown") {
        coEvery { sdk.getSavedConsents() } returns emptyMap()
        coEvery { sdk.fetchConsentSolution(consentSolutionId) } returns createConsentSolution(
          listOf(sampleRequiredConsentItem, sampleOptionalConsentItem, sampleInfoConsentItem)
        )

        val viewDataSlot = slot<PrivacyCenterViewData>()
        every { view.showViewData(capture(viewDataSlot)) } returns Unit

        presenter.initDefault()

        val expectedViewDataCollapsed = createViewData(
          items = listOf(
            sampleInfoItem(false),
            sampleInfoPreferencesItem(false),
          ),
          enabledAccept = false,
        )
        val expectedViewDataExpanded = createViewData(
          items = listOf(
            sampleInfoItem(false),
            sampleInfoPreferencesItem(true),
            samplePreferencesItem(listOf(sampleRequiredItem(false), sampleOptionalItem(false)))
          ),
          enabledAccept = false,
        )

        presenter.onPrivacyCenterDetailsToggle(preferencesId)
        viewDataSlot.captured shouldBe expectedViewDataCollapsed

        presenter.onPrivacyCenterDetailsToggle(preferencesId)
        viewDataSlot.captured shouldBe expectedViewDataExpanded
      }
    }

    describe("PrivacyCenterPresenterTest: buttons actions") {

      it("when user clicks back button, expect no changes and onDismissed is called") {
        coEvery { sdk.getSavedConsents() } returns emptyMap()
        coEvery { sdk.fetchConsentSolution(consentSolutionId) } returns createConsentSolution(
          listOf(sampleRequiredConsentItem, sampleOptionalConsentItem, sampleInfoConsentItem)
        )
        coEvery { sdk.postConsent(any()) } returns Unit

        val viewDataSlot = slot<PrivacyCenterViewData>()
        every { view.showViewData(capture(viewDataSlot)) } returns Unit

        presenter.initDefault()
        presenter.onPrivacyCenterDismissRequest()

        val expectedViewData = createViewData(
          items = listOf(
            sampleInfoItem(false),
            sampleInfoPreferencesItem(true),
            samplePreferencesItem(listOf(sampleRequiredItem(false), sampleOptionalItem(false)))
          ),
          enabledAccept = false,
        )
        viewDataSlot.captured shouldBe expectedViewData
        verify(exactly = 0) { listener.onConsentsChosen(any(), any(), false) }
        verify(exactly = 1) { listener.onDismissed() }
        verify(exactly = 0) { listener.onReadMore() }
      }

      it("when user clicks accept button, expect no changes and onConsentsChosen is called") {
        coEvery { sdk.getSavedConsents() } returns mapOf(sampleRequiredConsentItem.consentItemId to true)
        coEvery { sdk.fetchConsentSolution(consentSolutionId) } returns createConsentSolution(
          listOf(sampleRequiredConsentItem, sampleOptionalConsentItem, sampleInfoConsentItem)
        )
        coEvery { sdk.postConsent(any()) } returns Unit

        val viewDataSlot = slot<PrivacyCenterViewData>()
        every { view.showViewData(capture(viewDataSlot)) } returns Unit

        presenter.initDefault()
        presenter.onPrivacyCenterAcceptClicked()

        val expectedViewData = createViewData(
          items = listOf(
            sampleInfoItem(false),
            sampleInfoPreferencesItem(true),
            samplePreferencesItem(listOf(sampleRequiredItem(true), sampleOptionalItem(false)))
          ),
          enabledAccept = true,
        )
        viewDataSlot.captured shouldBe expectedViewData
        verify(exactly = 1) { listener.onConsentsChosen(any(), any(), false) }
        verify(exactly = 0) { listener.onDismissed() }
        verify(exactly = 0) { listener.onReadMore() }
      }
    }
  }

  describe("PrivacyCenterPresenterTest: send") {

    it("when sdk's postConsent satisfy, onConsentsChosen is called with correct data") {
      coEvery { sdk.getSavedConsents() } returns mapOf(
        sampleRequiredConsentItem.consentItemId to true,
        sampleOptionalConsentItem.consentItemId to true,
      )
      coEvery { sdk.fetchConsentSolution(consentSolutionId) } returns createConsentSolution(
        listOf(sampleRequiredConsentItem, sampleOptionalConsentItem, sampleInfoConsentItem)
      )
      val consentsSlot = slot<Consent>()
      coEvery { sdk.postConsent(capture(consentsSlot)) } returns Unit

      presenter.initDefault()
      presenter.onPrivacyCenterAcceptClicked()

      consentsSlot.captured shouldBe sampleConsent(requiredChosen = true, optionalChosen = true)
    }
  }

  describe("ConsentSolutionPresenter: consents changed externally") {

    it("when external save is triggered while has fetched consents, expect update data") {
      val consentSolution = createConsentSolution(
        listOf(sampleRequiredConsentItem, sampleOptionalConsentItem, sampleInfoConsentItem)
      )
      coEvery { sdk.getSavedConsents() } returns emptyMap()
      coEvery { sdk.fetchConsentSolution(consentSolutionId) } returns consentSolution

      val viewDataSlot = slot<PrivacyCenterViewData>()
      every { view.showViewData(capture(viewDataSlot)) } returns Unit

      presenter.initDefault()

      saveConsentsFlow.emit(mapOf(sampleRequiredConsentItem.consentItemId to true))

      val expectedViewData = createViewData(
        items = listOf(
          sampleInfoItem(false),
          sampleInfoPreferencesItem(true),
          samplePreferencesItem(listOf(sampleRequiredItem(true), sampleOptionalItem(false)))
        ),
        enabledAccept = true,
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

      val viewDataSlot = slot<PrivacyCenterViewData>()
      every { view.showViewData(capture(viewDataSlot)) } returns Unit

      presenter.initDefault()
      presenter.sendConsent()

      saveConsentsFlow.emit(mapOf(sampleOptionalConsentItem.consentItemId to true))

      val expectedViewData = createViewData(
        items = listOf(
          sampleInfoItem(false),
          sampleInfoPreferencesItem(true),
          samplePreferencesItem(listOf(sampleRequiredItem(false), sampleOptionalItem(true)))
        ),
        enabledAccept = false,
      )

      viewDataSlot.captured shouldBe expectedViewData
    }
  }
})

private val preferencesId = UUID(0, 0)

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

private fun createViewData(items: List<PrivacyCenterItem>, enabledAccept: Boolean) =
  PrivacyCenterViewData(
    title = "privacyCenterTitle",
    items = items,
    acceptButtonText = "savePreferencesButton",
    acceptButtonEnabled = enabledAccept,
  )

private fun sampleInfoItem(expanded: Boolean) = PrivacyCenterInfoItem(
  id = UUID.fromString("1d5920c7-c5d1-4c08-93cc-4238457d7a1f"),
  text = "Info",
  details = "Information",
  language = "EN",
  expanded = expanded
)

private val sampleDetailsItem = PrivacyCenterDetailsItem(
  id = UUID.fromString("1d5920c7-c5d1-4c08-93cc-4238457d7a1f"),
  details = "Information",
)

private fun sampleInfoPreferencesItem(expanded: Boolean) =
  PrivacyCenterInfoItem(
    id = preferencesId,
    text = "privacyPreferencesTabLabel",
    details = "",
    language = "EN",
    expanded = expanded
  )

private fun samplePreferencesItem(preferencesItems: List<PrivacyPreferencesItem>) =
  PrivacyCenterPreferencesItem(
    id = preferencesId,
    title = "consentPreferencesLabel",
    subTitle = "<a href=\"https://cookieinformation.com\">poweredByLabel</a>",
    items = preferencesItems,
  )
