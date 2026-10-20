# Plan: finishing Sehri & Iftar

Status: **waiting for approval.** Nothing below is built yet.

## What already works (shipped)

- Today's **Sehri ends** (= Fajr) and **Iftar** (= Maghrib), with a countdown, offline.
- Forbidden / discouraged / recommended days, with checked references.
- Placement: under the prayer card in Ramadan, above Today's verse otherwise.

## What is missing — the "half-cooked" part

1. **Which days are Ramadan, Eid and Arafah is a calculation.** It can be a day off from the
   moon sighting in your country, at the start and the end of the month. The card says so, but
   the app cannot correct itself; only the manual Hijri adjustment in Settings can.
2. **No Sehri alarm.** Nothing wakes the user for Sehri.

## The plan — three steps, each tested before it reaches your phone

### Step 1 — "Was the moon sighted?" (fixes the month start and the Eid)

On the **evening of the 29th** of Sha'ban, Ramadan, Shawwal (for Dhul-Qa'dah) and Dhul-Qa'dah (for
Dhul-Hijjah), after Maghrib, Today shows one card:

> **Was the new moon of Ramadan sighted in your country tonight?**
> [ Yes — Ramadan starts tomorrow ]  [ No — Sha'ban completes 30 days ]

- The answer is saved **for that month only** (not one offset for every month, as now).
- No answer: the calculated date stays, with its "may differ by a day" note.
- Works fully offline; the user decides from their country's announcement (TV, mosque, news).
- Test first: a ViewModel test for yes / no / no answer, and that Eid, Arafah and the forbidden
  days move with the answer.

### Step 2 — Sehri alarm

- A new setting: **Sehri reminder**, off by default, with "how long before Sehri ends":
  30 / 45 / 60 / 90 minutes.
- Rings like the prayer alarm (5 seconds), only on days the user is fasting: every day of
  Ramadan, plus days the user taps "I'm fasting tomorrow" on the card.
- No Iftar alarm of its own — Iftar is Maghrib, which already has the prayer alert.
- Test first: the alarm planner arms it at the right minute, only on fasting days, and never on a
  forbidden day.

### Step 3 — Online help (optional, needs internet)

- When online, the app asks **Aladhan** (free, no key) for **Saudi Arabia's announced date** and
  shows it as context: "Saudi Arabia: Ramadan began on …".
- **It never overrides** the user's answer or the calculation for Bangladesh — no public service
  publishes Bangladesh's sighting.
- Test first: a fake server; offline it simply shows nothing.

## What I need from you

- **Approve** the three steps (or drop any — e.g. step 3).
- **Sehri reminder default:** off, with 60 minutes as the suggested lead time — OK?
- **"I'm fasting tomorrow" for voluntary fasts:** include it in step 2, or Ramadan-only alarm?

Nothing changes in the app until you say yes.
