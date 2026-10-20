package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppClock
import com.example.data.hijri.HijriCalendar
import com.example.data.hijri.HijriMonthId
import com.example.data.hijri.HijriMonthTable
import com.example.data.hijri.HijriStore
import com.example.data.model.CityLocation
import com.example.data.preferences.UserPreferences
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** One moment worth testing, and the label on its button. */
data class TestMoment(val label: String, val at: ZonedDateTime)

/**
 * The moments to try, from the coming Ramadan and Dhul-Hijjah in [table] (built on the real date,
 * so the list does not move while a test date is on).
 */
fun testMoments(table: HijriMonthTable, zone: ZoneId): List<TestMoment> {
  fun start(month: Int) = table.starts.firstOrNull { it.id.month == month && it.id >= table.starts[3].id }?.startsOn
  fun at(date: LocalDate?, hour: Int, label: String) = date?.let { TestMoment(label, ZonedDateTime.of(it, LocalTime.of(hour, 0), zone)) }
  val ramadan = start(HijriMonthId.RAMADAN)
  val dhulHijjah = start(HijriMonthId.DHUL_HIJJAH)
  val shawwal = ramadan?.let { r -> table.starts.firstOrNull { it.startsOn.isAfter(r) }?.startsOn }
  val shabanStart = ramadan?.let { r -> table.starts.lastOrNull { it.startsOn.isBefore(r) }?.startsOn }
  return listOfNotNull(
    at(shabanStart?.plusDays(27), 20, "28 Sha'ban, 8 pm - the correction opens"),
    at(shabanStart?.plusDays(28), 20, "29 Sha'ban, 8 pm - the Ramadan sighting evening"),
    at(ramadan, 3, "1 Ramadan, 3 am - before Sehri ends"),
    at(ramadan, 14, "1 Ramadan, 2 pm - fasting, counting down to Iftar"),
    at(ramadan?.plusDays(14), 14, "15 Ramadan, 2 pm - mid-Ramadan"),
    at(ramadan?.plusDays(28), 20, "29 Ramadan, 8 pm - the Eid sighting evening"),
    at(shawwal, 9, "Eid al-Fitr, 9 am - fasting forbidden"),
    at(dhulHijjah?.plusDays(8), 9, "Day of Arafah, 9 am"),
    at(dhulHijjah?.plusDays(9), 9, "Eid al-Adha, 9 am - fasting forbidden"),
    at(dhulHijjah?.plusDays(10), 9, "11 Dhul-Hijjah - Tashreeq, fasting forbidden")
  )
}

private val SHOWN = DateTimeFormatter.ofPattern("EEE d MMM yyyy, h:mm a", Locale.ENGLISH)

/**
 * Settings → "Test a date", in debug builds only: runs Today, the Hijri calendar and the moon
 * sighting correction as if it were one of [testMoments]. Alarms stay on the real clock, and a
 * restart returns to the real time. English only - a developer tool, not a feature.
 */
@Composable
fun TestDateCard(onChanged: () -> Unit) {
  val context = LocalContext.current
  val city: CityLocation = remember { UserPreferences(context).selectedCity } ?: return
  val zone = remember(city) { runCatching { ZoneId.of(city.timeZoneId) }.getOrDefault(ZoneId.systemDefault()) }
  val offset by AppClock.offsetMillis.collectAsState()
  val moments = remember(city) {
    testMoments(HijriCalendar.build(city, LocalDate.now(zone), HijriStore(context)), zone)
  }

  fun apply(change: () -> Unit) {
    change()
    // A table around the new date, so Ramadan and Eid fall inside it.
    HijriCalendar.rebuild(context)
    onChanged()
  }

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4E0)),
    modifier = Modifier.fillMaxWidth().testTag("test_date_card")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text("Test a date (debug build only)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF8A5A00))
      Text(
        "Runs Today, the Hijri date and Moon sighting announced as if it were that moment. " +
          "Alarms keep the real time. Back to real time (or a restart) also removes any month " +
          "start saved under a test date. \"I'm fasting\" is hidden while testing - it would arm " +
          "a real alarm; try it on the real date instead.",
        fontSize = 12.sp,
        color = Color(0xFF8A5A00)
      )
      Spacer(Modifier.height(8.dp))
      Text(
        if (offset == 0L) "Now: real time" else "Now: ${AppClock.now(zone).format(SHOWN)} (test)",
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        modifier = Modifier.testTag("test_date_now")
      )
      Spacer(Modifier.height(8.dp))
      if (offset != 0L) {
        Button(
          onClick = {
            HijriCalendar.leaveTestDate(context)
            onChanged()
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3E2F)),
          modifier = Modifier.fillMaxWidth().testTag("test_date_real")
        ) { Text("Back to real time") }
        Spacer(Modifier.height(6.dp))
      }
      moments.forEachIndexed { i, moment ->
        OutlinedButton(
          onClick = { apply { AppClock.testAt(moment.at) } },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp).testTag("test_date_$i")
        ) {
          Column(modifier = Modifier.fillMaxWidth()) {
            Text(moment.label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F3E2F))
            Text(moment.at.format(SHOWN), fontSize = 11.sp, color = Color(0xFF0F3E2F).copy(alpha = 0.7f))
          }
        }
      }
    }
  }
}

/** On Today while a test date is on, so it is never mistaken for the real day. */
@Composable
fun TestDateBanner(zone: ZoneId, onBackToRealTime: () -> Unit) {
  val offset by AppClock.offsetMillis.collectAsState()
  if (offset == 0L) return
  Card(
    colors = CardDefaults.cardColors(containerColor = Color(0xFFB3261E)),
    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("test_date_banner")
  ) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
      Text("TEST DATE: ${AppClock.now(zone).format(SHOWN)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
      Text("Today is running on a test date. Alarms keep the real time.", color = Color.White, fontSize = 11.sp)
      OutlinedButton(onClick = onBackToRealTime, modifier = Modifier.padding(top = 4.dp).testTag("test_date_banner_real")) {
        Text("Back to real time", color = Color.White)
      }
    }
  }
}
