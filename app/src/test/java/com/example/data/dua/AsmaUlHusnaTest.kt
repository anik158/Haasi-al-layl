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
  fun everyNameHasEveryLanguage() {
    names.forEach { name ->
      AppLanguage.entries.forEach { language ->
        assertTrue("${name.number} ${language.name}", name.meanings[language].orEmpty().isNotBlank())
      }
    }
  }
}
