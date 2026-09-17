package com.example.data.dua

import java.time.LocalDate

/**
 * The pools the "Ayah of the day" and "Hadith of the day" cards draw from.
 *
 * There used to be exactly one of each, held in a single `DAILY_INSPIRATION` constant, so the card
 * said the same thing on every day of every year. The title promised a rotation the data could
 * not deliver.
 *
 * Selection is by epoch day rather than by a random draw, which buys three things: everyone sees
 * the same verse on the same day, it survives a process restart without repeating or skipping,
 * and it needs nothing stored. The two pools are **coprime in length** - twelve verses against
 * eleven narrations - so the pairing itself takes 132 days to come back around rather than
 * repeating every twelve.
 *
 * Arabic is the source text in both pools; the other languages are translations of it. Verse
 * numbering follows the standard Kufan count used by the Cairo edition.
 */
object DailyInspirationData {

  /** One verse, in Arabic with translations. */
  data class Ayah(
    val arabic: String,
    val surah: String,
    val en: String,
    val bn: String,
    val es: String,
    val pt: String
  )

  /** One narration, with who reported it and where it is recorded. */
  data class Hadith(
    val arabic: String,
    val narrator: String,
    val source: String,
    val en: String,
    val bn: String,
    val es: String,
    val pt: String
  )

  /**
   * Today's pairing, for the city's local date.
   *
   * `Math.floorMod` rather than `%`: epoch days before 1970 are negative, and Kotlin's remainder
   * keeps the sign, which would index out of bounds. It costs nothing to be right about it.
   */
  fun forDate(date: LocalDate): DailyInspiration {
    val day = date.toEpochDay()
    val ayah = AYAHS[Math.floorMod(day, AYAHS.size.toLong()).toInt()]
    val hadith = HADITHS[Math.floorMod(day, HADITHS.size.toLong()).toInt()]
    return DailyInspiration(
      ayahArabic = ayah.arabic,
      ayahTranslation = ayah.en,
      ayahTranslationBn = ayah.bn,
      ayahTranslationEs = ayah.es,
      ayahTranslationPt = ayah.pt,
      ayahSurah = ayah.surah,
      hadithArabic = hadith.arabic,
      hadithTranslation = hadith.en,
      hadithTranslationBn = hadith.bn,
      hadithTranslationEs = hadith.es,
      hadithTranslationPt = hadith.pt,
      hadithNarrator = hadith.narrator,
      hadithSource = hadith.source
    )
  }

