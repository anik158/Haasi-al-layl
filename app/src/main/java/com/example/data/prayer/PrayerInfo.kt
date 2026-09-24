package com.example.data.prayer

import com.example.data.localization.AppLanguage
import com.example.data.model.PrayerName

/** One translated string, as a record - the same shape PrayerAlertMessages uses. */
private data class Copy(
  val en: String,
  val ar: String,
  val bn: String,
  val es: String,
  val pt: String
) {
  fun get(language: AppLanguage): String = when (language) {
    AppLanguage.ENGLISH -> en
    AppLanguage.ARABIC -> ar
    AppLanguage.BENGALI -> bn
    AppLanguage.SPANISH -> es
    AppLanguage.PORTUGUESE -> pt
  }
}

/**
 * What each entry on the day's schedule means, for the schedule popup.
 *
 * Deliberately conservative: the meaning of the name, the obligatory rak'ah count and the time
 * window - things the schools agree on. Where they differ (how long Asr's shadow must be, when
 * Isha's preferred time ends) the text says so instead of choosing one. Imsak and Sunrise are
 * markers rather than prayers, and say that too, since both appear in the list beside the five.
 */
object PrayerInfo {

  fun meaning(prayer: PrayerName, language: AppLanguage): String =
    MEANINGS.getValue(prayer).get(language)

  fun description(prayer: PrayerName, language: AppLanguage): String =
    DESCRIPTIONS.getValue(prayer).get(language)

  private val MEANINGS: Map<PrayerName, Copy> = mapOf(
    PrayerName.IMSAK to Copy(
      en = "Holding back",
      ar = "الكفّ والإمساك",
      bn = "বিরত থাকা",
      es = "Abstenerse",
      pt = "Abster-se"
    ),
    PrayerName.FAJR to Copy(
      en = "Dawn",
      ar = "أول ضوء النهار",
      bn = "ভোর",
      es = "Alba",
      pt = "Alvorada"
    ),
    PrayerName.SUNRISE to Copy(
      en = "Sunrise",
      ar = "طلوع الشمس",
      bn = "সূর্যোদয়",
      es = "Salida del sol",
      pt = "Nascer do sol"
    ),
    PrayerName.DHUHR to Copy(
      en = "Midday",
      ar = "الظهيرة",
      bn = "মধ্যাহ্ন",
      es = "Mediodía",
      pt = "Meio-dia"
    ),
    PrayerName.ASR to Copy(
      en = "Afternoon",
      ar = "آخر النهار",
      bn = "অপরাহ্ণ",
      es = "Tarde",
      pt = "Tarde"
    ),
    PrayerName.MAGHRIB to Copy(
      en = "Sunset",
      ar = "الغروب",
      bn = "সূর্যাস্ত",
      es = "Puesta del sol",
      pt = "Pôr do sol"
    ),
    PrayerName.ISHA to Copy(
      en = "Night",
      ar = "أول ظلام الليل",
      bn = "রাত",
      es = "Noche",
      pt = "Noite"
    )
  )

