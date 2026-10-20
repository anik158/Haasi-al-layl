package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.astronomy.CrescentCalculator
import com.example.data.hijri.ChangeRow
import com.example.data.hijri.HeardFrom
import com.example.data.hijri.HijriCalendar
import com.example.data.hijri.HijriCountryRules
import com.example.data.hijri.HijriMonthId
import com.example.data.hijri.HijriMonthTable
import com.example.data.hijri.HijriStore
import com.example.data.hijri.MoonCheck
import com.example.data.hijri.SightingAnswer
import com.example.data.hijri.SightingCorrection
import com.example.data.hijri.SightingFlow
import com.example.data.hijri.SightingOption
import com.example.data.hijri.SightingStep
import com.example.data.localization.AppLanguage
import com.example.data.localization.HijriStrings
import com.example.data.localization.LocalizationManager
import com.example.data.model.CityLocation
import com.example.data.model.PrayerName
import com.example.data.notification.PrayerNotificationManager
import com.example.data.prayer.CalculationMethod
import com.example.data.prayer.FastingTimes
import com.example.data.prayer.HijriDateHelper
import com.example.data.prayer.IslamicMadhab
import com.example.data.prayer.PrayerCalculator
import com.example.data.preferences.UserPreferences
import com.example.ui.theme.DarkForestInk
import com.example.ui.theme.LinenBorderLight
import com.example.ui.theme.WarmBeigeBackground
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import androidx.compose.runtime.withFrameMillis

/**
 * Everything the correction flow computes, apart from the screens - so a Robolectric test can
 * drive it exactly as the dialog does.
 */
class SightingFlowModel(
  private val context: Context,
  val city: CityLocation,
  private val method: CalculationMethod,
  private val madhab: IslamicMadhab,
  private val nowMillis: () -> Long = System::currentTimeMillis
) {
  private val store = HijriStore(context)
  val zone: ZoneId = runCatching { ZoneId.of(city.timeZoneId) }.getOrDefault(ZoneId.systemDefault())
  val today: LocalDate get() = Instant.ofEpochMilli(nowMillis()).atZone(zone).toLocalDate()
  private val policy = HijriCountryRules.policyFor(city.country)

  /** What the app shows now. */
  fun currentTable(): HijriMonthTable = HijriCalendar.build(city, today, store)

  fun correctableMonths(): List<HijriMonthId> = SightingCorrection.correctableMonths(today, currentTable())

  fun hasUserStart(id: HijriMonthId) = id in store.userStarts()

  /** The table as it would be without the user's correction for [id]. */
  fun estimateTable(id: HijriMonthId): HijriMonthTable =
    HijriCalendar.build(city, today, store, store.userStarts().mapValues { it.value.startsOn } - id)

  fun options(id: HijriMonthId): List<SightingOption> =
    SightingCorrection.optionsFor(id, estimateTable(id), hasUserStart(id))

  /** The table if [decision] were saved. */
  fun afterTable(decision: SightingFlow.Decision): HijriMonthTable {
    val others = store.userStarts().mapValues { it.value.startsOn } - decision.month
    val user = if (decision.answer == SightingAnswer.USE_ESTIMATE) others else others + (decision.month to decision.startsOn)
    return HijriCalendar.build(city, today, store, user)
  }

  fun moonCheck(option: SightingOption): MoonCheck {
    val evening = CrescentCalculator.evaluate(option.sightingEvening, city.latitude, city.longitude, zone)
    return SightingCorrection.moonCheck(option.answer, evening.visibility, evening.odehV)
  }

  /** The review's rows, with the first Sehri and the Sehri reminder added for Ramadan. */
  fun review(decision: SightingFlow.Decision): List<ChangeRow> {
    val before = currentTable()
    val after = afterTable(decision)
    val rows = SightingCorrection.changes(decision.month, before, after).toMutableList()
    val ramadan = when (decision.month.month) {
      HijriMonthId.RAMADAN -> decision.month
      HijriMonthId.SHABAN -> decision.month.next()
      else -> null
    }
    if (ramadan != null) {
      rows += ChangeRow("first_sehri", before.start(ramadan)?.startsOn, after.start(ramadan)?.startsOn)
      if (PrayerNotificationManager.isSehriReminderEnabled(context)) {
        rows += ChangeRow("sehri_alarm", before.start(ramadan)?.startsOn, after.start(ramadan)?.startsOn)
      }
    }
    return rows
  }

  fun sehriEnd(date: LocalDate): LocalTime = FastingTimes.dayFor(date, city, method, madhab).sehriEnd

  /** Saves [decision] - the only write in the whole flow - and re-arms nothing; the caller does. */
  fun save(decision: SightingFlow.Decision) {
    HijriCalendar.applyCorrection(context, decision, nowMillis(), undoUntilMillis = nextMaghribMillis())
  }

  /** The next Maghrib after now, until which Today offers Undo. */
  fun nextMaghribMillis(): Long {
    val now = Instant.ofEpochMilli(nowMillis()).atZone(zone)
    fun maghrib(date: LocalDate): ZonedDateTime {
      val schedule = PrayerCalculator.calculateSchedule(
        date = date,
        location = city,
        now = LocalDateTime.of(date, LocalTime.NOON),
        method = method,
        madhab = madhab
      )
      return ZonedDateTime.of(date, schedule.items.first { it.prayer == PrayerName.MAGHRIB }.time, zone)
    }
    val todays = maghrib(now.toLocalDate())
    return (if (now.isBefore(todays)) todays else maghrib(now.toLocalDate().plusDays(1))).toInstant().toEpochMilli()
  }

  val authority get() = policy.authority
}

