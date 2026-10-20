package com.example

/**
 * Features that are built but not shown yet. Flip a flag to bring one back; the code behind it is
 * kept compiling and tested so it does not rot while hidden.
 */
object FeatureFlags {

  /**
   * The Sehri & Iftar card on Today (at the top in Ramadan, above Today's verse otherwise), the
   * Hijri adjustment card in Settings, and the Imsak row. On since the card says how sure its Hijri
   * date is and which days are forbidden, discouraged or recommended (FastingRules). The month
   * start still follows the calculation; see docs/design/MONTH-START.md.
   */
  const val SHOW_FASTING_TIMES = true

  /**
   * Whether [prayer] belongs in the schedule lists. Imsak (Fajr - 10) is the conventional Sehri
   * cut-off, so it is hidden along with the rest of the fasting times.
   */
  fun showsInSchedule(prayer: com.example.data.model.PrayerName): Boolean =
    SHOW_FASTING_TIMES || prayer != com.example.data.model.PrayerName.IMSAK
}
