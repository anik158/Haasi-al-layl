package com.example

/**
 * Features that are built but not shown yet. Flip a flag to bring one back; the code behind it is
 * kept compiling and tested so it does not rot while hidden.
 */
object FeatureFlags {

  /**
   * The Sehri & Iftar card on Today and the Hijri adjustment card in Settings. Held back until the
   * start of the month can follow the local moon sighting rather than only a calculation.
   * FastingTimes and the automatic Hijri adjustment still run underneath.
   */
  const val SHOW_FASTING_TIMES = false

  /**
   * Whether [prayer] belongs in the schedule lists. Imsak (Fajr - 10) is the conventional Sehri
   * cut-off, so it is hidden along with the rest of the fasting times.
   */
  fun showsInSchedule(prayer: com.example.data.model.PrayerName): Boolean =
    SHOW_FASTING_TIMES || prayer != com.example.data.model.PrayerName.IMSAK
}
