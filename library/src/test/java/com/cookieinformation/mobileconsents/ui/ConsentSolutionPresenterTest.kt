package com.cookieinformation.mobileconsents.ui

import com.cookieinformation.mobileconsents.Consent
import com.cookieinformation.mobileconsents.ConsentItem
import com.cookieinformation.mobileconsents.ConsentItem.Type.Info
import com.cookieinformation.mobileconsents.ConsentItem.Type.Setting
import com.cookieinformation.mobileconsents.ConsentSolution
import com.cookieinformation.mobileconsents.MobileConsentSdk
import com.cookieinformation.mobileconsents.ProcessingPurpose
import com.cookieinformation.mobileconsents.TextTranslation
import com.cookieinformation.mobileconsents.UiTexts
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import java.io.IOException
import java.util.Locale
import java.util.UUID

private data class TestViewData(val name: String)

private interface TestIntentListener

private interface TestConsentSolutionView : ConsentSolutionView<TestViewData, TestIntentListener>

private class TestConsentSolutionPresenter :
  ConsentSolutionPresenter<TestConsentSolutionView, TestViewData, TestIntentListener>(Dispatchers.Unconfined),
  TestIntentListener {

  override fun getViewIntentListener(): TestIntentListener = this

  override fun createViewData(consentSolution: ConsentSolution, savedConsents: Map<UUID, Boolean>): TestViewData =
    TestViewData("TestViewData")

  override fun getGivenConsents(viewData: TestViewData): GivenConsent = emptyMap()
}

internal class ConsentSolutionPresenterTest : DescribeSpec({

  lateinit var sdk: MobileConsentSdk

  lateinit var view: TestConsentSolutionView

  lateinit var localeProvider: LocaleProvider

  lateinit var listener: ConsentSolutionListener

  lateinit var presenter: TestConsentSolutionPresenter

  val sampleConsentSolution = createConsentSolution(
    listOf(sampleRequiredConsentItem, sampleOptionalConsentItem, sampleInfoConsentItem)
  )
  val consentSolutionId = sampleConsentSolution.consentSolutionId

  val initDefault: TestConsentSolutionPresenter.() -> Unit = {
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

    presenter = TestConsentSolutionPresenter()
  }

  describe("ConsentSolutionPresenter: attach/detach view") {

    it("when attachView and detachView is called, expect add and remove view listener") {
      presenter.attachView(view)
      presenter.detachView()

      verify(exactly = 1) { view.addIntentListener(presenter) }
      verify(exactly = 1) { view.removeIntentListener(presenter) }
    }
  }

  describe("ConsentSolutionPresenter: dispose cancels scope") {

    it("when dispose is called, expect fetchConsentSolution is canceled") {
      coEvery { sdk.getSavedConsents() } returns emptyMap()
      coEvery { sdk.fetchConsentSolution(consentSolutionId) } coAnswers {
        delay(Long.MAX_VALUE)
        sampleConsentSolution
      }
      presenter.initialize(sdk, consentSolutionId, localeProvider, listener)
      presenter.fetch()
      presenter.dispose()
    }
  }

  describe("ConsentSolutionPresenter: fetch") {

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

      verify(exactly = 0) { listener.onConsentsChosen(any()) }
      verify(exactly = 0) { listener.onDismissed() }
      verify(exactly = 0) { listener.onReadMore() }
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

      verify(exactly = 0) { listener.onConsentsChosen(any()) }
      verify(exactly = 0) { listener.onDismissed() }
      verify(exactly = 0) { listener.onReadMore() }
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

      verify(exactly = 0) { listener.onConsentsChosen(any()) }
      verify(exactly = 0) { listener.onDismissed() }
      verify(exactly = 0) { listener.onReadMore() }
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

      verify(exactly = 0) { listener.onConsentsChosen(any()) }
      verify(exactly = 0) { listener.onDismissed() }
      verify(exactly = 0) { listener.onReadMore() }
    }
  }

  describe("ConsentSolutionPresenter: send") {

    it("when sdk's postConsent satisfy, onConsentsChosen is called") {
      coEvery { sdk.getSavedConsents() } returns mapOf(sampleRequiredConsentItem.consentItemId to true)
      coEvery { sdk.fetchConsentSolution(consentSolutionId) } returns createConsentSolution(
        listOf(sampleRequiredConsentItem, sampleOptionalConsentItem, sampleInfoConsentItem)
      )
      coEvery { sdk.postConsent(any()) } returns Unit

      presenter.initialize(sdk, consentSolutionId, localeProvider, listener)
      presenter.fetch()
      presenter.attachView(view)
      presenter.send()

      verify(exactly = 1) { view.showProgressBar() }
      verify(exactly = 0) { view.hideViewData() }
      verify(exactly = 1) { view.hideProgressBar() }
      verify(exactly = 0) { view.showRetryDialog(any(), any()) }
      verify(exactly = 2) { view.showViewData(any()) }
      verify(exactly = 0) { view.showErrorDialog(any()) }

      verify(exactly = 1) { listener.onConsentsChosen(any()) }
      verify(exactly = 0) { listener.onDismissed() }
      verify(exactly = 0) { listener.onReadMore() }
    }

    it("when sdk's postConsent fails, view show error dialog") {
      coEvery { sdk.getSavedConsents() } returns mapOf(sampleRequiredConsentItem.consentItemId to true)
      coEvery { sdk.fetchConsentSolution(consentSolutionId) } returns createConsentSolution(
        listOf(sampleRequiredConsentItem, sampleOptionalConsentItem, sampleInfoConsentItem)
      )
      coEvery { sdk.postConsent(any()) } throws IOException()

      presenter.initialize(sdk, consentSolutionId, localeProvider, listener)
      presenter.fetch()
      presenter.attachView(view)
      presenter.send()

      verify(exactly = 1) { view.showProgressBar() }
      verify(exactly = 0) { view.hideViewData() }
      verify(exactly = 2) { view.hideProgressBar() }
      verify(exactly = 0) { view.showRetryDialog(any(), any()) }
      verify(exactly = 3) { view.showViewData(any()) }
      verify(exactly = 1) { view.showErrorDialog(any()) }

      verify(exactly = 0) { listener.onConsentsChosen(any()) }
      verify(exactly = 0) { listener.onDismissed() }
      verify(exactly = 0) { listener.onReadMore() }
    }
  }
})

