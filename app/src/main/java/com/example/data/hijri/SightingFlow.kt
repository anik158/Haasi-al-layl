package com.example.data.hijri

import java.time.LocalDate

/** The screens of the correction flow, in order. */
enum class SightingStep { MONTH, ANSWER, SOURCE, REVIEW, CONFIRM, DONE }

/**
 * Where the user is in "Moon sighting announced", and what they chose so far.
 *
 * Immutable, and nothing is saved until [confirm] - so cancelling, going back, rotating or the
 * process dying at any step leaves the calendar exactly as it was. Each step can only be left
 * forward with its choice made.
 */
data class SightingFlow(
  val step: SightingStep = SightingStep.MONTH,
  val month: HijriMonthId? = null,
  val option: SightingOption? = null,
  val heardFrom: HeardFrom? = null
) {
  fun chooseMonth(id: HijriMonthId) =
    if (step == SightingStep.MONTH) copy(step = SightingStep.ANSWER, month = id, option = null, heardFrom = null) else this

  fun chooseOption(choice: SightingOption) = when {
    step != SightingStep.ANSWER -> this
    // Undoing a correction needs no source.
    choice.answer == SightingAnswer.USE_ESTIMATE -> copy(step = SightingStep.REVIEW, option = choice, heardFrom = null)
    else -> copy(step = SightingStep.SOURCE, option = choice)
  }

  fun chooseSource(source: HeardFrom) =
    if (step == SightingStep.SOURCE) copy(step = SightingStep.REVIEW, heardFrom = source) else this

  /** First confirmation: the review was read. */
  fun reviewed() = if (step == SightingStep.REVIEW) copy(step = SightingStep.CONFIRM) else this

  /** Second confirmation. Only now does the caller save [decision]. */
  fun confirm() = if (step == SightingStep.CONFIRM && decision != null) copy(step = SightingStep.DONE) else this

  fun back(): SightingFlow = when (step) {
    SightingStep.MONTH, SightingStep.DONE -> this
    SightingStep.ANSWER -> SightingFlow()
    SightingStep.SOURCE -> copy(step = SightingStep.ANSWER, option = null)
    SightingStep.REVIEW -> if (option?.answer == SightingAnswer.USE_ESTIMATE) {
      copy(step = SightingStep.ANSWER, option = null)
    } else {
      copy(step = SightingStep.SOURCE, heardFrom = null)
    }
    SightingStep.CONFIRM -> copy(step = SightingStep.REVIEW)
  }

  /** What would be saved: complete only once every choice is made. */
  val decision: Decision?
    get() {
      val id = month ?: return null
      val choice = option ?: return null
      if (choice.answer != SightingAnswer.USE_ESTIMATE && heardFrom == null) return null
      return Decision(id, choice.answer, choice.startsOn, heardFrom)
    }

  data class Decision(
    val month: HijriMonthId,
    val answer: SightingAnswer,
    val startsOn: LocalDate,
    val heardFrom: HeardFrom?
  )
}
