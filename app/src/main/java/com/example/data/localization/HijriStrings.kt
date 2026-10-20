package com.example.data.localization

import com.example.data.hijri.Authority
import com.example.data.hijri.HeardFrom
import com.example.data.hijri.HistoryAction
import com.example.data.hijri.MonthStartSource

/**
 * Copy for the month-start warnings, the "Moon sighting announced" correction and the Sehri
 * reminder. Kept apart from LocalizationManager, which is already very long; same shape - one
 * function per string, every language spelled out.
 */
object HijriStrings {

  private fun pick(language: AppLanguage, en: String, ar: String, bn: String, es: String, pt: String) =
    when (language) {
      AppLanguage.ENGLISH -> en
      AppLanguage.ARABIC -> ar
      AppLanguage.BENGALI -> bn
      AppLanguage.SPANISH -> es
      AppLanguage.PORTUGUESE -> pt
    }

  // --- Warnings on the days that matter ---------------------------------------------------------

  fun ramadanFollowsSighting(l: AppLanguage) = pick(
    l,
    "Ramadan's start follows the moon sighting announced in your country.",
    "بداية رمضان تتبع رؤية الهلال المعلنة في بلدك.",
    "রমজানের শুরু আপনার দেশে ঘোষিত চাঁদ দেখার ওপর নির্ভর করে।",
    "El inicio del Ramadán sigue el avistamiento de la luna anunciado en su país.",
    "O início do Ramadão segue o avistamento da lua anunciado no seu país."
  )

  fun dateDependsOnSighting(l: AppLanguage) = pick(
    l,
    "This day depends on the moon sighting announced in your country.",
    "هذا اليوم يتبع رؤية الهلال المعلنة في بلدك.",
    "এই দিনটি আপনার দেশে ঘোষিত চাঁদ দেখার ওপর নির্ভর করে।",
    "Este día depende del avistamiento de la luna anunciado en su país.",
    "Este dia depende do avistamento da lua anunciado no seu país."
  )

  fun sourceUser(heard: HeardFrom?, l: AppLanguage) = pick(
    l,
    "Month start set by you" + (heard?.let { " (${heardFrom(it, l).lowercase()})" } ?: "") + ".",
    "بداية الشهر حددتها أنت" + (heard?.let { " (${heardFrom(it, l)})" } ?: "") + ".",
    "মাসের শুরু আপনি ঠিক করেছেন" + (heard?.let { " (${heardFrom(it, l)})" } ?: "") + "।",
    "Inicio del mes fijado por usted" + (heard?.let { " (${heardFrom(it, l).lowercase()})" } ?: "") + ".",
    "Início do mês definido por si" + (heard?.let { " (${heardFrom(it, l).lowercase()})" } ?: "") + "."
  )

  fun sourceAnnounced(authority: Authority, l: AppLanguage) = when (authority) {
    Authority.SAUDI -> pick(
      l,
      "Date as announced in Saudi Arabia. If your country announces another, set it in Settings.",
      "التاريخ كما أُعلن في السعودية. إن أعلن بلدك تاريخاً آخر فحدده من الإعدادات.",
      "সৌদি আরবে ঘোষিত তারিখ। আপনার দেশ অন্য তারিখ ঘোষণা করলে সেটিংসে ঠিক করুন।",
      "Fecha anunciada en Arabia Saudí. Si su país anuncia otra, fíjela en Ajustes.",
      "Data anunciada na Arábia Saudita. Se o seu país anunciar outra, defina-a nas Definições."
    )
    Authority.TURKEY -> pick(
      l,
      "Date from Diyanet's calendar (Turkey). If your country announces another, set it in Settings.",
      "التاريخ من تقويم ديانت (تركيا). إن أعلن بلدك تاريخاً آخر فحدده من الإعدادات.",
      "দিয়ানেতের (তুরস্ক) ক্যালেন্ডারের তারিখ। আপনার দেশ অন্য তারিখ ঘোষণা করলে সেটিংসে ঠিক করুন।",
      "Fecha del calendario de Diyanet (Turquía). Si su país anuncia otra, fíjela en Ajustes.",
      "Data do calendário da Diyanet (Turquia). Se o seu país anunciar outra, defina-a nas Definições."
    )
  }

