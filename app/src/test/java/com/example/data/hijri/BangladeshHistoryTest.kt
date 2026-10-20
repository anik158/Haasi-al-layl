package com.example.data.hijri

import com.example.data.astronomy.CrescentCalculator
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId

/**
 * The moon rule against what Bangladesh's committee actually announced, 1440-1448 AH: every row of
 * [BangladeshHistory], Saudi Arabia's date in, Bangladesh's date out, the crescent judged from
 * Dhaka.
 */
class BangladeshHistoryTest {

  private val dhaka = ZoneId.of("Asia/Dhaka")
  private val crescentV = { date: java.time.LocalDate ->
    CrescentCalculator.evaluate(date, 23.8103, 90.4125, dhaka).odehV
  }

  private fun resolverFor(announced: Map<HijriMonthId, java.time.LocalDate>) = MonthStartResolver(
    policy = SightingPolicy.LOCAL_SIGHTING,
    announced = announced,
    userStarts = emptyMap(),
    crescentV = crescentV
  )

  @Test
  fun `every announced Bangladesh start is reproduced from Saudi Arabia and the moon`() {
    val resolver = resolverFor(emptyMap())
    val misses = BangladeshHistory.STARTS.filter { it.verified }.mapNotNull { row ->
      val estimate = resolver.localEstimate(row.saudi)
      if (estimate == row.bangladesh) null else "${row.label}: expected ${row.bangladesh}, got $estimate"
    }
    assertEquals("misses:\n" + misses.joinToString("\n"), 0, misses.size)
  }

  @Test
  fun `the unverified row agrees too`() {
    val row = BangladeshHistory.STARTS.single { !it.verified }
    assertEquals(row.bangladesh, resolverFor(emptyMap()).localEstimate(row.saudi))
  }

  @Test
  fun `Ramadan into Shawwal, resolved as a chain, gives the announced Eid`() {
    val ramadans = BangladeshHistory.STARTS.filter { it.hijriMonth == HijriMonthId.RAMADAN }
    ramadans.forEach { ramadan ->
      val shawwal = BangladeshHistory.STARTS.single {
        it.hijriYear == ramadan.hijriYear && it.hijriMonth == HijriMonthId.SHAWWAL
      }
      val r = HijriMonthId(ramadan.hijriYear, HijriMonthId.RAMADAN)
      val table = resolverFor(mapOf(r to ramadan.saudi, r.next() to shawwal.saudi)).resolve(r, 2)
      assertEquals(ramadan.label, ramadan.bangladesh, table.start(r)!!.startsOn)
      assertEquals(shawwal.label, shawwal.bangladesh, table.start(r.next())!!.startsOn)
    }
  }
}
