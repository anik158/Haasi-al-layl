# Plan: finishing Sehri & Iftar

Status: **waiting for approval.** Nothing below is built yet.

## What already works (shipped)

- Today's **Sehri ends** (= Fajr) and **Iftar** (= Maghrib), with a countdown, offline.
- Forbidden / discouraged / recommended days, with checked references.
- Placement: under the prayer card in Ramadan, above Today's verse otherwise.

## What is missing — the "half-cooked" part

1. **Which days are Ramadan, Eid and Arafah is a calculation**: Umm al-Qura (`HijrahDate`) plus
   one fixed shift for every month (−1 for BD/IN/PK/NP/LK/MA, or the user's choice in Settings).
   It can be a day off from the moon sighting in the user's country, and the app cannot correct
   itself.
2. **The app cannot find "the 29th" reliably either.** It counts days from that same calculation,
   so its 29th can really be the local 28th or 30th.
3. **No Sehri alarm.** Nothing wakes the user for Sehri.

## How the date is actually decided (the facts the plan is built on)

- **Saudi Arabia:** the High Judiciary Council announces the start of Muharram, Ramadan, Shawwal
  and Dhul-Hijjah after a sighting. Aladhan publishes it (`calendarMethod=HJCoSA`, free, no key).
- **Turkey:** Diyanet calculates its calendar in advance. Aladhan publishes it (`DIYANET`).
- **Bangladesh:** the **National Moon Sighting Committee** (chaired by the religious affairs
  minister, hosted by the Islamic Foundation at Baitul Mukarram) meets on the evening a month can
  start. It reviews reports from district administrations, the Meteorological Department, SPARRSO
  and Islamic Foundation offices, then announces through a press release, TV and newspapers. It
  does this for ordinary months too (e.g. Jumada al-Ula), not only Ramadan and Eid. Newspapers
  print the Hijri date by counting days from the latest announcement. **There is no API or data
  feed.**
- In practice Bangladesh starts a month on the **same day as Saudi Arabia or one day later**.

## The plan — three steps, each tested before it reaches your phone

### Step 1 — Know when the month starts: online first, then the user, checked by the moon

**1a. Online first.** When there is internet, fetch the announced dates from Aladhan, cache them
for offline use, refresh once a day and on the evenings a month can start.

- Saudi Arabia (and countries that follow it): `HJCoSA` **is** the answer. No question is asked.
- Turkey: `DIYANET` is the answer.
- Bangladesh and other sighting countries: the Saudi start narrows the local start to two days,
  the same day or the next. It is shown as context ("Saudi Arabia: Ramadan began on …"), never as
  Bangladesh's date.

**1b. Find the right evening from the sky, not the calendar.** `MoonCalculator` already computes
where the moon is. Add the moment of new moon (when the moon passes the sun) and, for the user's
location on each evening, the moon's age at sunset, how long it stays up after sunset, and its
height. The sighting question appears on the **evening after the new moon** (falling back to the
next evening if not answered), within the calculated 29th/30th. A calendar that is a day off no
longer moves the question to the wrong evening.

**1c. Ask the user, with double confirmation.** After Maghrib on that evening, Today shows:

> **Has the new moon of Ramadan been announced in your country tonight?**
> [ Yes — sighted ]  [ No — not sighted ]  [ Not announced yet ]

- "Not announced yet" is needed: Bangladesh's committee often announces an hour or more after
  Maghrib. The card stays and asks again later that evening.
- **Second confirmation** after Yes or No, with the result spelled out:
  > Ramadan starts tomorrow, Thursday 19 February. First Sehri ends at 4:58. Confirm?

  with Saudi Arabia's date and the moon check (1d) as context.
- **Undo** until the next Maghrib, in case of a wrong tap or a changed announcement.
- The answer is saved **for that month only**, as that month's start date. The next month's
  29th is then counted from it exactly, so after one confirmed month every later question lands on
  the right evening.
- No answer: the best other source stays (Saudi + one day for BD, else the calculation).

**1d. The moon check — a warning, never a block.** From the numbers in 1b, a standard crescent
visibility test (Yallop / Odeh) sorts the evening:

| Tonight's moon | Meaning | What the app does |
| --- | --- | --- |
| Not possible — the moon sets before the sun, or the new moon has not happened yet | A sighting is physically impossible | A "Yes" gets a clear warning on the confirm screen |
| Hard to see — could go either way | Either answer is believable | No warning |
| Easy to see | A "No" is unusual, but cloud and haze happen | A "No" gets a gentle note |

The committee has the final word, so the app always accepts the user's confirmed answer.

**1e. The Ramadan warning stays.** Whatever the source, during the evenings a month can start and
the first days of Ramadan, the Sehri & Iftar card keeps a note:
*"Ramadan's start follows the moon sighting announced in your country. Check your local
announcement."* It is reworded to the source: calculated / Saudi announcement / confirmed by you.

**Tests first:**
- Moon: new-moon times against published tables; the visibility test against past Bangladesh
  announcements from news archives (both "sighted" and "not sighted" evenings).
- The question lands on the evening after the new moon even when the calendar is set a day off.
- ViewModel: Yes→confirm, Yes→cancel, No, Not announced yet, undo, no answer; Eid, Arafah and the
  forbidden days move with the answer; the warning on an impossible "Yes".
- Online: a fake server for HJCoSA / DIYANET; offline uses the cache, or nothing.

### Step 2 — Sehri alarm

- A new setting: **Sehri reminder**, off by default, with "how long before Sehri ends":
  30 / 45 / 60 / 90 minutes.
- Rings like the prayer alarm (5 seconds), only on days the user is fasting: every day of
  Ramadan, plus days the user taps "I'm fasting tomorrow" on the card.
- No Iftar alarm of its own — Iftar is Maghrib, which already has the prayer alert.
- Test first: the alarm planner arms it at the right minute, only on fasting days, and never on a
  forbidden day.

### Not in this plan

- **A Bangladesh feed** (`bd.json` updated on sighting nights, or reading the Islamic Foundation's
  notices). Deferred: it needs someone to update it every month, and scraping Bengali press
  releases is fragile. Without it, Bangladesh relies on the user's answer, narrowed by Saudi
  Arabia and checked by the moon.

## What I need from you

- **Approve** steps 1 and 2.
- **Sehri reminder default:** off, with 60 minutes as the suggested lead time — OK?
- **"I'm fasting tomorrow" for voluntary fasts:** include it in step 2, or Ramadan-only alarm?

Nothing changes in the app until you say yes.