  /**
   * "Saudi Arabia: Ramadan began Wednesday, 18 February. Here it is expected on Thursday, 19
   * February, based on the moon." For a month still ahead, Saudi Arabia's date is its calendar's
   * prediction, not an announcement, and says so.
   */
  fun sourceMoonEstimate(month: String, saudiDate: String?, localDate: String, saudiStarted: Boolean, l: AppLanguage): String {
    val saudi = saudiDate?.let {
      if (saudiStarted) {
        pick(
          l,
          "Saudi Arabia: $month began $it. ",
          "السعودية: بدأ $month يوم $it. ",
          "সৌদি আরব: $month শুরু হয়েছে $it। ",
          "Arabia Saudí: $month empezó el $it. ",
          "Arábia Saudita: $month começou a $it. "
        )
      } else {
        pick(
          l,
          "Saudi Arabia's calendar: $month starts $it. ",
          "تقويم السعودية: يبدأ $month يوم $it. ",
          "সৌদি আরবের ক্যালেন্ডারে: $month শুরু $it। ",
          "Calendario de Arabia Saudí: $month empieza el $it. ",
          "Calendário da Arábia Saudita: $month começa a $it. "
        )
      }
    }.orEmpty()
    return saudi + pick(
      l,
      "Here it is expected on $localDate, based on the moon. Confirm with your country's announcement.",
      "ويُتوقع هنا يوم $localDate بناءً على الهلال. تأكد من إعلان بلدك.",
      "চাঁদ অনুযায়ী এখানে $localDate শুরু হওয়ার কথা। আপনার দেশের ঘোষণা দেখে নিশ্চিত হোন।",
      "Aquí se espera el $localDate, según la luna. Confírmelo con el anuncio de su país.",
      "Aqui espera-se a $localDate, pela lua. Confirme com o anúncio do seu país."
    )
  }

  fun sourceLabel(source: MonthStartSource, authority: Authority, l: AppLanguage) = when (source) {
    MonthStartSource.USER -> pick(l, "set by you", "حددتها أنت", "আপনি ঠিক করেছেন", "fijado por usted", "definido por si")
    MonthStartSource.ANNOUNCED -> if (authority == Authority.TURKEY) {
      pick(l, "Diyanet (Turkey)", "ديانت (تركيا)", "দিয়ানেত (তুরস্ক)", "Diyanet (Turquía)", "Diyanet (Turquia)")
    } else {
      pick(l, "announced in Saudi Arabia", "معلن في السعودية", "সৌদি আরবে ঘোষিত", "anunciado en Arabia Saudí", "anunciado na Arábia Saudita")
    }
    MonthStartSource.MOON_ESTIMATE -> pick(
      l,
      "estimated from Saudi Arabia and the moon",
      "تقدير من السعودية والهلال",
      "সৌদি আরব ও চাঁদ থেকে অনুমান",
      "estimado desde Arabia Saudí y la luna",
      "estimado pela Arábia Saudita e pela lua"
    )
    MonthStartSource.CALCULATED -> pick(
      l,
      "calculated (Umm al-Qura), may differ by a day",
      "محسوب (أم القرى)، قد يختلف يوماً",
      "হিসাব করা (উম্মুল কুরা), এক দিন পার্থক্য হতে পারে",
      "calculado (Umm al-Qura), puede variar un día",
      "calculado (Umm al-Qura), pode variar um dia"
    )
  }

  // --- Settings card ----------------------------------------------------------------------------

