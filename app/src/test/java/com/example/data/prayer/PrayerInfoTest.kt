package com.example.data.prayer

import com.example.data.localization.AppLanguage
import com.example.data.model.PrayerName
import org.junit.Assert.assertTrue
import org.junit.Test

class PrayerInfoTest {

  @Test
  fun `every schedule entry has a meaning and description in every language`() {
    for (prayer in PrayerName.entries) {
      for (language in AppLanguage.entries) {
        assertTrue("$prayer/$language meaning", PrayerInfo.meaning(prayer, language).isNotBlank())
        assertTrue("$prayer/$language description", PrayerInfo.description(prayer, language).isNotBlank())
      }
    }
  }
}
