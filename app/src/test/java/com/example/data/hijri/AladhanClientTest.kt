package com.example.data.hijri

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class AladhanClientTest {

  private val ramadan = HijriMonthId(1447, 9)
  private val real = javaClass.getResource("/aladhan/ramadan-1447-hjcosa.json")!!.readText()

  @Test
  fun `the real response for Ramadan 1447 parses to 18 February 2026`() {
    assertEquals(LocalDate.of(2026, 2, 18), AladhanClient.parse(real, ramadan))
  }

  @Test
  fun `an answer about another month is dropped`() {
    assertNull(AladhanClient.parse(real, HijriMonthId(1447, 10)))
    assertNull(AladhanClient.parse(real, HijriMonthId(1446, 9)))
  }

  @Test
  fun `errors and junk are dropped, not thrown`() {
    assertNull(AladhanClient.parse("", ramadan))
    assertNull(AladhanClient.parse("<html>502 Bad Gateway</html>", ramadan))
    assertNull(AladhanClient.parse("""{"code":400,"status":"BAD_REQUEST","data":"Invalid date"}""", ramadan))
    assertNull(AladhanClient.parse(real.replace("18-02-2026", "2026-02-18"), ramadan))
  }

  @Test
  fun `urls ask for the 1st of the month in the authority's calendar`() {
    assertEquals(
      "https://api.aladhan.com/v1/hToG/01-09-1447?calendarMethod=HJCoSA",
      AladhanClient.firstOfMonthUrl(ramadan, Authority.SAUDI)
    )
    assertTrue(AladhanClient.firstOfMonthUrl(ramadan, Authority.TURKEY).endsWith("calendarMethod=DIYANET"))
  }

  @Test
  fun `a month that fails is missing, the rest still come`() {
    val server = SaudiFixture.Server()
    val months = listOf(HijriMonthId(1447, 8), ramadan, HijriMonthId(1460, 1))
    val starts = AladhanClient.fetchStarts(months, Authority.SAUDI, server)
    assertEquals(setOf(HijriMonthId(1447, 8), ramadan), starts.keys)
    assertEquals(LocalDate.of(2026, 2, 18), starts[ramadan])
    assertEquals(3, server.requests.size)
  }

  @Test
  fun `offline returns nothing`() {
    assertTrue(AladhanClient.fetchStarts(listOf(ramadan), Authority.SAUDI, SaudiFixture.Server(online = false)).isEmpty())
  }
}