  fun cardTitle(l: AppLanguage) = pick(
    l, "Moon sighting announced", "إعلان رؤية الهلال", "চাঁদ দেখার ঘোষণা", "Avistamiento anunciado", "Avistamento anunciado"
  )

  fun cardDesc(l: AppLanguage) = pick(
    l,
    "The month start is worked out automatically. Use this only when your country's announcement differs from the date shown - it changes one month at a time.",
    "تُحسب بداية الشهر تلقائياً. استخدم هذا فقط إن اختلف إعلان بلدك عن التاريخ المعروض - يغيّر شهراً واحداً في كل مرة.",
    "মাসের শুরু স্বয়ংক্রিয়ভাবে নির্ধারিত হয়। আপনার দেশের ঘোষণা দেখানো তারিখ থেকে আলাদা হলেই এটি ব্যবহার করুন — একবারে একটি মাস বদলায়।",
    "El inicio del mes se calcula solo. Úselo solo si el anuncio de su país difiere de la fecha mostrada; cambia un mes cada vez.",
    "O início do mês é calculado automaticamente. Use isto só se o anúncio do seu país diferir da data mostrada; muda um mês de cada vez."
  )

  fun openFlow(l: AppLanguage) = pick(
    l, "Report the announcement", "أبلغ عن الإعلان", "ঘোষণা জানান", "Indicar el anuncio", "Indicar o anúncio"
  )

  fun nothingToCorrect(l: AppLanguage) = pick(
    l,
    "Nothing to change today. A month can be set from its 28th day to the 3rd day of the next.",
    "لا شيء يُغيَّر اليوم. يمكن تحديد الشهر من يومه الثامن والعشرين حتى اليوم الثالث من الشهر التالي.",
    "আজ বদলানোর কিছু নেই। মাসের ২৮ তারিখ থেকে পরের মাসের ৩ তারিখ পর্যন্ত ঠিক করা যায়।",
    "Hoy no hay nada que cambiar. Un mes puede fijarse desde su día 28 hasta el día 3 del siguiente.",
    "Hoje não há nada a mudar. Um mês pode ser definido do dia 28 ao dia 3 do seguinte."
  )

  fun historyTitle(l: AppLanguage) = pick(l, "Recent changes", "التغييرات الأخيرة", "সাম্প্রতিক পরিবর্তন", "Cambios recientes", "Alterações recentes")

  fun historyAction(action: HistoryAction, l: AppLanguage) = when (action) {
    HistoryAction.SET -> pick(l, "Set", "حُدد", "ঠিক করা", "Fijado", "Definido")
    HistoryAction.UNDO -> pick(l, "Undone", "تراجع", "ফিরিয়ে নেওয়া", "Deshecho", "Desfeito")
    HistoryAction.CLEARED -> pick(l, "Back to the estimate", "عودة إلى التقدير", "অনুমানে ফেরা", "Vuelta a la estimación", "De volta à estimativa")
  }

  fun heardFrom(h: HeardFrom, l: AppLanguage) = when (h) {
    HeardFrom.NATIONAL_COMMITTEE -> pick(l, "National moon sighting committee", "لجنة رؤية الهلال الوطنية", "জাতীয় চাঁদ দেখা কমিটি", "Comité nacional de avistamiento", "Comité nacional de avistamento")
    HeardFrom.LOCAL_MOSQUE -> pick(l, "Local mosque", "المسجد المحلي", "স্থানীয় মসজিদ", "Mezquita local", "Mesquita local")
    HeardFrom.TV_OR_NEWS -> pick(l, "TV or news", "التلفاز أو الأخبار", "টিভি বা সংবাদ", "Televisión o noticias", "Televisão ou notícias")
    HeardFrom.OTHER -> pick(l, "Other", "غير ذلك", "অন্যান্য", "Otro", "Outro")
  }

  // --- The flow ---------------------------------------------------------------------------------

