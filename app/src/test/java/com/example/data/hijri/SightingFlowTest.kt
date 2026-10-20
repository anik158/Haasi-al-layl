package com.example.data.hijri

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

/** The screens can only be walked forward with a choice made, and nothing is decided before CONFIRM. */
class SightingFlowTest {

  private val ramadan = HijriMonthId(1448, 9)
  private val sighted = SightingOption(SightingAnswer.SIGHTED, LocalDate.of(2027, 2, 8), LocalDate.of(2027, 2, 7))
  private val estimate = SightingOption(SightingAnswer.USE_ESTIMATE, LocalDate.of(2027, 2, 9), LocalDate.of(2027, 2, 7))

  private fun walkedToConfirm() = SightingFlow()
    .chooseMonth(ramadan).chooseOption(sighted).chooseSource(HeardFrom.NATIONAL_COMMITTEE).reviewed()

  @Test
  fun `the full walk decides exactly what was chosen`() {
    val done = walkedToConfirm().confirm()
    assertEquals(SightingStep.DONE, done.step)
    assertEquals(
      SightingFlow.Decision(ramadan, SightingAnswer.SIGHTED, sighted.startsOn, HeardFrom.NATIONAL_COMMITTEE),
      done.decision
    )
  }

  @Test
  fun `undoing a correction skips the source`() {
    val flow = SightingFlow().chooseMonth(ramadan).chooseOption(estimate)
    assertEquals(SightingStep.REVIEW, flow.step)
    assertEquals(SightingStep.DONE, flow.reviewed().confirm().step)
    assertEquals(SightingStep.ANSWER, flow.back().step)
  }

  @Test
  fun `no decision before every choice is made`() {
    listOf(
      SightingFlow(),
      SightingFlow().chooseMonth(ramadan),
      SightingFlow().chooseMonth(ramadan).chooseOption(sighted)
    ).forEach { assertNull(it.step.name, it.decision) }
  }

  @Test
  fun `steps cannot be skipped`() {
    val start = SightingFlow()
    assertEquals(start, start.chooseOption(sighted))
    assertEquals(start, start.chooseSource(HeardFrom.TV_OR_NEWS))
    assertEquals(start, start.reviewed())
    assertEquals(start, start.confirm())
    val answer = start.chooseMonth(ramadan)
    assertEquals(answer, answer.chooseSource(HeardFrom.TV_OR_NEWS))
    assertEquals(answer, answer.confirm())
    val review = answer.chooseOption(sighted).chooseSource(HeardFrom.OTHER)
    assertEquals(review, review.confirm()) // the first confirmation cannot be skipped
  }

  @Test
  fun `back from every step goes one step back and forgets that step's choice`() {
    val confirm = walkedToConfirm()
    val review = confirm.back()
    assertEquals(SightingStep.REVIEW, review.step)
    val source = review.back()
    assertEquals(SightingStep.SOURCE, source.step)
    assertNull(source.heardFrom)
    val answer = source.back()
    assertEquals(SightingStep.ANSWER, answer.step)
    assertNull(answer.option)
    assertEquals(SightingFlow(), answer.back())
    assertEquals(SightingFlow(), SightingFlow().back())
  }

  @Test
  fun `done is final`() {
    val done = walkedToConfirm().confirm()
    assertEquals(done, done.back())
    assertEquals(done, done.chooseMonth(HijriMonthId(1448, 10)))
  }
}