/**
 * Settings → "Moon sighting announced". Shows today's Hijri date and where its month start came
 * from, and opens the correction flow only when a month can start.
 */
@Composable
fun MoonSightingCard(
  language: AppLanguage,
  method: CalculationMethod,
  madhab: IslamicMadhab,
  onSettingsChanged: () -> Unit
) {
  val context = LocalContext.current
  val city = remember { UserPreferences(context).selectedCity } ?: return
  var version by remember { mutableIntStateOf(0) }
  val model = remember(city, method, madhab) { SightingFlowModel(context, city, method, madhab) }
  var showFlow by remember { mutableStateOf(false) }

  // Re-read after every save, so the date and history under the button are current.
  val (todayLabel, months, history) = remember(version, model) {
    val day = model.currentTable().resolve(model.today)
    val label = day?.let {
      HijriDateHelper.getTodayHijriFormatted(model.today, language) + " · " +
        HijriStrings.sourceLabel(it.monthStart.source, it.monthStart.authority, language)
    } ?: HijriDateHelper.getTodayHijriFormatted(model.today, language)
    Triple(label, model.correctableMonths(), HijriStore(context).history().takeLast(5).reversed())
  }

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = WarmBeigeBackground),
    border = androidx.compose.foundation.BorderStroke(1.dp, LinenBorderLight),
    modifier = Modifier.fillMaxWidth().testTag("moon_sighting_card")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(HijriStrings.cardTitle(language), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DarkForestInk)
      Text(HijriStrings.cardDesc(language), fontSize = 12.sp, color = DarkForestInk.copy(alpha = 0.75f))
      Spacer(Modifier.height(8.dp))
      Text(
        todayLabel,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = DarkForestInk.copy(alpha = 0.85f),
        modifier = Modifier.testTag("moon_sighting_today")
      )
      Spacer(Modifier.height(10.dp))
      if (months.isEmpty()) {
        Text(
          HijriStrings.nothingToCorrect(language),
          fontSize = 12.sp,
          color = DarkForestInk.copy(alpha = 0.7f),
          modifier = Modifier.testTag("moon_sighting_nothing")
        )
      } else {
        Button(
          onClick = { showFlow = true },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3E2F)),
          modifier = Modifier.fillMaxWidth().testTag("moon_sighting_open")
        ) { Text(HijriStrings.openFlow(language)) }
      }
      if (history.isNotEmpty()) {
        Spacer(Modifier.height(10.dp))
        Text(HijriStrings.historyTitle(language), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DarkForestInk)
        history.forEach { h ->
          val month = HijriDateHelper.monthYearLabel(h.id.month, h.id.year, language)
          val dates = listOfNotNull(h.from, h.to).joinToString(" → ") { HijriDateHelper.shortGregorian(it, language) }
          Text(
            "${HijriStrings.historyAction(h.action, language)} · $month · $dates",
            fontSize = 11.sp,
            color = DarkForestInk.copy(alpha = 0.7f)
          )
        }
      }
    }
  }

  if (showFlow) {
    SightingFlowDialog(
      model = model,
      language = language,
      onSaved = {
        version++
        onSettingsChanged()
      },
      onDismiss = {
        showFlow = false
        version++
      }
    )
  }
}

