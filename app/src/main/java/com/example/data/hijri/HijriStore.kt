package com.example.data.hijri

import android.content.Context
import java.time.LocalDate

/** Where the user heard the announcement. */
enum class HeardFrom { NATIONAL_COMMITTEE, LOCAL_MOSQUE, TV_OR_NEWS, OTHER }

/** The user's correction of one month's start. */
data class UserMonthStart(
  val id: HijriMonthId,
  val startsOn: LocalDate,
  val heardFrom: HeardFrom,
  val recordedAtMillis: Long,
  /** Until when Today offers Undo: the next Maghrib after it was set. */
  val undoUntilMillis: Long,
  /** The user's earlier date for this month, restored by Undo; null when there was none. */
  val previous: LocalDate?
)

enum class HistoryAction { SET, UNDO, CLEARED }

/** One change, kept so a wrong tap can be traced. */
data class HistoryEntry(
  val atMillis: Long,
  val action: HistoryAction,
  val id: HijriMonthId,
  val from: LocalDate?,
  val to: LocalDate?,
  val heardFrom: HeardFrom?
)

/**
 * The Hijri calendar's inputs that must survive a restart: announcements cached from Aladhan, the
 * user's corrections, and their history. Plain strings in SharedPreferences - small, and no schema
 * to migrate. Writes commit synchronously, like UserPreferences: a correction that vanished after
 * a force-stop would move Ramadan back without telling anyone.
 */
class HijriStore(context: Context) {

  private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

  fun announced(authority: Authority): Map<HijriMonthId, LocalDate> =
    decodeDates(prefs.getString(announcedKey(authority), null))

  /** Merges [starts] over what is cached; months not fetched this time keep their old answer. */
  fun saveAnnounced(authority: Authority, starts: Map<HijriMonthId, LocalDate>, atMillis: Long) {
    val merged = announced(authority) + starts
    prefs.edit()
      .putString(announcedKey(authority), encodeDates(merged))
      .putLong(fetchedKey(authority), atMillis)
      .commit()
  }

  fun fetchedAtMillis(authority: Authority): Long = prefs.getLong(fetchedKey(authority), 0L)

  fun userStarts(): Map<HijriMonthId, UserMonthStart> =
    prefs.getString(KEY_USER, null).orEmpty().split(';').mapNotNull(::decodeUser).associateBy { it.id }

  fun history(): List<HistoryEntry> =
    prefs.getString(KEY_HISTORY, null).orEmpty().split(';').mapNotNull(::decodeHistory)

  /** Saves one month's correction and records it. */
  fun setUserStart(start: UserMonthStart) {
    val starts = userStarts() + (start.id to start)
    val entry = HistoryEntry(start.recordedAtMillis, HistoryAction.SET, start.id, start.previous, start.startsOn, start.heardFrom)
    write(starts.values, history() + entry)
  }

  /** Undo: back to the user's earlier date for the month, or to the app's estimate. */
  fun undo(id: HijriMonthId, atMillis: Long) {
    val current = userStarts()[id] ?: return
    val starts = userStarts().toMutableMap()
    if (current.previous != null) {
      starts[id] = current.copy(startsOn = current.previous, previous = null, undoUntilMillis = 0L)
    } else {
      starts.remove(id)
    }
    write(starts.values, history() + HistoryEntry(atMillis, HistoryAction.UNDO, id, current.startsOn, current.previous, null))
  }

  /** "Use the app's estimate again". */
  fun clear(id: HijriMonthId, atMillis: Long) {
    val current = userStarts()[id] ?: return
    write(
      (userStarts() - id).values,
      history() + HistoryEntry(atMillis, HistoryAction.CLEARED, id, current.startsOn, null, null)
    )
  }

  /**
   * Drops corrections and history stamped after [realNowMillis] - which only a debug build's test
   * date can produce - so trying the flow on a future Ramadan never moves the real one.
   */
  fun dropTestRecords(realNowMillis: Long) {
    val starts = userStarts().values.filter { it.recordedAtMillis <= realNowMillis }
    val history = history().filter { it.atMillis <= realNowMillis }
    if (starts.size != userStarts().size || history.size != history().size) write(starts, history)
  }

  private fun write(starts: Collection<UserMonthStart>, history: List<HistoryEntry>) {
    prefs.edit()
      .putString(KEY_USER, starts.sortedBy { it.id }.joinToString(";", transform = ::encodeUser))
      .putString(KEY_HISTORY, history.takeLast(MAX_HISTORY).joinToString(";", transform = ::encodeHistory))
      .commit()
  }

  companion object {
    private const val PREFS = "haasi_hijri"
    private const val KEY_USER = "user_starts"
    private const val KEY_HISTORY = "history"
    private const val MAX_HISTORY = 30

    private fun announcedKey(authority: Authority) = "announced_${authority.aladhanMethod}"
    private fun fetchedKey(authority: Authority) = "fetched_${authority.aladhanMethod}"

    internal fun encodeDates(dates: Map<HijriMonthId, LocalDate>): String =
      dates.entries.sortedBy { it.key }.joinToString(",") { "${it.key.key}=${it.value}" }

    internal fun decodeDates(text: String?): Map<HijriMonthId, LocalDate> =
      text.orEmpty().split(',').mapNotNull { pair ->
        val (key, value) = pair.split('=').takeIf { it.size == 2 } ?: return@mapNotNull null
        val id = HijriMonthId.parse(key) ?: return@mapNotNull null
        val date = runCatching { LocalDate.parse(value) }.getOrNull() ?: return@mapNotNull null
        id to date
      }.toMap()

    private fun date(text: String): LocalDate? =
      if (text.isEmpty()) null else runCatching { LocalDate.parse(text) }.getOrNull()

    internal fun encodeUser(u: UserMonthStart) =
      listOf(u.id.key, u.startsOn, u.heardFrom.name, u.recordedAtMillis, u.undoUntilMillis, u.previous ?: "")
        .joinToString("|")

    internal fun decodeUser(text: String): UserMonthStart? {
      val p = text.split('|')
      if (p.size != 6) return null
      return UserMonthStart(
        id = HijriMonthId.parse(p[0]) ?: return null,
        startsOn = date(p[1]) ?: return null,
        heardFrom = HeardFrom.entries.find { it.name == p[2] } ?: HeardFrom.OTHER,
        recordedAtMillis = p[3].toLongOrNull() ?: return null,
        undoUntilMillis = p[4].toLongOrNull() ?: 0L,
        previous = date(p[5])
      )
    }

    internal fun encodeHistory(h: HistoryEntry) =
      listOf(h.atMillis, h.action.name, h.id.key, h.from ?: "", h.to ?: "", h.heardFrom?.name ?: "").joinToString("|")

    internal fun decodeHistory(text: String): HistoryEntry? {
      val p = text.split('|')
      if (p.size != 6) return null
      return HistoryEntry(
        atMillis = p[0].toLongOrNull() ?: return null,
        action = HistoryAction.entries.find { it.name == p[1] } ?: return null,
        id = HijriMonthId.parse(p[2]) ?: return null,
        from = date(p[3]),
        to = date(p[4]),
        heardFrom = HeardFrom.entries.find { it.name == p[5] }
      )
    }
  }
}
