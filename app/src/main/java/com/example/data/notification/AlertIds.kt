package com.example.data.notification

import com.example.data.model.PrayerName
import com.example.data.prayer.ForbiddenWindowType

/** Which of the two alerts a prayer gets. */
enum class PrayerAlertKind {
  /** Posted at the prayer time itself. */
  AT_TIME,

  /** Posted five minutes later, and skipped if the prayer is already marked done. */
  FOLLOW_UP
}

/**
 * Stable ids for alarms and notifications.
 *
 * One namespace serves both the PendingIntent request code and the notification id, so a re-fired
 * alert replaces its own previous notification instead of stacking. Slots are reused day after
 * day on purpose: arming tomorrow's Fajr overwrites today's spent alarm rather than leaking one
 * PendingIntent per day.
 *
 * This replaces `abs(prayerName.hashCode()) % 100`, which hashed a *localized display string* -
 * so the same prayer got a different id in every language, with collisions unguarded.
 */
object AlertIds {
  private const val BASE_PRAYER = 10_000
  private const val BASE_FORBIDDEN = 20_000
  private const val BASE_TAHAJJUD = 30_000
  private const val BASE_MAINTENANCE = 40_000

  // Keyed on enum ordinal. Reordering PrayerName or ForbiddenWindowType silently rebinds every
  // slot, so AlertIdsTest pins the exact values.
  fun prayer(prayer: PrayerName, kind: PrayerAlertKind): Int =
    BASE_PRAYER + prayer.ordinal * 10 + kind.ordinal

  fun forbidden(type: ForbiddenWindowType, isStart: Boolean): Int =
    BASE_FORBIDDEN + type.ordinal * 10 + (if (isStart) 0 else 1)

  const val TAHAJJUD = BASE_TAHAJJUD
  const val DAILY_REFRESH = BASE_MAINTENANCE

  /** Every id this app can produce, so a reschedule can cancel the whole space and start clean. */
  fun all(): List<Int> = buildList {
    PrayerName.entries.forEach { prayer ->
      PrayerAlertKind.entries.forEach { kind -> add(prayer(prayer, kind)) }
    }
    ForbiddenWindowType.entries.forEach { type ->
      add(forbidden(type, isStart = true))
      add(forbidden(type, isStart = false))
    }
    add(TAHAJJUD)
    add(DAILY_REFRESH)
  }
}