  fun stepTitle(step: com.example.data.hijri.SightingStep, l: AppLanguage) = when (step) {
    com.example.data.hijri.SightingStep.MONTH -> pick(l, "Which month?", "أي شهر؟", "কোন মাস?", "¿Qué mes?", "Que mês?")
    com.example.data.hijri.SightingStep.ANSWER -> pick(l, "What was announced?", "ماذا أُعلن؟", "কী ঘোষণা হয়েছে?", "¿Qué se anunció?", "O que foi anunciado?")
    com.example.data.hijri.SightingStep.SOURCE -> pick(l, "Where did you hear it?", "من أين سمعت؟", "কোথা থেকে জেনেছেন?", "¿Dónde lo oyó?", "Onde ouviu?")
    com.example.data.hijri.SightingStep.REVIEW -> pick(l, "Check what changes", "راجع ما سيتغير", "কী বদলাবে দেখে নিন", "Revise lo que cambia", "Reveja o que muda")
    com.example.data.hijri.SightingStep.CONFIRM -> pick(l, "Confirm", "تأكيد", "নিশ্চিত করুন", "Confirmar", "Confirmar")
    com.example.data.hijri.SightingStep.DONE -> pick(l, "Saved", "تم الحفظ", "সংরক্ষিত", "Guardado", "Guardado")
  }

  fun stepCounter(index: Int, total: Int, l: AppLanguage) = LocalizationManager.localizeDigits(
    pick(l, "Step $index of $total", "الخطوة $index من $total", "ধাপ $index / $total", "Paso $index de $total", "Passo $index de $total"),
    l
  )

  fun monthSuffix(month: Int, l: AppLanguage): String = when (month) {
    com.example.data.hijri.HijriMonthId.SHAWWAL -> " - " + pick(l, "Eid al-Fitr", "عيد الفطر", "ঈদুল ফিতর", "Eid al-Fitr", "Eid al-Fitr")
    com.example.data.hijri.HijriMonthId.DHUL_HIJJAH -> " - " + pick(l, "Eid al-Adha", "عيد الأضحى", "ঈদুল আজহা", "Eid al-Adha", "Eid al-Adha")
    else -> ""
  }

  fun optionSighted(newMonth: String, date: String, l: AppLanguage) = pick(
    l,
    "Moon sighted - $newMonth starts $date",
    "رُئي الهلال - يبدأ $newMonth يوم $date",
    "চাঁদ দেখা গেছে — $newMonth শুরু $date",
    "Luna avistada: $newMonth empieza el $date",
    "Lua avistada: $newMonth começa a $date"
  )

  fun optionNotSighted(oldMonth: String, newMonth: String, date: String, l: AppLanguage) = pick(
    l,
    "Moon not sighted - $oldMonth completes 30 days, $newMonth starts $date",
    "لم يُرَ الهلال - يُكمل $oldMonth ثلاثين يوماً، ويبدأ $newMonth يوم $date",
    "চাঁদ দেখা যায়নি — $oldMonth ৩০ দিন পূর্ণ হবে, $newMonth শুরু $date",
    "Luna no avistada: $oldMonth completa 30 días, $newMonth empieza el $date",
    "Lua não avistada: $oldMonth completa 30 dias, $newMonth começa a $date"
  )

  fun optionEstimate(date: String, l: AppLanguage) = pick(
    l,
    "Use the app's estimate again - $date",
    "العودة إلى تقدير التطبيق - $date",
    "আবার অ্যাপের অনুমান ব্যবহার করুন — $date",
    "Volver a la estimación de la app: $date",
    "Voltar à estimativa da app: $date"
  )

  fun eveningOf(date: String, l: AppLanguage) = pick(
    l,
    "Sighting evening: $date, after sunset",
    "مساء الرؤية: $date بعد الغروب",
    "চাঁদ দেখার সন্ধ্যা: $date, সূর্যাস্তের পর",
    "Tarde del avistamiento: $date, tras la puesta de sol",
    "Tarde do avistamento: $date, após o pôr do sol"
  )

