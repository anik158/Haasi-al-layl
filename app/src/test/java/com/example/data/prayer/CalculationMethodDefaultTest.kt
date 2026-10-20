package com.example.data.prayer

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculationMethodDefaultTest {

  @Test
  fun followsTheCountryByCodeOrName() {
    assertEquals(CalculationMethod.KARACHI, CalculationMethod.defaultFor("Bangladesh"))
    assertEquals(CalculationMethod.KARACHI, CalculationMethod.defaultFor("BD")) // IP lookup
    assertEquals(CalculationMethod.KARACHI, CalculationMethod.defaultFor(" pakistan "))
    assertEquals(CalculationMethod.UMM_AL_QURA, CalculationMethod.defaultFor("Saudi Arabia"))
    assertEquals(CalculationMethod.DUBAI, CalculationMethod.defaultFor("United Arab Emirates"))
    assertEquals(CalculationMethod.EGYPTIAN, CalculationMethod.defaultFor("EG"))
    assertEquals(CalculationMethod.ISNA, CalculationMethod.defaultFor("United States of America (the)"))
    assertEquals(CalculationMethod.MUSLIM_WORLD_LEAGUE, CalculationMethod.defaultFor("Germany"))
  }

  /** No location yet (the Makkah placeholder) keeps the previous default. */
  @Test
  fun noCountryKeepsUmmAlQura() {
    assertEquals(CalculationMethod.UMM_AL_QURA, CalculationMethod.defaultFor(null))
  }
}
