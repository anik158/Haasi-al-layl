# Design: knowing when the Hijri month starts — and saying how sure we are

Status: **proposed**, not implemented. Sehri & Iftar stay hidden (`FeatureFlags.SHOW_FASTING_TIMES`)
until phases 1–4 are done.

## The problem

Sehri ends at Fajr and Iftar is at sunset. Both are astronomy, and the app already computes them
offline. The hard part is **which days are Ramadan**: each month starts on a moon sighting, so it
lasts 29 or 30 days, and the answer differs by country (Saudi Arabia, then often a day later in
Bangladesh). No calculation can know it in advance — *"fast when you see it, and stop when you see
it; if it is hidden, complete thirty"* (Bukhari 1909, Muslim 1081).

## Where the current code falls short

1. **One offset for all months.** `HijriDateHelper.adjustmentDays` shifts every date by the same
   −2…+2. Real sightings differ month to month, so a −1 that is right for Ramadan can be wrong at
   Eid.
2. **The country default is a guess.** −1 for BD/IN/PK/NP/LK/MA, 0 elsewhere — a rule of thumb.
3. **Nothing online.** The app has a network layer but never asks anyone what the date is.
4. **No user answer.** There is no "was the moon sighted?" and nowhere to keep it.
5. **The end of Ramadan is not modelled.** Day 30 is shown even after a 29-day month; no Eid.
6. **Two ideas of "today".** The Islamic day starts at Maghrib; the Hijri date on Today flips at
   midnight, while `FastingTimes` switches at Iftar.
7. **No Sehri/Iftar alerts** in the alarm planner.

(Fixed before this design: `minSdk` 26 — `java.time` was crashing Android 7.x.)

## The design

### 1. Month starts, not an offset

Store **when each month started**, per country:

```
MonthStart(hijriYear, hijriMonth, country, startsOn: LocalDate, source, recordedAt)
source = USER | ANNOUNCED_LOCAL | ANNOUNCED_SAUDI | CALCULATED
```

A date is resolved from the latest known start on or before it: day = days since start + 1.
Day 30 counts as the same month only until the next month's start is known. Kept as a small JSON
file in the app's private storage (not Room, so there is no schema migration), and replaces
`adjustmentDays`.

### 2. Sources, best first

| Source | How | When it applies |
| --- | --- | --- |
| **The user's answer** | "Was the new moon sighted where you are?" | Always wins for that month |
| **A local announcement** | `https://www.ahsanulhouqe.com/haasi-al-layl/hijri/bd.json`, a small file **you** update on sighting nights (Ramadan, Shawwal, Dhul Hijjah at least) | Bangladesh — no public API exists for it |
| **Saudi Arabia's announcement** | Aladhan, free, no key: `GET api.aladhan.com/v1/gToH/{dd-MM-yyyy}?calendarMethod=HJCoSA` | Countries that follow Saudi Arabia; elsewhere shown only as context |
| **Calculation** | Umm al-Qura (`HijrahDate`) + the country default | Offline, nothing else known |

Fetched when online, cached for offline use, refreshed once a day and on the evening of the 29th.

### 3. Always say how sure the date is (the warning)

Every Hijri date the app shows carries its source:

| Source | Shown as |
| --- | --- |
| Calculated | "calculated · may differ by a day from your local sighting" |
| Saudi announcement | "announced in Saudi Arabia · your country may differ" |
| Local announcement | "announced for Bangladesh" |
| User | "confirmed by you" |

During the days a month could start (the 29th and 30th) and the first days of Ramadan, the
Sehri & Iftar card says plainly: *"Ramadan's start follows the moon sighting in your country. Check
your local announcement."* It never shows a calculated date as certain.

### 4. The sighting question

On the evening (after Maghrib) of the 29th of Sha'ban, Ramadan and Dhul Qa'dah, a card on Today:
**"Was the new moon sighted where you are tonight?"**
- **Yes** → the next month starts tomorrow.
- **No** → this month completes 30 days.
- No answer → the best other source is used, with its warning.

### 5. One "day" for fasting

The fasting day already switches at Iftar. The Hijri date shown next to it should do the same
during Ramadan, so the first Sehri appears under "1 Ramadan", not "29 Sha'ban".

### 6. Then Sehri & Iftar

Sehri ends at Fajr and Iftar is at Maghrib, on the days the month table marks as Ramadan — as
`FastingTimes` already computes. Later: a Sehri wake-up alarm ("N minutes before Sehri ends") in
the alarm planner, with its own slots.

## Phases — each one tested before it reaches a phone

| Phase | What | Test first |
| --- | --- | --- |
| 1 | `MonthStart` store and resolver, calculation fallback, replace `adjustmentDays` | resolver unit tests: 29/30-day months, missing data, country |
| 2 | The source label and warning on the Hijri date and Sehri card | UI test for each source |
| 3 | Online: Aladhan HJCoSA + your `bd.json`, cached | fake-server tests, offline cache |
| 4 | The sighting question | ViewModel test: yes/no/no answer |
| 5 | Turn Sehri & Iftar on; Sehri alarm | phone checklist |

## Open choices

- **`bd.json` needs you** to update it on sighting nights. Without it, Bangladesh gets the
  calculation plus the sighting question — still correct if the user answers.
- The calculation default per country (Umm al-Qura vs Karachi for Fajr) affects Sehri by a couple
  of minutes; worth a per-country default.