  /**
   * Twelve verses, weighted towards prayer, the night and remembrance - what this app is for.
   * Add to the list freely; only the count matters, and only for how long the cycle runs.
   */
  val AYAHS = listOf(
    Ayah(
      arabic = "إِنَّ الصَّلَاةَ كَانَتْ عَلَى الْمُؤْمِنِينَ كِتَابًا مَّوْقُوتًا",
      surah = "Surah An-Nisa (4:103)",
      en = "Indeed, prayer has been decreed upon the believers a decree of specified times.",
      bn = "নিশ্চয়ই নামাজ মুমিনদের ওপর নির্দিষ্ট সময়ে ফরজ করা হয়েছে।",
      es = "Ciertamente, la oración ha sido prescrita a los creyentes en horarios determinados.",
      pt = "Por certo, a oração está prescrita aos crentes em horários determinados."
    ),
    Ayah(
      arabic = "وَمِنَ اللَّيْلِ فَتَهَجَّدْ بِهِ نَافِلَةً لَّكَ",
      surah = "Surah Al-Isra (17:79)",
      en = "And rise at night to pray, as an additional worship for you.",
      bn = "আর রাতের একাংশে তাহাজ্জুদ পড়ুন, এটি আপনার জন্য অতিরিক্ত ইবাদত।",
      es = "Y levántate parte de la noche para orar, como una adoración adicional para ti.",
      pt = "E levanta-te parte da noite para orar, como adoração adicional para ti."
    ),
    Ayah(
      arabic = "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
      surah = "Surah Ar-Ra'd (13:28)",
      en = "Unquestionably, by the remembrance of Allah hearts are assured.",
      bn = "জেনে রাখো, আল্লাহর স্মরণেই অন্তর প্রশান্তি পায়।",
      es = "Sin duda, con el recuerdo de Allah los corazones se sosiegan.",
      pt = "Sem dúvida, é na lembrança de Allah que os corações se tranquilizam."
    ),
    Ayah(
      arabic = "فَاذْكُرُونِي أَذْكُرْكُمْ وَاشْكُرُوا لِي وَلَا تَكْفُرُونِ",
      surah = "Surah Al-Baqarah (2:152)",
      en = "So remember Me; I will remember you. And be grateful to Me and do not deny Me.",
      bn = "সুতরাং তোমরা আমাকে স্মরণ করো, আমিও তোমাদের স্মরণ করব। আমার কৃতজ্ঞতা প্রকাশ করো, অকৃতজ্ঞ হয়ো না।",
      es = "Recordadme, y Yo os recordaré. Y sedme agradecidos y no Me neguéis.",
      pt = "Lembrai-vos de Mim, e Eu Me lembrarei de vós. E sede-Me gratos e não Me negueis."
    ),
    Ayah(
      arabic = "وَأَقِمِ الصَّلَاةَ لِذِكْرِي",
      surah = "Surah Taha (20:14)",
      en = "And establish prayer for My remembrance.",
      bn = "আর আমার স্মরণের উদ্দেশ্যে নামাজ কায়েম করো।",
      es = "Y establece la oración para recordarme.",
      pt = "E observa a oração para lembrar-te de Mim."
    ),
    Ayah(
      arabic = "لَا يُكَلِّفُ اللَّهُ نَفْسًا إِلَّا وُسْعَهَا",
      surah = "Surah Al-Baqarah (2:286)",
      en = "Allah does not burden a soul beyond what it can bear.",
      bn = "আল্লাহ কারও ওপর তার সামর্থ্যের বেশি বোঝা চাপান না।",
      es = "Allah no impone a nadie una carga mayor de la que puede soportar.",
      pt = "Allah não impõe a alma alguma além das suas forças."
    ),
    Ayah(
      arabic = "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا",
      surah = "Surah Ash-Sharh (94:5)",
      en = "For indeed, with hardship comes ease.",
      bn = "নিশ্চয়ই কষ্টের সাথেই রয়েছে স্বস্তি।",
      es = "Ciertamente, junto a la dificultad está la facilidad.",
      pt = "Por certo, junto à dificuldade há facilidade."
    ),
    Ayah(
      arabic = "يَا أَيُّهَا الَّذِينَ آمَنُوا اسْتَعِينُوا بِالصَّبْرِ وَالصَّلَاةِ ۚ إِنَّ اللَّهَ مَعَ الصَّابِرِينَ",
      surah = "Surah Al-Baqarah (2:153)",
      en = "O you who believe, seek help through patience and prayer. Indeed, Allah is with the patient.",
      bn = "হে মুমিনগণ, ধৈর্য ও নামাজের মাধ্যমে সাহায্য প্রার্থনা করো। নিশ্চয়ই আল্লাহ ধৈর্যশীলদের সঙ্গে আছেন।",
      es = "¡Oh, creyentes! Buscad ayuda en la paciencia y la oración. Ciertamente, Allah está con los pacientes.",
      pt = "Ó vós que credes! Buscai ajuda na paciência e na oração. Por certo, Allah está com os pacientes."
    ),
    Ayah(
      arabic = "إِنَّ الصَّلَاةَ تَنْهَى عَنِ الْفَحْشَاءِ وَالْمُنكَرِ",
      surah = "Surah Al-Ankabut (29:45)",
      en = "Indeed, prayer restrains from immorality and wrongdoing.",
      bn = "নিশ্চয়ই নামাজ অশ্লীলতা ও মন্দ কাজ থেকে বিরত রাখে।",
      es = "Ciertamente, la oración preserva de la indecencia y de lo reprobable.",
      pt = "Por certo, a oração preserva da indecência e do reprovável."
    ),
    Ayah(
      arabic = "كَانُوا قَلِيلًا مِّنَ اللَّيْلِ مَا يَهْجَعُونَ",
      surah = "Surah Adh-Dhariyat (51:17)",
      en = "They used to sleep but little of the night.",
      bn = "তারা রাতের খুব অল্প অংশই ঘুমিয়ে কাটাত।",
      es = "Dormían poco por la noche.",
      pt = "Pouco dormiam durante a noite."
    ),
    Ayah(
      arabic = "لَا تَقْنَطُوا مِن رَّحْمَةِ اللَّهِ",
      surah = "Surah Az-Zumar (39:53)",
      en = "Do not despair of the mercy of Allah.",
      bn = "আল্লাহর রহমত থেকে নিরাশ হয়ো না।",
      es = "No desesperéis de la misericordia de Allah.",
      pt = "Não desespereis da misericórdia de Allah."
    ),
    Ayah(
      arabic = "قَدْ أَفْلَحَ الْمُؤْمِنُونَ ۝ الَّذِينَ هُمْ فِي صَلَاتِهِمْ خَاشِعُونَ",
      surah = "Surah Al-Mu'minun (23:1-2)",
      en = "Successful indeed are the believers — those who are humbly attentive in their prayers.",
      bn = "নিশ্চয়ই সফল হয়েছে মুমিনগণ — যারা তাদের নামাজে বিনয়ী ও মনোযোগী।",
      es = "Ciertamente, triunfan los creyentes: aquellos que en sus oraciones son humildes y atentos.",
      pt = "Por certo, bem-aventurados são os crentes — aqueles que, em suas orações, são humildes."
    )
  )