  private val DESCRIPTIONS: Map<PrayerName, Copy> = mapOf(
    PrayerName.IMSAK to Copy(
      en = "Not a prayer. A precautionary point shortly before Fajr, when those fasting stop eating and drinking.",
      ar = "ليس صلاة. وقت احتياطي قبيل الفجر يمسك فيه الصائم عن الطعام والشراب.",
      bn = "এটি নামাজ নয়। ফজরের কিছু আগে সতর্কতামূলক সময়, যখন রোজাদার পানাহার বন্ধ করেন।",
      es = "No es una oración. Un margen de precaución poco antes del Fayr, cuando quien ayuna deja de comer y beber.",
      pt = "Não é uma oração. Uma margem de precaução pouco antes do Fajr, quando quem jejua deixa de comer e beber."
    ),
    PrayerName.FAJR to Copy(
      en = "2 obligatory rak'ahs. From true dawn until sunrise.",
      ar = "ركعتان فرضًا. من طلوع الفجر الصادق إلى طلوع الشمس.",
      bn = "২ রাকাত ফরজ। সুবহে সাদিক থেকে সূর্যোদয় পর্যন্ত।",
      es = "2 rakat obligatorias. Desde el alba verdadera hasta la salida del sol.",
      pt = "2 rakat obrigatórias. Da verdadeira alvorada até ao nascer do sol."
    ),
    PrayerName.SUNRISE to Copy(
      en = "Not a prayer. Fajr's time ends here, and praying is disliked until the sun has risen a little.",
      ar = "ليس صلاة. ينتهي به وقت الفجر، وتُكره الصلاة حتى ترتفع الشمس قليلًا.",
      bn = "এটি নামাজ নয়। এখানে ফজরের সময় শেষ হয়, আর সূর্য কিছুটা ওপরে না ওঠা পর্যন্ত নামাজ মাকরুহ।",
      es = "No es una oración. Aquí termina el tiempo del Fayr, y rezar es desaconsejable hasta que el sol se eleve un poco.",
      pt = "Não é uma oração. Aqui termina o tempo do Fajr, e rezar é desaconselhado até o sol subir um pouco."
    ),
    PrayerName.DHUHR to Copy(
      en = "4 obligatory rak'ahs. From just after the sun passes its zenith until Asr begins; on Friday, Jumu'ah takes its place.",
      ar = "أربع ركعات فرضًا. من زوال الشمس إلى دخول وقت العصر، وتقوم الجمعة مقامها يوم الجمعة.",
      bn = "৪ রাকাত ফরজ। সূর্য মধ্যাকাশ থেকে হেলে পড়ার পর থেকে আসরের সময় পর্যন্ত; শুক্রবারে জুমা এর স্থান নেয়।",
      es = "4 rakat obligatorias. Desde que el sol pasa el cenit hasta el comienzo del Asr; el viernes la sustituye la Yumua.",
      pt = "4 rakat obrigatórias. Desde que o sol passa o zénite até ao início do Asr; à sexta-feira é substituída pela Jumuá."
    ),
    PrayerName.ASR to Copy(
      en = "4 obligatory rak'ahs. From when an object's shadow equals its length (twice its length in the Hanafi school) until sunset; best prayed before the sun turns yellow.",
      ar = "أربع ركعات فرضًا. من صيرورة ظل الشيء مثله (مثليه عند الحنفية) إلى غروب الشمس، والأفضل قبل اصفرارها.",
      bn = "৪ রাকাত ফরজ। বস্তুর ছায়া তার সমান (হানাফি মতে দ্বিগুণ) হওয়া থেকে সূর্যাস্ত পর্যন্ত; সূর্য হলুদ হওয়ার আগে আদায় করা উত্তম।",
      es = "4 rakat obligatorias. Desde que la sombra de un objeto iguala su longitud (el doble en la escuela hanafí) hasta la puesta del sol; mejor antes de que el sol amarillee.",
      pt = "4 rakat obrigatórias. Desde que a sombra de um objeto iguala o seu comprimento (o dobro na escola hanafita) até ao pôr do sol; melhor antes de o sol amarelecer."
    ),
    PrayerName.MAGHRIB to Copy(
      en = "3 obligatory rak'ahs. From sunset until the red twilight fades - the shortest window of the day.",
      ar = "ثلاث ركعات فرضًا. من غروب الشمس إلى مغيب الشفق الأحمر، وهو أقصر الأوقات.",
      bn = "৩ রাকাত ফরজ। সূর্যাস্ত থেকে লাল আভা মিলিয়ে যাওয়া পর্যন্ত - দিনের সবচেয়ে সংক্ষিপ্ত সময়।",
      es = "3 rakat obligatorias. Desde la puesta del sol hasta que desaparece el crepúsculo rojo, el periodo más corto del día.",
      pt = "3 rakat obrigatórias. Do pôr do sol até desaparecer o crepúsculo vermelho, o período mais curto do dia."
    ),
    PrayerName.ISHA to Copy(
      en = "4 obligatory rak'ahs. From the end of twilight; preferred before midnight, and valid until dawn according to most scholars.",
      ar = "أربع ركعات فرضًا. من مغيب الشفق، والأفضل قبل منتصف الليل، ويمتد وقت الجواز إلى الفجر عند أكثر العلماء.",
      bn = "৪ রাকাত ফরজ। সন্ধ্যার আভা মিলিয়ে যাওয়ার পর থেকে; মধ্যরাতের আগে আদায় উত্তম, আর অধিকাংশ আলেমের মতে বৈধ সময় ফজর পর্যন্ত।",
      es = "4 rakat obligatorias. Desde el fin del crepúsculo; es preferible antes de la medianoche, y para la mayoría de los sabios es válida hasta el alba.",
      pt = "4 rakat obrigatórias. Desde o fim do crepúsculo; é preferível antes da meia-noite, e para a maioria dos sábios é válida até à alvorada."
    )
  )
}