internal val sampleRequiredConsentItem = ConsentItem(
  consentItemId = UUID.fromString("a10853b5-85b8-4541-a9ab-fd203176bdce"),
  shortText = listOf(TextTranslation("EN", "Required consent")),
  longText = listOf(TextTranslation("EN", "Required consent description")),
  required = true,
  type = Setting
)

internal val sampleOptionalConsentItem = ConsentItem(
  consentItemId = UUID.fromString("ef7d8f35-fc1a-4369-ada2-c00cc0eecc4b"),
  shortText = listOf(TextTranslation("EN", "Optional consent")),
  longText = listOf(TextTranslation("EN", "Optional consent description")),
  required = false,
  type = Setting
)

internal val sampleInfoConsentItem = ConsentItem(
  consentItemId = UUID.fromString("1d5920c7-c5d1-4c08-93cc-4238457d7a1f"),
  shortText = listOf(TextTranslation("EN", "Info")),
  longText = listOf(TextTranslation("EN", "Information")),
  required = true,
  type = Info
)

internal val sampleUiTexts = UiTexts(
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

internal fun createConsentSolution(consentItems: List<ConsentItem>): ConsentSolution =
  ConsentSolution(
    consentItems = consentItems,
    consentSolutionId = UUID.fromString("843ddd4a-3eae-4286-a17b-0e8d3337e767"),
    consentSolutionVersionId = UUID.fromString("00000000-0000-0000-0000-000000000000"),
    uiTexts = sampleUiTexts
  )

internal fun sampleConsent(requiredChosen: Boolean, optionalChosen: Boolean) = Consent(
  consentSolutionId = UUID.fromString("843ddd4a-3eae-4286-a17b-0e8d3337e767"),
  consentSolutionVersionId = UUID.fromString("00000000-0000-0000-0000-000000000000"),
  processingPurposes = sampleProcessingPurposeList(requiredChosen, optionalChosen),
  customData = emptyMap(),
)

internal fun sampleProcessingPurposeList(requiredChosen: Boolean, optionalChosen: Boolean) = listOf(
  ProcessingPurpose(
    consentItemId = sampleRequiredConsentItem.consentItemId,
    consentGiven = requiredChosen,
    language = "EN",
  ),
  ProcessingPurpose(
    consentItemId = sampleOptionalConsentItem.consentItemId,
    consentGiven = optionalChosen,
    language = "EN",
  ),
  ProcessingPurpose(
    consentItemId = sampleInfoConsentItem.consentItemId,
    consentGiven = true,
    language = "EN",
  ),
)
