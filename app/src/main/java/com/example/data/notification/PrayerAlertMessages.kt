package com.example.data.notification

import com.example.data.localization.AppLanguage
import com.example.data.localization.LocalizationManager
import com.example.data.model.PrayerName

/**
 * One translated string, as a record rather than a `when` block.
 *
 * There are five prayers times two alert kinds times five languages of body text here. Written as
 * nested `when` expressions that is a hundred branches nobody can proofread; as records it is ten
 * literals per field, each reading left to right in one place.
 */
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
 * What each prayer alert says.
 *
 * The follow-up deliberately reads as a nudge rather than a second announcement - that is what
 * makes a repeat notification tolerable instead of spam. The Arabic is the source text for the
 * adhan and hadith lines, with the other languages as translations of it.
 */
object PrayerAlertMessages {

  fun title(
    prayer: PrayerName,
    kind: PrayerAlertKind,
    language: AppLanguage,
    timeFormatted: String
  ): String {
    val name = LocalizationManager.getPrayerDisplayName(prayer, language)
    return when (kind) {
      PrayerAlertKind.AT_TIME -> "$name · $timeFormatted"
      PrayerAlertKind.FOLLOW_UP -> FOLLOW_UP_TITLE.get(language).replace("%s", name)
    }
  }

  fun body(prayer: PrayerName, kind: PrayerAlertKind, language: AppLanguage): String {
    val copy = when (kind) {
      PrayerAlertKind.AT_TIME -> AT_TIME[prayer]
      PrayerAlertKind.FOLLOW_UP -> FOLLOW_UP[prayer]
    }
    return copy?.get(language) ?: GENERIC_BODY.get(language)
  }

  private val FOLLOW_UP_TITLE = Copy(
    en = "Have you prayed %s?",
    ar = "هل صليت %s؟",
    bn = "%s আদায় করেছেন কি?",
    es = "¿Has rezado %s?",
    pt = "Já rezou %s?"
  )

  private val GENERIC_BODY = Copy(
    en = "It is time for prayer.",
    ar = "حان وقت الصلاة.",
    bn = "নামাজের সময় হয়েছে।",
    es = "Es hora de la oración.",
    pt = "É hora da oração."
  )

  private val AT_TIME: Map<PrayerName, Copy> = mapOf(
    // The tathwib, authentic specifically to the Fajr adhan.
    PrayerName.FAJR to Copy(
      en = "Prayer is better than sleep.",
      ar = "الصلاة خير من النوم.",
      bn = "নামাজ ঘুমের চেয়ে উত্তম।",
      es = "La oración es mejor que el sueño.",
      pt = "A oração é melhor do que o sono."
    ),
    PrayerName.DHUHR to Copy(
      en = "The sun has passed its zenith. Pause for Dhuhr.",
      ar = "زالت الشمس عن كبد السماء، فحان وقت الظهر.",
      bn = "সূর্য মধ্যাকাশ অতিক্রম করেছে। জোহরের জন্য বিরতি নিন।",
      es = "El sol ha pasado su cenit. Haz una pausa para el Dhuhr.",
      pt = "O sol passou o seu zénite. Faça uma pausa para o Dhuhr."
    ),
    PrayerName.ASR to Copy(
      en = "Guard strictly the prayers, especially the middle prayer. (Qur'an 2:238)",
      ar = "حَافِظُوا عَلَى الصَّلَوَاتِ وَالصَّلَاةِ الْوُسْطَىٰ. (البقرة ٢٣٨)",
      bn = "সমস্ত নামাজ ও মধ্যবর্তী নামাজের প্রতি যত্নবান হও। (কুরআন ২:২৩৮)",
      es = "Observad las oraciones, especialmente la oración intermedia. (Corán 2:238)",
      pt = "Observai as orações, especialmente a oração intermédia. (Alcorão 2:238)"
    ),
    PrayerName.MAGHRIB to Copy(
      en = "Hasten - Maghrib has the shortest window of the day.",
      ar = "عجّل، فوقت المغرب أقصر أوقات اليوم.",
      bn = "দ্রুত করুন - মাগরিবের সময় দিনের মধ্যে সবচেয়ে সংক্ষিপ্ত।",
      es = "Apresúrate: el Magrib tiene la ventana más corta del día.",
      pt = "Apresse-se: o Magrib tem a janela mais curta do dia."
    ),
    PrayerName.ISHA to Copy(
      en = "Close the day with Isha.",
      ar = "اختم يومك بصلاة العشاء.",
      bn = "এশার নামাজ দিয়ে দিনটি শেষ করুন।",
      es = "Cierra el día con el Isha.",
      pt = "Encerre o dia com o Isha."
    )
  )