/**
 * The correction, one screen at a time. Nothing is written until the second confirmation; Cancel,
 * Back, the system back button, rotation and process death all leave the calendar as it was.
 */
@Composable
fun SightingFlowDialog(
  model: SightingFlowModel,
  language: AppLanguage,
  onSaved: () -> Unit,
  onDismiss: () -> Unit,
  /** Seconds left before Confirm unlocks, reported each frame - for tests. */
  onConfirmWait: (Int) -> Unit = {}
) {
  var flow by remember { mutableStateOf(SightingFlow()) }
  val months = remember { model.correctableMonths() }
  val step = flow.step

  Dialog(
    onDismissRequest = { if (step == SightingStep.DONE) onDismiss() else if (step == SightingStep.MONTH) onDismiss() else flow = flow.back() },
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(22.dp),
      color = Color.White,
      modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("sighting_flow")
    ) {
      Column(
        modifier = Modifier
          .heightIn(max = 640.dp)
          .verticalScroll(rememberScrollState())
          .padding(20.dp)
      ) {
        if (step != SightingStep.DONE) {
          Text(
            HijriStrings.stepCounter(minOf(step.ordinal + 1, 5), 5, language),
            fontSize = 11.sp,
            color = DarkForestInk.copy(alpha = 0.6f)
          )
        }
        Text(
          HijriStrings.stepTitle(if (step == SightingStep.CONFIRM) SightingStep.REVIEW else step, language),
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp,
          color = DarkForestInk,
          modifier = Modifier.testTag("sighting_step_${step.name}")
        )
        Spacer(Modifier.height(12.dp))

        when (step) {
          SightingStep.MONTH -> months.forEach { id ->
            val current = remember(id) { model.currentTable().start(id) }
            ChoiceButton(
              text = HijriDateHelper.monthYearLabel(id.month, id.year, language) + HijriStrings.monthSuffix(id.month, language),
              detail = current?.let {
                HijriDateHelper.shortGregorian(it.startsOn, language) + " · " +
                  HijriStrings.sourceLabel(it.source, it.authority, language)
              },
              tag = "sighting_month_${id.key}"
            ) { flow = flow.chooseMonth(id) }
          }

          SightingStep.ANSWER -> {
            val id = flow.month!!
            val options = remember(id) { model.options(id) }
            val newMonth = HijriDateHelper.monthName(id.month, language)
            val oldMonth = HijriDateHelper.monthName(id.previous().month, language)
            options.firstOrNull()?.let {
              Text(
                HijriStrings.eveningOf(HijriDateHelper.shortGregorian(it.sightingEvening, language), language),
                fontSize = 12.sp,
                color = DarkForestInk.copy(alpha = 0.7f)
              )
              Spacer(Modifier.height(8.dp))
            }
            options.forEach { option ->
              val date = HijriDateHelper.shortGregorian(option.startsOn, language)
              val text = when (option.answer) {
                SightingAnswer.SIGHTED -> HijriStrings.optionSighted(newMonth, date, language)
                SightingAnswer.NOT_SIGHTED -> HijriStrings.optionNotSighted(oldMonth, newMonth, date, language)
                SightingAnswer.USE_ESTIMATE -> HijriStrings.optionEstimate(date, language)
              }
              ChoiceButton(text = text, detail = null, tag = "sighting_option_${option.answer.name}") {
                flow = flow.chooseOption(option)
              }
            }
          }

          SightingStep.SOURCE -> HeardFrom.entries.forEach { source ->
            ChoiceButton(text = HijriStrings.heardFrom(source, language), detail = null, tag = "sighting_source_${source.name}") {
              flow = flow.chooseSource(source)
            }
          }

          SightingStep.REVIEW, SightingStep.CONFIRM -> {
            val decision = flow.decision!!
            val rows = remember(decision) { model.review(decision) }
            val check = remember(decision) { model.moonCheck(flow.option!!) }
            ReviewRows(rows, decision.month, model, language)
            when (check) {
              MoonCheck.SIGHTING_IMPOSSIBLE -> WarningBox(HijriStrings.warningImpossible(language), strong = true, tag = "sighting_warning_impossible")
              MoonCheck.EASILY_VISIBLE -> WarningBox(HijriStrings.noteEasilyVisible(language), strong = false, tag = "sighting_note_easy")
              MoonCheck.OK -> Unit
            }
            Spacer(Modifier.height(12.dp))
            Button(
              onClick = { flow = flow.reviewed() },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3E2F)),
              modifier = Modifier.fillMaxWidth().testTag("sighting_review_continue")
            ) { Text(HijriStrings.reviewContinue(language)) }

            if (step == SightingStep.CONFIRM) {
              ConfirmDialog(
                decision = decision,
                strongWarning = check == MoonCheck.SIGHTING_IMPOSSIBLE,
                language = language,
                onConfirm = {
                  val confirmed = flow.confirm()
                  if (confirmed.step == SightingStep.DONE) {
                    model.save(decision)
                    flow = confirmed
                    onSaved()
                  }
                },
                onBack = { flow = flow.back() },
                onWait = onConfirmWait
              )
            }
          }

          SightingStep.DONE -> {
            val decision = flow.decision!!
            Text(
              HijriStrings.saved(
                HijriDateHelper.monthName(decision.month.month, language),
                HijriDateHelper.shortGregorian(decision.startsOn, language),
                language
              ),
              fontSize = 14.sp,
              color = DarkForestInk,
              modifier = Modifier.testTag("sighting_saved")
            )
          }
        }

        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          if (step == SightingStep.DONE) {
            Spacer(Modifier)
            Button(
              onClick = onDismiss,
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3E2F)),
              modifier = Modifier.testTag("sighting_close")
            ) { Text(HijriStrings.close(language)) }
          } else {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("sighting_cancel")) {
              Text(HijriStrings.cancel(language), color = DarkForestInk)
            }
            if (step != SightingStep.MONTH) {
              OutlinedButton(onClick = { flow = flow.back() }, modifier = Modifier.testTag("sighting_back")) {
                Text(HijriStrings.back(language), color = DarkForestInk)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ChoiceButton(text: String, detail: String?, tag: String, onClick: () -> Unit) {
  OutlinedButton(
    onClick = onClick,
    shape = RoundedCornerShape(14.dp),
    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag(tag)
  ) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
      Text(text, color = DarkForestInk, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
      if (detail != null) Text(detail, color = DarkForestInk.copy(alpha = 0.65f), fontSize = 11.sp)
    }
  }
}

@Composable
private fun ReviewRows(rows: List<ChangeRow>, month: HijriMonthId, model: SightingFlowModel, language: AppLanguage) {
  val monthName = HijriDateHelper.monthName(month.month, language)
  rows.forEach { row ->
    fun show(date: LocalDate?): String = when {
      date == null -> "-"
      row.key == "first_sehri" -> HijriDateHelper.shortGregorian(date, language) + ", " +
        LocalizationManager.formatTime(model.sehriEnd(date), language)
      else -> HijriDateHelper.shortGregorian(date, language)
    }
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 6.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(if (row.changed) Color(0xFFFFF4E0) else DarkForestInk.copy(alpha = 0.04f))
        .padding(horizontal = 12.dp, vertical = 8.dp)
        .testTag("sighting_row_${row.key}")
    ) {
      Text(HijriStrings.changeLabel(row.key, monthName, language), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkForestInk)
      Text(
        if (row.changed) "${show(row.from)}  →  ${show(row.to)}" else "${show(row.to)} · ${HijriStrings.unchanged(language)}",
        fontSize = 12.sp,
        color = DarkForestInk.copy(alpha = 0.85f)
      )
    }
  }
}