  fun changeLabel(key: String, monthName: String, l: AppLanguage) = when (key) {
    "month_start" -> pick(l, "$monthName starts", "بداية $monthName", "$monthName শুরু", "Empieza $monthName", "Começa $monthName")
    "ramadan_first_day" -> pick(l, "First day of fasting", "أول أيام الصيام", "প্রথম রোজা", "Primer día de ayuno", "Primeiro dia de jejum")
    "ramadan_last_day" -> pick(l, "Last day of fasting", "آخر أيام الصيام", "শেষ রোজা", "Último día de ayuno", "Último dia de jejum")
    "eid_fitr" -> pick(l, "Eid al-Fitr (no fasting)", "عيد الفطر (لا صيام)", "ঈদুল ফিতর (রোজা নিষেধ)", "Eid al-Fitr (no se ayuna)", "Eid al-Fitr (sem jejum)")
    "arafah" -> pick(l, "Day of Arafah", "يوم عرفة", "আরাফার দিন", "Día de Arafah", "Dia de Arafah")
    "eid_adha" -> pick(l, "Eid al-Adha (no fasting)", "عيد الأضحى (لا صيام)", "ঈদুল আজহা (রোজা নিষেধ)", "Eid al-Adha (no se ayuna)", "Eid al-Adha (sem jejum)")
    "tashreeq" -> pick(l, "Days of Tashreeq begin (no fasting)", "بداية أيام التشريق (لا صيام)", "তাশরিকের দিন শুরু (রোজা নিষেধ)", "Empiezan los días de Tashriq (no se ayuna)", "Começam os dias de Tashriq (sem jejum)")
    "ashura" -> pick(l, "Ashura", "عاشوراء", "আশুরা", "Ashura", "Ashura")
    "first_sehri" -> pick(l, "First Sehri ends", "انتهاء أول سحور", "প্রথম সাহরির শেষ সময়", "Fin del primer suhur", "Fim do primeiro suhur")
    "sehri_alarm" -> pick(l, "Sehri reminder moves with it", "يتحرك منبّه السحور معه", "সাহরির রিমাইন্ডারও সরবে", "El recordatorio de suhur se mueve también", "O lembrete do suhur muda também")
    else -> key
  }

  fun unchanged(l: AppLanguage) = pick(l, "unchanged", "دون تغيير", "অপরিবর্তিত", "sin cambios", "sem alteração")

  fun warningImpossible(l: AppLanguage) = pick(
    l,
    "The moon had already set before sunset here that evening, or the new moon had not happened yet - a sighting was not possible. Are you sure this was the announcement?",
    "كان القمر قد غاب قبل الشمس هنا ذلك المساء، أو لم يحدث الاقتران بعد - فالرؤية لم تكن ممكنة. هل أنت متأكد أن هذا هو الإعلان؟",
    "সেই সন্ধ্যায় এখানে সূর্যের আগেই চাঁদ অস্ত গিয়েছিল, অথবা নতুন চাঁদ তখনও হয়নি — চাঁদ দেখা সম্ভব ছিল না। আপনি কি নিশ্চিত যে এটাই ঘোষণা?",
    "Esa tarde la luna ya se había puesto antes que el sol aquí, o aún no había luna nueva: no era posible verla. ¿Seguro que este fue el anuncio?",
    "Nessa tarde a lua já se tinha posto antes do sol aqui, ou ainda não havia lua nova: não era possível vê-la. Tem a certeza de que foi este o anúncio?"
  )

