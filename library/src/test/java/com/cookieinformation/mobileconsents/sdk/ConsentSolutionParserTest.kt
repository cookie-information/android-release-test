package com.cookieinformation.mobileconsents.sdk

import com.cookieinformation.mobileconsents.ConsentItem
import com.cookieinformation.mobileconsents.TextTranslation
import com.cookieinformation.mobileconsents.adapter.moshi
import com.cookieinformation.mobileconsents.networking.response.ConsentSolutionResponseJsonAdapter
import com.cookieinformation.mobileconsents.networking.response.toDomain
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.util.UUID

internal class ConsentSolutionParserTest : DescribeSpec({

  val consentString = javaClass.getResourceAsString("/consent_solution.json")

  val consentSolution = ConsentSolutionResponseJsonAdapter(moshi).fromJson(consentString)!!.toDomain()

  describe("ContentSolutionParser") {

    it("parse consent solution UI text") {
      consentSolution.uiTexts.privacyPreferencesTitle[0] shouldBe TextTranslation("EN", "Privacy settings")
      consentSolution.uiTexts.privacyPreferencesDescription[0] shouldBe TextTranslation(
        "EN",
        "Some are used for statistical purposes and others are set up by third party services."
      )
      consentSolution.uiTexts.privacyCenterButton[0] shouldBe TextTranslation("EN", "Read more")
      consentSolution.uiTexts.rejectAllButton[0] shouldBe TextTranslation("EN", "Reject all")
      consentSolution.uiTexts.acceptAllButton[0] shouldBe TextTranslation("EN", "Accept all")
      consentSolution.uiTexts.acceptSelectedButton[0] shouldBe TextTranslation("EN", "Accept selected")
      consentSolution.uiTexts.savePreferencesButton[0] shouldBe TextTranslation("EN", "Accept")
      consentSolution.uiTexts.privacyCenterTitle[0] shouldBe TextTranslation("EN", "Privacy")
      consentSolution.uiTexts.privacyPreferencesTabLabel[0] shouldBe TextTranslation("EN", "Privacy preferences")
      consentSolution.uiTexts.poweredByLabel[0] shouldBe TextTranslation("EN", "Powered by Cookie Information")
      consentSolution.uiTexts.consentPreferencesLabel[0] shouldBe TextTranslation("EN", "consentPreferences")
    }

    it("parse consent solution items") {
      consentSolution.consentItems.size shouldBe 7

      consentSolution.consentItems[0] shouldBe ConsentItem(
        consentItemId = UUID.fromString("a10853b5-85b8-4541-a9ab-fd203176bdce"),
        shortText = listOf(TextTranslation("EN", "I agree to the Terms of Services.")),
        longText = listOf(TextTranslation("EN", "")),
        required = true,
        type = ConsentItem.Type.Setting
      )

      consentSolution.consentItems[1] shouldBe ConsentItem(
        consentItemId = UUID.fromString("ef7d8f35-fc1a-4369-ada2-c00cc0eecc4b"),
        shortText = listOf(TextTranslation("EN", "I consent to the use of my personal data.")),
        longText = listOf(TextTranslation("EN", "")),
        required = false,
        type = ConsentItem.Type.Setting
      )

      consentSolution.consentItems[6] shouldBe ConsentItem(
        consentItemId = UUID.fromString("99f6f633-7193-4d69-bf8a-759e7cee349a"),
        shortText = listOf(TextTranslation("EN", "Your rights")),
        longText = listOf(TextTranslation("EN", "Yes")),
        required = true,
        type = ConsentItem.Type.Info
      )
    }
  }
})