@Composable
private fun WarningBox(text: String, strong: Boolean, tag: String) {
  val (tint, background) = if (strong) Color(0xFFB3261E) to Color(0xFFFDECEA) else Color(0xFF8A5A00) to Color(0xFFFFF4E0)
  Text(
    text,
    color = tint,
    fontSize = 13.sp,
    fontWeight = if (strong) FontWeight.Bold else FontWeight.Normal,
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 6.dp)
      .clip(RoundedCornerShape(12.dp))
      .background(background)
      .padding(12.dp)
      .testTag(tag)
  )
}

/** Whole seconds left on a [lockMillis] lock after [elapsedMillis], rounded up: 3, 2, 1, then 0. */
fun confirmLockSecondsLeft(lockMillis: Long, elapsedMillis: Long): Int {
  val left = lockMillis - elapsedMillis
  return if (left <= 0) 0 else ((left + 999) / 1000).toInt()
}

/** The second confirmation: its own dialog, its button named after the action. */
@Composable
private fun ConfirmDialog(
  decision: SightingFlow.Decision,
  strongWarning: Boolean,
  language: AppLanguage,
  onConfirm: () -> Unit,
  onBack: () -> Unit,
  onWait: (Int) -> Unit
) {
  // After an impossible-sighting warning the button waits three seconds, so the warning is read.
  // Counted on frame time, the clock Compose tests can step.
  val lockMillis = if (strongWarning) 3_000L else 0L
  var wait by remember(decision) { mutableIntStateOf(if (strongWarning) 3 else 0) }
  val reportWait by androidx.compose.runtime.rememberUpdatedState(onWait)
  LaunchedEffect(decision) {
    reportWait(wait)
    if (lockMillis == 0L) return@LaunchedEffect
    val opened = withFrameMillis { it }
    while (wait > 0) {
      withFrameMillis { now -> wait = confirmLockSecondsLeft(lockMillis, now - opened) }
      reportWait(wait)
    }
  }
  val month = HijriDateHelper.monthName(decision.month.month, language)
  val date = HijriDateHelper.shortGregorian(decision.startsOn, language)
  AlertDialog(
    onDismissRequest = onBack,
    title = { Text(HijriStrings.stepTitle(SightingStep.CONFIRM, language)) },
    text = {
      Column {
        Text(HijriStrings.confirmQuestion(month, date, language), modifier = Modifier.testTag("sighting_confirm_question"))
        if (strongWarning) {
          Spacer(Modifier.height(8.dp))
          Text(HijriStrings.warningImpossible(language), color = Color(0xFFB3261E), fontSize = 12.sp)
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onConfirm,
        enabled = wait == 0,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3E2F)),
        modifier = Modifier.testTag("sighting_confirm")
      ) {
        Text(
          when {
            wait > 0 -> HijriStrings.waitSeconds(wait, language)
            decision.answer == SightingAnswer.USE_ESTIMATE -> HijriStrings.confirmEstimateButton(language)
            else -> HijriStrings.confirmButton(HijriDateHelper.weekday(decision.startsOn, language), language)
          }
        )
      }
    },
    dismissButton = {
      TextButton(onClick = onBack, modifier = Modifier.testTag("sighting_confirm_back")) {
        Text(HijriStrings.back(language))
      }
    }
  )
}