  fun noteEasilyVisible(l: AppLanguage) = pick(
    l,
    "The crescent was easy to see here that evening, though cloud or haze can hide it. Keep this answer only if it was announced.",
    "كان الهلال سهل الرؤية هنا ذلك المساء، وإن كان الغيم أو الغبار قد يحجبه. أبقِ هذه الإجابة فقط إن أُعلنت.",
    "সেই সন্ধ্যায় এখানে চাঁদ সহজেই দেখা যাওয়ার কথা, যদিও মেঘ বা কুয়াশা আড়াল করতে পারে। ঘোষণা হয়ে থাকলেই এই উত্তর রাখুন।",
    "Esa tarde el creciente era fácil de ver aquí, aunque las nubes o la bruma pueden ocultarlo. Mantenga esta respuesta solo si se anunció.",
    "Nessa tarde o crescente era fácil de ver aqui, embora nuvens ou neblina o possam esconder. Mantenha esta resposta só se foi anunciada."
  )

  fun reviewContinue(l: AppLanguage) = pick(
    l, "I've checked - continue", "راجعت - متابعة", "দেখে নিয়েছি — এগিয়ে যান", "Lo he revisado: continuar", "Já revi: continuar"
  )

  fun confirmQuestion(month: String, date: String, l: AppLanguage) = pick(
    l,
    "Change $month to start on $date?",
    "هل تريد أن يبدأ $month يوم $date؟",
    "$month কি $date থেকে শুরু হবে?",
    "¿Cambiar el inicio de $month al $date?",
    "Mudar o início de $month para $date?"
  )

  fun confirmButton(weekday: String, l: AppLanguage) = pick(
    l, "Change to $weekday", "غيّر إلى $weekday", "$weekday করুন", "Cambiar a $weekday", "Mudar para $weekday"
  )

  fun confirmEstimateButton(l: AppLanguage) = pick(
    l, "Use the estimate", "استخدم التقدير", "অনুমান ব্যবহার করুন", "Usar la estimación", "Usar a estimativa"
  )

  fun waitSeconds(seconds: Int, l: AppLanguage) = LocalizationManager.localizeDigits(
    pick(l, "Read the warning ($seconds)", "اقرأ التحذير ($seconds)", "সতর্কতা পড়ুন ($seconds)", "Lea el aviso ($seconds)", "Leia o aviso ($seconds)"),
    l
  )

  fun cancel(l: AppLanguage) = pick(l, "Cancel", "إلغاء", "বাতিল", "Cancelar", "Cancelar")
  fun back(l: AppLanguage) = pick(l, "Back", "رجوع", "পেছনে", "Atrás", "Voltar")
  fun close(l: AppLanguage) = pick(l, "Close", "إغلاق", "বন্ধ", "Cerrar", "Fechar")

  fun saved(month: String, date: String, l: AppLanguage) = pick(
    l,
    "Saved. $month starts $date. You can undo it on Today until the next Maghrib.",
    "تم الحفظ. يبدأ $month يوم $date. يمكنك التراجع من صفحة اليوم حتى المغرب القادم.",
    "সংরক্ষিত। $month শুরু $date। পরের মাগরিব পর্যন্ত আজকের পাতা থেকে ফিরিয়ে নিতে পারবেন।",
    "Guardado. $month empieza el $date. Puede deshacerlo en Hoy hasta el próximo Magrib.",
    "Guardado. $month começa a $date. Pode desfazer em Hoje até ao próximo Maghrib."
  )

  // --- Today ------------------------------------------------------------------------------------

  fun undoBanner(month: String, date: String, l: AppLanguage) = pick(
    l,
    "$month start set by you - $date.",
    "حددت بداية $month - $date.",
    "$month-এর শুরু আপনি ঠিক করেছেন — $date।",
    "Inicio de $month fijado por usted: $date.",
    "Início de $month definido por si: $date."
  )

  fun undo(l: AppLanguage) = pick(l, "Undo", "تراجع", "ফিরিয়ে নিন", "Deshacer", "Desfazer")

  // --- Sehri reminder ---------------------------------------------------------------------------

  fun sehriReminderTitle(l: AppLanguage) = pick(l, "Sehri reminder", "منبّه السحور", "সাহরির রিমাইন্ডার", "Recordatorio de suhur", "Lembrete do suhur")