  private val FOLLOW_UP: Map<PrayerName, Copy> = mapOf(
    PrayerName.FAJR to Copy(
      en = "Whoever prays Fajr is under Allah's protection. (Sahih Muslim 657)",
      ar = "من صلى الصبح فهو في ذمة الله. (صحيح مسلم ٦٥٧)",
      bn = "যে ফজর আদায় করে সে আল্লাহর জিম্মায় থাকে। (সহিহ মুসলিম ৬৫৭)",
      es = "Quien reza el Fayr está bajo la protección de Alá. (Sahih Muslim 657)",
      pt = "Quem reza o Fajr está sob a proteção de Allah. (Sahih Muslim 657)"
    ),
    PrayerName.DHUHR to Copy(
      en = "Four rak'ah before Dhuhr - the gates of heaven are opened. (Tirmidhi 478)",
      ar = "أربع ركعات قبل الظهر تُفتح لها أبواب السماء. (الترمذي ٤٧٨)",
      bn = "জোহরের পূর্বে চার রাকাত - আসমানের দরজা খুলে দেওয়া হয়। (তিরমিজি ৪৭৮)",
      es = "Cuatro rakat antes del Dhuhr: se abren las puertas del cielo. (Tirmidhi 478)",
      pt = "Quatro rakat antes do Dhuhr: abrem-se as portas do céu. (Tirmidhi 478)"
    ),
    PrayerName.ASR to Copy(
      en = "Whoever misses Asr, it is as though he lost his family and property. (Bukhari 552)",
      ar = "من فاتته صلاة العصر فكأنما وُتر أهله وماله. (البخاري ٥٥٢)",
      bn = "যার আসর ছুটে গেল, যেন তার পরিবার ও সম্পদ ধ্বংস হলো। (বুখারি ৫৫২)",
      es = "Quien pierde el Asr es como si hubiera perdido su familia y sus bienes. (Bujari 552)",
      pt = "Quem perde o Asr é como se tivesse perdido a família e os bens. (Bukhari 552)"
    ),
    PrayerName.MAGHRIB to Copy(
      en = "Maghrib closes when the twilight disappears - do not delay it.",
      ar = "ينتهي وقت المغرب بمغيب الشفق، فلا تؤخرها.",
      bn = "গোধূলি অদৃশ্য হলে মাগরিবের সময় শেষ - দেরি করবেন না।",
      es = "El Magrib termina al desaparecer el crepúsculo: no lo retrases.",
      pt = "O Magrib termina quando o crepúsculo desaparece: não o adie."
    ),
    PrayerName.ISHA to Copy(
      en = "Whoever prays Isha in congregation is as though he prayed half the night. (Muslim 656)",
      ar = "من صلى العشاء في جماعة فكأنما قام نصف الليل. (مسلم ٦٥٦)",
      bn = "যে জামাতে এশা আদায় করে, সে যেন অর্ধরাত্রি দাঁড়িয়ে থাকল। (মুসলিম ৬৫৬)",
      es = "Quien reza el Isha en congregación es como si hubiera rezado media noche. (Muslim 656)",
      pt = "Quem reza o Isha em congregação é como se tivesse rezado metade da noite. (Muslim 656)"
    )
  )
}