  /** Eleven narrations. Coprime with the verse pool by design; see the class comment. */
  val HADITHS = listOf(
    Hadith(
      arabic = "مَنْ سَلَكَ طَرِيقًا يَلْتَمِسُ فِيهِ عِلْمًا سَهَّلَ اللَّهُ لَهُ بِهِ طَرِيقًا إِلَى الْجَنَّةِ",
      narrator = "Abu Hurairah (RA)",
      source = "Sahih Muslim 2699",
      en = "Whoever takes a path upon which he seeks knowledge, Allah makes the path to Paradise easy for him.",
      bn = "যে ব্যক্তি জ্ঞান অর্জনের উদ্দেশ্যে কোনো পথ অবলম্বন করে, আল্লাহ তার জন্য জান্নাতের পথ সহজ করে দেন।",
      es = "Quien emprenda un camino en busca de conocimiento, Allah le facilitará el camino hacia el Paraíso.",
      pt = "Aquele que trilha um caminho em busca de conhecimento, Allah facilitará para ele o caminho para o Paraíso."
    ),
    Hadith(
      arabic = "أَحَبُّ الْأَعْمَالِ إِلَى اللَّهِ أَدْوَمُهَا وَإِنْ قَلَّ",
      narrator = "Aishah (RA)",
      source = "Sahih al-Bukhari 6464",
      en = "The deeds most beloved to Allah are those done consistently, even if they are small.",
      bn = "আল্লাহর কাছে সবচেয়ে প্রিয় আমল হলো যা নিয়মিত করা হয়, তা পরিমাণে অল্প হলেও।",
      es = "Las obras más amadas por Allah son las que se hacen con constancia, aunque sean pocas.",
      pt = "As obras mais amadas por Allah são as feitas com constância, mesmo que sejam poucas."
    ),
    Hadith(
      arabic = "يَنْزِلُ رَبُّنَا تَبَارَكَ وَتَعَالَى كُلَّ لَيْلَةٍ إِلَى السَّمَاءِ الدُّنْيَا حِينَ يَبْقَى ثُلُثُ اللَّيْلِ الْآخِرُ",
      narrator = "Abu Hurairah (RA)",
      source = "Sahih al-Bukhari 1145",
      en = "Our Lord descends to the lowest heaven each night, when the last third of the night remains.",
      bn = "আমাদের রব প্রতি রাতে, যখন রাতের শেষ তৃতীয়াংশ বাকি থাকে, নিকটতম আসমানে অবতরণ করেন।",
      es = "Nuestro Señor desciende al cielo más bajo cada noche, cuando queda el último tercio de la noche.",
      pt = "O nosso Senhor desce ao céu mais baixo todas as noites, quando resta o último terço da noite."
    ),
    Hadith(
      arabic = "أَفْضَلُ الصَّلَاةِ بَعْدَ الْفَرِيضَةِ صَلَاةُ اللَّيْلِ",
      narrator = "Abu Hurairah (RA)",
      source = "Sahih Muslim 1163",
      en = "The most excellent prayer after the obligatory prayers is the prayer of the night.",
      bn = "ফরজ নামাজের পর সর্বোত্তম নামাজ হলো রাতের নামাজ।",
      es = "La mejor oración después de las obligatorias es la oración de la noche.",
      pt = "A melhor oração depois das obrigatórias é a oração da noite."
    ),
    Hadith(
      arabic = "الطُّهُورُ شَطْرُ الْإِيمَانِ",
      narrator = "Abu Malik al-Ash'ari (RA)",
      source = "Sahih Muslim 223",
      en = "Purity is half of faith.",
      bn = "পবিত্রতা ঈমানের অর্ধেক।",
      es = "La purificación es la mitad de la fe.",
      pt = "A pureza é metade da fé."
    ),
    Hadith(
      arabic = "لَا يُؤْمِنُ أَحَدُكُمْ حَتَّى يُحِبَّ لِأَخِيهِ مَا يُحِبُّ لِنَفْسِهِ",
      narrator = "Anas ibn Malik (RA)",
      source = "Sahih al-Bukhari 13",
      en = "None of you truly believes until he loves for his brother what he loves for himself.",
      bn = "তোমাদের কেউ প্রকৃত মুমিন হতে পারবে না, যতক্ষণ না সে তার ভাইয়ের জন্য তা-ই পছন্দ করে যা নিজের জন্য পছন্দ করে।",
      es = "Ninguno de vosotros cree de verdad hasta que ame para su hermano lo que ama para sí mismo.",
      pt = "Nenhum de vós crê verdadeiramente até amar para o seu irmão o que ama para si mesmo."
    ),
    Hadith(
      arabic = "وَمَا يَزَالُ عَبْدِي يَتَقَرَّبُ إِلَيَّ بِالنَّوَافِلِ حَتَّى أُحِبَّهُ",
      narrator = "Abu Hurairah (RA)",
      source = "Sahih al-Bukhari 6502",
      en = "My servant continues to draw near to Me with voluntary worship until I love him.",
      bn = "আমার বান্দা নফল ইবাদতের মাধ্যমে আমার নিকটবর্তী হতে থাকে, যতক্ষণ না আমি তাকে ভালোবাসি।",
      es = "Mi siervo sigue acercándose a Mí con la adoración voluntaria hasta que Yo lo amo.",
      pt = "O Meu servo continua a aproximar-se de Mim com a adoração voluntária até que Eu o ame."
    ),
    Hadith(
      arabic = "الدُّعَاءُ هُوَ الْعِبَادَةُ",
      narrator = "An-Nu'man ibn Bashir (RA)",
      source = "Sunan Abi Dawud 1479",
      en = "Supplication is itself worship.",
      bn = "দোয়া করাই ইবাদত।",
      es = "La súplica es, en sí misma, adoración.",
      pt = "A súplica é, em si mesma, adoração."
    ),
    Hadith(
      arabic = "الْمُؤْمِنُ الْقَوِيُّ خَيْرٌ وَأَحَبُّ إِلَى اللَّهِ مِنَ الْمُؤْمِنِ الضَّعِيفِ",
      narrator = "Abu Hurairah (RA)",
      source = "Sahih Muslim 2664",
      en = "The strong believer is better and more beloved to Allah than the weak believer.",
      bn = "শক্তিশালী মুমিন দুর্বল মুমিনের চেয়ে উত্তম এবং আল্লাহর কাছে অধিক প্রিয়।",
      es = "El creyente fuerte es mejor y más amado por Allah que el creyente débil.",
      pt = "O crente forte é melhor e mais amado por Allah do que o crente fraco."
    ),
    Hadith(
      arabic = "مَنْ كَانَ يُؤْمِنُ بِاللَّهِ وَالْيَوْمِ الْآخِرِ فَلْيَقُلْ خَيْرًا أَوْ لِيَصْمُتْ",
      narrator = "Abu Hurairah (RA)",
      source = "Sahih al-Bukhari 6018",
      en = "Whoever believes in Allah and the Last Day, let him speak what is good or stay silent.",
      bn = "যে ব্যক্তি আল্লাহ ও পরকালে বিশ্বাস করে, সে ভালো কথা বলুক অথবা নীরব থাকুক।",
      es = "Quien crea en Allah y en el Último Día, que diga algo bueno o que guarde silencio.",
      pt = "Quem crê em Allah e no Último Dia, que diga algo bom ou que se cale."
    ),
    Hadith(
      arabic = "مَثَلُ الَّذِي يَذْكُرُ رَبَّهُ وَالَّذِي لَا يَذْكُرُهُ مَثَلُ الْحَيِّ وَالْمَيِّتِ",
      narrator = "Abu Musa al-Ash'ari (RA)",
      source = "Sahih al-Bukhari 6407",
      en = "The one who remembers his Lord and the one who does not are like the living and the dead.",
      bn = "যে ব্যক্তি তার রবকে স্মরণ করে এবং যে করে না, তাদের উদাহরণ জীবিত ও মৃতের মতো।",
      es = "Quien recuerda a su Señor y quien no lo hace son como el vivo y el muerto.",
      pt = "Quem se lembra do seu Senhor e quem não se lembra são como o vivo e o morto."
    )
  )
}