  fun sehriReminderDesc(l: AppLanguage) = pick(
    l,
    "Rings before Sehri ends on the days you fast: every day of Ramadan, and days you mark on the Sehri & Iftar card. Never on a day fasting is forbidden.",
    "يرن قبل انتهاء السحور في أيام صيامك: كل أيام رمضان، والأيام التي تحددها في بطاقة السحور والإفطار. ولا يرن في يوم يحرم صومه.",
    "যেদিন রোজা রাখবেন সেদিন সাহরি শেষ হওয়ার আগে বাজবে: রমজানের প্রতিদিন, আর সাহরি ও ইফতার কার্ডে যে দিন চিহ্নিত করবেন। রোজা নিষিদ্ধ দিনে কখনো নয়।",
    "Suena antes del fin del suhur los días que ayune: todo el Ramadán y los días que marque en la tarjeta. Nunca en un día en que ayunar está prohibido.",
    "Toca antes do fim do suhur nos dias em que jejuar: todo o Ramadão e os dias que marcar no cartão. Nunca num dia em que jejuar é proibido."
  )

  fun sehriLeadLabel(l: AppLanguage) = pick(
    l, "How long before Sehri ends", "قبل انتهاء السحور بـ", "সাহরি শেষ হওয়ার কতক্ষণ আগে", "Cuánto antes del fin del suhur", "Quanto antes do fim do suhur"
  )

  fun minutes(n: Int, l: AppLanguage) = LocalizationManager.localizeDigits(
    pick(l, "$n min", "$n دقيقة", "$n মিনিট", "$n min", "$n min"), l
  )

  fun fastingTomorrowButton(tomorrow: Boolean, l: AppLanguage) = if (tomorrow) {
    pick(l, "I'm fasting tomorrow - remind me for Sehri", "سأصوم غداً - نبّهني للسحور", "আগামীকাল রোজা রাখব — সাহরিতে জাগাবেন", "Ayuno mañana: avíseme para el suhur", "Jejuo amanhã: avise-me para o suhur")
  } else {
    pick(l, "I'm fasting today - remind me for Sehri", "سأصوم اليوم - نبّهني للسحور", "আজ রোজা রাখব — সাহরিতে জাগাবেন", "Ayuno hoy: avíseme para el suhur", "Jejuo hoje: avise-me para o suhur")
  }

  fun fastingMarked(l: AppLanguage) = pick(
    l, "Sehri reminder set - tap to cancel", "منبّه السحور مضبوط - اضغط للإلغاء", "সাহরির রিমাইন্ডার চালু — বাতিল করতে চাপুন", "Recordatorio puesto: toque para cancelar", "Lembrete ativo: toque para cancelar"
  )

  fun sehriAlertTitle(time: String, l: AppLanguage) = pick(
    l, "Sehri ends at $time", "ينتهي السحور الساعة $time", "সাহরির শেষ সময় $time", "El suhur termina a las $time", "O suhur termina às $time"
  )

  fun sehriAlertBody(minutes: Int, l: AppLanguage) = LocalizationManager.localizeDigits(
    pick(
      l,
      "$minutes minutes left to eat and drink. \"Take Sehri, for in Sehri there is blessing.\" (Bukhari 1923)",
      "بقي $minutes دقيقة للأكل والشرب. «تسحروا فإن في السحور بركة» (البخاري 1923)",
      "খাওয়া-দাওয়ার জন্য আর $minutes মিনিট বাকি। \"তোমরা সাহরি খাও, কেননা সাহরিতে বরকত আছে।\" (বুখারি ১৯২৩)",
      "Quedan $minutes minutos para comer y beber. «Tomad el suhur, pues en él hay bendición» (Bujari 1923)",
      "Restam $minutes minutos para comer e beber. «Tomai o suhur, pois nele há bênção» (Bukhari 1923)"
    ),
    l
  )
}
