package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Haasi al-layl", appName)
  }

  @Test
  fun `verify bangla date calculation`() {
    val date = java.time.LocalDate.of(2026, 9, 11)
    val banglaDate = com.example.data.calendar.BanglaDateHelper.getBanglaDate(date)
    assertEquals(1433, banglaDate.year)
    assertEquals("ভাদ্র", banglaDate.monthNameBn)
  }
}
