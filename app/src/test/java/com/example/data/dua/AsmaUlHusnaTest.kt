package com.example.data.dua

import com.example.data.localization.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AsmaUlHusnaTest {

  private val names = AsmaUlHusna.NAMES

  @Test
  fun exactlyNinetyNineNumberedInOrder() {
    assertEquals(99, names.size)
    assertEquals((1..99).toList(), names.map { it.number })
    assertEquals(99, names.map { it.arabic }.toSet().size)
  }

  /** Tirmidhi 3507 exactly: "Allah" is the first, it ends with as-Sabur, and al-Ahad is absent. */
  @Test
  fun followsTirmidhi3507() {
    assertEquals("Allah", names.first().transliteration)
    assertEquals("As-Sabur", names.last().transliteration)
    assertFalse(names.any { it.arabic == "الْأَحَدُ" })
  }

  @Test
  fun bengaliReadersGetBengaliPronunciation() {
    assertEquals("আল্লাহ", names.first().pronunciation(AppLanguage.BENGALI))
    assertEquals("আস-সাবূর", names.last().pronunciation(AppLanguage.BENGALI))
    names.forEach { assertTrue("${it.number}", it.transliterationBn.isNotBlank()) }
    assertEquals(null, names.first().pronunciation(AppLanguage.ARABIC))
  }

  @Test
  fun spanishReadersGetSpanishSpelling() {
    assertEquals("Al-Yabbár", names.first { it.number == 10 }.pronunciation(AppLanguage.SPANISH))
    assertEquals("Al-Jáliq", names.first { it.number == 12 }.pronunciation(AppLanguage.SPANISH))
    names.forEach { assertTrue("${it.number}", it.transliterationEs.isNotBlank()) }
    // Portuguese keeps the common spelling.
    assertEquals("Al-Jabbar", names.first { it.number == 10 }.pronunciation(AppLanguage.PORTUGUESE))
  }

  @Test
  fun everyNameHasEveryLanguage() {
    names.forEach { name ->
      AppLanguage.entries.forEach { language ->
        assertTrue("${name.number} ${language.name}", name.meanings[language].orEmpty().isNotBlank())
      }
    }
  }

  @Test
  fun everyNameHasAnExplanationAndAWellFormedVerse() {
    assertEquals(99, ASMA_EXPLANATIONS.size)
    names.forEach { name ->
      AppLanguage.entries.forEach { language ->
        assertTrue("${name.number} ${language.name}", name.explanation(language).isNotBlank())
      }
      name.quranRef?.let { ref ->
        val match = Regex("""^(\d{1,3}):(\d{1,3})$""").matchEntire(ref)
        assertTrue("${name.number} $ref", match != null)
        assertTrue("${name.number} $ref", match!!.groupValues[1].toInt() in 1..114)
      }
    }
    // Spot checks against the Qur'an text, verified when the references were added.
    assertEquals("1:3", names[1].quranRef) // Ar-Rahman
    assertEquals("59:23", names.first { it.transliteration == "Al-Quddus" }.quranRef)
  }
}

