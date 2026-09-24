package com.example.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class ResponsiveFontFactorTest {

  @Test
  fun `reference width is unscaled`() {
    assertEquals(1f, responsiveFontFactor(393), 0.001f)
  }

  @Test
  fun `a 360dp phone shrinks proportionally`() {
    assertEquals(360f / 393f, responsiveFontFactor(360), 0.001f)
  }

  @Test
  fun `the narrowest phones stop at the floor`() {
    assertEquals(0.85f, responsiveFontFactor(320), 0.001f)
    assertEquals(0.85f, responsiveFontFactor(240), 0.001f)
  }

  @Test
  fun `wide screens are never enlarged`() {
    assertEquals(1f, responsiveFontFactor(480), 0.001f)
    assertEquals(1f, responsiveFontFactor(840), 0.001f)
  }
}
