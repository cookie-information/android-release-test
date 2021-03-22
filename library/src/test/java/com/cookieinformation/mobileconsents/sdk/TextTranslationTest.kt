package com.cookieinformation.mobileconsents.sdk

import com.cookieinformation.mobileconsents.TextTranslation
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.util.Locale

internal class TextTranslationTest : DescribeSpec({

  describe("TextTranslation.getTranslationFor") {

    it("empty locale list and empty translation list") {
      TextTranslation.getTranslationFor(emptyList(), emptyList()) shouldBe TextTranslation("", "")
    }

    it("empty locale list with EN translation") {
      val translations = listOf(TextTranslation("EN", "A"))
      TextTranslation.getTranslationFor(translations, emptyList()) shouldBe TextTranslation("EN", "A")
    }

    it("empty locale list without EN translation") {
      val translations = listOf(TextTranslation("PL", "A"))
      TextTranslation.getTranslationFor(translations, emptyList()) shouldBe TextTranslation("", "")
    }

    it("locales [PL, DE] list with PL and DE translation") {
      val translations = listOf(TextTranslation("PL", "A"), TextTranslation("DE", "A"))
      val locales = listOf(Locale("pl"), Locale("de"))
      TextTranslation.getTranslationFor(translations, locales) shouldBe TextTranslation("PL", "A")
    }

    it("locales [DE, PL] list with PL and DE translation") {
      val translations = listOf(TextTranslation("PL", "A"), TextTranslation("DE", "B"))
      val locales = listOf(Locale("de"), Locale("pl"))
      TextTranslation.getTranslationFor(translations, locales) shouldBe TextTranslation("DE", "B")
    }

    it("locales [DE, PL] list without PL and DE translation") {
      val translations = listOf(TextTranslation("EN", "A"), TextTranslation("FR", "B"))
      val locales = listOf(Locale("de"), Locale("pl"))
      TextTranslation.getTranslationFor(translations, locales) shouldBe TextTranslation("EN", "A")
    }
  }
})
