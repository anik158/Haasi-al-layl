package com.example.data.dua

import com.example.data.localization.AppLanguage

enum class DuaCategory(val displayName: String) {
  ALL("All"),
  WEATHER("Weather & Nature"),
  MORNING("Morning Adhkar"),
  EVENING("Evening Adhkar"),
  PRAYER("Prayer & Mosque"),
  DAILY("Daily Life");

  fun getLocalizedName(language: AppLanguage): String = when (language) {
    AppLanguage.BENGALI -> when (this) {
      ALL -> "সব"
      WEATHER -> "আবহাওয়া ও প্রকৃতি"
      MORNING -> "সকালের জিকির"
      EVENING -> "সন্ধ্যার জিকির"
      PRAYER -> "নামাজ ও মসজিদ"
      DAILY -> "দৈনন্দিন জীবন"
    }
    AppLanguage.ARABIC -> when (this) {
      ALL -> "الكل"
      WEATHER -> "الطقس والطبيعة"
      MORNING -> "أذكار الصباح"
      EVENING -> "أذكار المساء"
      PRAYER -> "الصلاة والمسجد"
      DAILY -> "الحياة اليومية"
    }
    AppLanguage.SPANISH -> when (this) {
      ALL -> "Todas"
      WEATHER -> "Clima y Naturaleza"
      MORNING -> "Adhkar Mañana"
      EVENING -> "Adhkar Tarde"
      PRAYER -> "Oración y Mezquita"
      DAILY -> "Vida Diaria"
    }
    AppLanguage.PORTUGUESE -> when (this) {
      ALL -> "Todas"
      WEATHER -> "Clima e Natureza"
      MORNING -> "Adhkar Manhã"
      EVENING -> "Adhkar Tarde"
      PRAYER -> "Oração e Mesquita"
      DAILY -> "Vida Diária"
    }
    AppLanguage.ENGLISH -> displayName
  }
}

data class DuaItem(
  val id: String,
  val title: String,
  val titleBn: String = "",
  val titleAr: String = "",
  val titleEs: String = "",
  val titlePt: String = "",
  val category: DuaCategory,
  val arabic: String,
  val transliteration: String,
  val translation: String,
  val translationBn: String = "",
  val translationEs: String = "",
  val translationPt: String = "",
  val reference: String,
  val repetition: String = "1x"
) {
  fun getLocalizedTitle(language: AppLanguage): String = when (language) {
    AppLanguage.BENGALI -> if (titleBn.isNotBlank()) titleBn else title
    AppLanguage.ARABIC -> if (titleAr.isNotBlank()) titleAr else title
    AppLanguage.SPANISH -> if (titleEs.isNotBlank()) titleEs else title
    AppLanguage.PORTUGUESE -> if (titlePt.isNotBlank()) titlePt else title
    AppLanguage.ENGLISH -> title
  }

  fun getLocalizedTranslation(language: AppLanguage): String = when (language) {
    AppLanguage.BENGALI -> if (translationBn.isNotBlank()) translationBn else translation
    AppLanguage.ARABIC -> "" // In Arabic view, the Arabic text itself is read
    AppLanguage.SPANISH -> if (translationEs.isNotBlank()) translationEs else translation
    AppLanguage.PORTUGUESE -> if (translationPt.isNotBlank()) translationPt else translation
    AppLanguage.ENGLISH -> translation
  }
}

data class DailyInspiration(
  val ayahArabic: String,
  val ayahTranslation: String,
  val ayahTranslationBn: String = "",
  val ayahTranslationEs: String = "",
  val ayahTranslationPt: String = "",
  val ayahSurah: String,
  val hadithArabic: String,
  val hadithTranslation: String,
  val hadithTranslationBn: String = "",
  val hadithTranslationEs: String = "",
  val hadithTranslationPt: String = "",
  val hadithNarrator: String,
  val hadithSource: String
) {
  fun getLocalizedAyah(language: AppLanguage): String = when (language) {
    AppLanguage.BENGALI -> if (ayahTranslationBn.isNotBlank()) ayahTranslationBn else ayahTranslation
    AppLanguage.SPANISH -> if (ayahTranslationEs.isNotBlank()) ayahTranslationEs else ayahTranslation
    AppLanguage.PORTUGUESE -> if (ayahTranslationPt.isNotBlank()) ayahTranslationPt else ayahTranslation
    else -> ayahTranslation
  }

  fun getLocalizedHadith(language: AppLanguage): String = when (language) {
    AppLanguage.BENGALI -> if (hadithTranslationBn.isNotBlank()) hadithTranslationBn else hadithTranslation
    AppLanguage.SPANISH -> if (hadithTranslationEs.isNotBlank()) hadithTranslationEs else hadithTranslation
    AppLanguage.PORTUGUESE -> if (hadithTranslationPt.isNotBlank()) hadithTranslationPt else hadithTranslation
    else -> hadithTranslation
  }
}

object IslamicContentProvider {

  // The single hard-coded inspiration that used to live here is now a rotating pool in
  // DailyInspirationData, selected by epoch day. One constant could never be "of the day".

  val DUAS = listOf(
    // Weather & Nature Duas
    DuaItem(
      id = "dua_rain",
      title = "When Rain Descends",
      titleBn = "বৃষ্টি বর্ষণের সময় দোয়া",
      titleAr = "دعاء نزول المطر",
      titleEs = "Al Caer la Lluvia",
      titlePt = "Quando Cai a Chuva",
      category = DuaCategory.WEATHER,
      arabic = "اللَّهُمَّ صَيِّبًا نَافِعًا",
      transliteration = "Allahumma sayyiban nafi'an",
      translation = "O Allah, make it a beneficial downpour.",
      translationBn = "হে আল্লাহ, একে উপকারী ও কল্যাণময় বৃষ্টি বানিয়ে দিন।",
      translationEs = "¡Oh Allah! Haz que sea una lluvia beneficiosa y fructífera.",
      translationPt = "Ó Allah! Fazei com que seja uma chuva benéfica e produtiva.",
      reference = "Sahih al-Bukhari 1032",
      repetition = "1x"
    ),
    DuaItem(
      id = "dua_thunder",
      title = "When Hearing Thunder",
      titleBn = "মেঘের গর্জন শোনার পর দোয়া",
      titleAr = "دعاء سماع الرعد",
      titleEs = "Al Escuchar el Trueno",
      titlePt = "Ao Ouvir o Trovão",
      category = DuaCategory.WEATHER,
      arabic = "سُبْحَانَ الَّذِي يُسَبِّحُ الرَّعْدُ بِحَمْدِهِ وَالْمَلَائِكَةُ مِنْ خِيفَتِهِ",
      transliteration = "Subhana alladhi yusabbihu ar-ra'du bihamdihi wal-mala'ikatu min kheefatih",
      translation = "Glory is to Him Whom thunder glorifies with His praise, and the angels out of fear of Him.",
      translationBn = "পবিত্র সেই সত্ত্বা, যাঁর প্রশংসায় মেঘের গর্জন এবং ফেরেশতাগণ তাঁর ভয়ে তাসবিহ পাঠ করে।",
      translationEs = "Glorificado sea Aquel a Quien el trueno glorifica con Su alabanza, y los ángeles por Su temor.",
      translationPt = "Glorificado seja Aquele a Quem o trovão glorifica com Seu louvor, e os anjos pelo Seu temor.",
      reference = "Muwatta Malik 1808",
      repetition = "1x"
    ),
    DuaItem(
      id = "dua_wind",
      title = "When Strong Winds Blow",
      titleBn = "প্রচণ্ড ঝড়-বাতাস চলার সময় দোয়া",
      titleAr = "دعاء هبوب الرياح",
      titleEs = "Cuando Soplan Vientos Fuertes",
      titlePt = "Quando Sopram Ventos Fortes",
      category = DuaCategory.WEATHER,
      arabic = "اللَّهُمَّ إِنِّي أَسْأَلُكَ خَيْرَهَا وَخَيْرَ مَا فِيهَا وَخَيْرَ مَا أُرْسِلَتْ بِهِ، وَأَعُوذُ بِكَ مِنْ شَرِّهَا وَشَرِّ مَا فِيهَا وَشَرِّ مَا أُرْسِلَتْ بِهِ",
      transliteration = "Allahumma inni as'aluka khayraha wa khayra ma feeha wa khayra ma ursilat bihi, wa a'udhu bika min sharriha wa sharri ma feeha wa sharri ma ursilat bih",
      translation = "O Allah, I ask You for its good, the good that is in it, and the good with which it was sent; and I seek refuge in You from its evil, the evil that is in it, and the evil with which it was sent.",
      translationBn = "হে আল্লাহ, আমি আপনার কাছে এর কল্যাণ, এর ভেতরের কল্যাণ এবং যে উদ্দেশ্যে এটি প্রেরিত তার কল্যাণ প্রার্থনা করছি; এবং এর অকল্যাণ ও ক্ষতি থেকে আশ্রয় চাই।",
      translationEs = "¡Oh Allah! Te pido el bien de este viento, el bien que contiene y el bien con el que fue enviado; y me refugio en Ti de su mal, del mal que contiene y del mal con el que fue enviado.",
      translationPt = "Ó Allah! Peço-Te o bem deste vento, o bem que contém e o bem com que foi enviado; e refugio-me em Ti do seu mal, do mal que contém e do mal com que foi enviado.",
      reference = "Sahih Muslim 899",
      repetition = "1x"
    ),
    DuaItem(
      id = "dua_heat",
      title = "During Severe Heat",
      titleBn = "তীব্র গরমের সময় দোয়া",
      titleAr = "دعاء شدة الحر",
      titleEs = "Durante Calor Extremo",
      titlePt = "Durante o Calor Extremo",
      category = DuaCategory.WEATHER,
      arabic = "لَا إِلَهَ إِلَّا اللَّهُ، مَا أَشَدَّ حَرَّ هَذَا الْيَوْمِ، اللَّهُمَّ أَجِرْنِي مِنْ حَرِّ جَهَنَّمَ",
      transliteration = "La ilaha illallah, ma ashadda harra hadhal-yawm, Allahumma ajirni min harri Jahannam",
      translation = "There is no deity worthy of worship except Allah; how intense is the heat of this day! O Allah, protect me from the heat of Hellfire.",
      translationBn = "আল্লাহ ছাড়া কোনো উপাস্য নেই; আজকের দিনের তাপ কতই না তীব্র! হে আল্লাহ, আমাকে জাহান্নামের উত্তাপ থেকে রক্ষা করুন।",
      translationEs = "No hay más divinidad que Allah; ¡qué intenso es el calor de este día! ¡Oh Allah! Protégeme del ardor del Fuego del Infierno.",
      translationPt = "Não há divindade exceto Allah; quão intenso é o calor deste dia! Ó Allah! Protege-me do ardor do Fogo do Inferno.",
      reference = "Ibn as-Sunni 306",
      repetition = "1x"
    ),

    // Morning Adhkar
    DuaItem(
      id = "dua_morning_waking",
      title = "Upon Waking Up",
      titleBn = "ঘুম থেকে জাগার পর দোয়া",
      titleAr = "أذكار الاستيقاظ من النوم",
      titleEs = "Al Despertar por la Mañana",
      titlePt = "Ao Despertar pela Manhã",
      category = DuaCategory.MORNING,
      arabic = "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
      transliteration = "Alhamdu lillahil-ladhi ahyana ba'da ma amatana wa ilayhin-nushoor",
      translation = "All praise is for Allah who gave us life after having given us death, and unto Him is the resurrection.",
      translationBn = "সমস্ত প্রশংসা আল্লাহর জন্য, যিনি আমাদেরকে মৃত্যু (ঘুম) দেওয়ার পর আবার জীবিত করলেন এবং তাঁর দিকেই পুনরুত্থান।",
      translationEs = "Todas las alabanzas son para Allah, Quien nos dio vida después de habernos dado la muerte, y hacia Él es la resurrección.",
      translationPt = "Todos os louvores são para Allah, Que nos deu vida depois de nos ter dado a morte, e a Ele é a ressurreição.",
      reference = "Sahih al-Bukhari 6312",
      repetition = "1x"
    ),
    DuaItem(
      id = "dua_morning_sayyid",
      title = "Sayyid al-Istighfar (Master of Forgiveness)",
      titleBn = "সাইয়্যিদুল ইস্তিগফার (শ্রেষ্ঠ ক্ষমা প্রার্থনা)",
      titleAr = "سيد الاستغفار",
      titleEs = "Sayyid al-Istighfar (Maestro del Perdón)",
      titlePt = "Sayyid al-Istighfar (Mestre do Perdão)",
      category = DuaCategory.MORNING,
      arabic = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
      transliteration = "Allahumma Anta Rabbi la ilaha illa Ant, khalaqtani wa ana 'abduk, wa ana 'ala 'ahdika wa wa'dika mastata't, a'udhu bika min sharri ma sana't, aboo'u laka bini'matika 'alayya, wa aboo'u bidhanbi faghfir li, fa'innahu la yaghfirudh-dhunooba illa Ant",
      translation = "O Allah, You are my Lord, none has the right to be worshiped but You. You created me and I am Your servant. I am committed to Your covenant and promise as much as I can. I seek refuge in You from the evil of what I have done. I acknowledge Your favors upon me and I acknowledge my sins, so forgive me, for none forgives sins except You.",
      translationBn = "হে আল্লাহ, আপনিই আমার প্রতিপালক, আপনি ছাড়া কোনো উপাস্য নেই। আপনি আমাকে সৃষ্টি করেছেন এবং আমি আপনার বান্দা। আমি আমার সাধ্যমতো আপনার অঙ্গীকারে অবিচল আছি। আমি আপনার ক্ষমার আশা করি ও আমার গুনাহ স্বীকার করছি, সুতরাং আমাকে ক্ষমা করুন, কারণ আপনি ছাড়া কেউ গুনাহ ক্ষমা করতে পারে না।",
      translationEs = "¡Oh Allah! Tú eres mi Señor, no hay deidad digna de adoración salvo Tú. Me creaste y soy Tu siervo. Cumplo con Tu pacto y promesa según mi capacidad. Me refugio en Ti del mal que he cometido. Reconozco Tus bendiciones y confieso mis pecados; perdóname, pues nadie perdona los pecados sino Tú.",
      translationPt = "Ó Allah! Tu és o meu Senhor, não há divindade digna de adoração exceto Tu. Criaste-me e sou Teu servo. Cumpro o Teu pacto e promessa conforme a minha capacidade. Refugio-me em Ti do mal que cometi. Reconheço as Tuas bênçãos e confesso os meus pecados; perdoa-me, pois ninguém perdoa os pecados exceto Tu.",
      reference = "Sahih al-Bukhari 6306",
      repetition = "Morning & Evening"
    ),
    DuaItem(
      id = "dua_morning_protection",
      title = "Morning Protection & Praise",
      titleBn = "সকালের সার্বিক নিরাপত্তা ও প্রশংসার দোয়া",
      titleAr = "أذكار الصباح والثناء",
      titleEs = "Protección y Alabanza Matutina",
      titlePt = "Proteção e Louvor Matinal",
      category = DuaCategory.MORNING,
      arabic = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
      transliteration = "Asbahna wa asbahal-mulku lillah, wal-hamdu lillah, la ilaha illallahu wahdahu la shareeka lah, lahul-mulku wa lahul-hamd, wa Huwa 'ala kulli shay'in qadeer",
      translation = "We have entered a new morning and the dominion belongs to Allah, and all praise is for Allah. There is no deity worthy of worship except Allah alone, without partner. To Him belongs sovereignty and to Him belongs all praise, and He has power over all things.",
      translationBn = "আমরা সকালে উপনীত হয়েছি এবং সমগ্র রাজত্ব আল্লাহরই রইল, এবং সমস্ত প্রশংসা আল্লাহর। আল্লাহ ছাড়া কোনো উপাস্য নেই, তিনি এক ও তাঁর কোনো শরিক নেই।",
      translationEs = "Hemos alcanzado la mañana y la soberanía pertenece a Allah, y toda alabanza es para Allah. No hay más divinidad que Allah, Único, sin copartícipes. Suya es la soberanía y Suya la alabanza, y Él tiene poder sobre todas las cosas.",
      translationPt = "Chegamos à manhã e a soberania pertence a Allah, e todo louvor é para Allah. Não há divindade exceto Allah, Único, sem parceiros. Dele é a soberania e Dele é o louvor, e Ele tem poder sobre todas as coisas.",
      reference = "Sahih Muslim 2723",
      repetition = "1x"
    ),

    // Evening Adhkar
    DuaItem(
      id = "dua_evening_start",
      title = "Entering the Evening",
      titleBn = "সন্ধ্যায় প্রবেশের দোয়া",
      titleAr = "أذكار المساء",
      titleEs = "Al Entrar en la Tarde/Noche",
      titlePt = "Ao Entrar na Tarde/Noite",
      category = DuaCategory.EVENING,
      arabic = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
      transliteration = "Amsayna wa amsal-mulku lillah, wal-hamdu lillah, la ilaha illallahu wahdahu la shareeka lah",
      translation = "We have reached the evening and the kingdom belongs to Allah, and all praise is for Allah. None has the right to be worshiped except Allah alone without associate.",
      translationBn = "আমরা সন্ধ্যায় উপনীত হয়েছি এবং সমগ্র রাজত্ব আল্লাহরই রইল, সমস্ত প্রশংসা আল্লাহর, তিনি একক ও নিরঙ্কুশ।",
      translationEs = "Hemos alcanzado la tarde y el reino pertenece a Allah, y toda alabanza es para Allah. Nadie merece ser adorado excepto Allah, Único y sin asociados.",
      translationPt = "Chegamos à noite e o reino pertence a Allah, e todo louvor é para Allah. Ninguém merece ser adorado exceto Allah, Único e sem associados.",
      reference = "Sahih Muslim 2723",
      repetition = "1x"
    ),
    DuaItem(
      id = "dua_evening_protection",
      title = "Protection Against Any Harm",
      titleBn = "সর্বপ্রকার অনিষ্ট থেকে সুরক্ষার দোয়া",
      titleAr = "دعاء الحماية من كل شر",
      titleEs = "Protección Contra Todo Mal",
      titlePt = "Proteção Contra Todo o Mal",
      category = DuaCategory.EVENING,
      arabic = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
      transliteration = "Bismillahi alladhi la yadurru ma'as-mihi shay'un fil-ardi wa la fis-sama'i wa Huwas-Samee'ul-'Aleem",
      translation = "In the Name of Allah, with Whose Name nothing can cause harm in the earth nor in the heavens, and He is the All-Hearing, the All-Knowing.",
      translationBn = "আল্লাহর নামে, যাঁর নামের বরকতে আসমান ও জমিনের কোনো কিছুই কোনো ক্ষতি করতে পারে না; আর তিনি সর্বশ্রোতা, সর্বজ্ঞ।",
      translationEs = "En el Nombre de Allah, con Cuyo Nombre nada en la tierra ni en el cielo puede causar daño, y Él es el Que Todo lo Oye, el Que Todo lo Sabe.",
      translationPt = "Em Nome de Allah, com Cujo Nome nada na terra nem no céu pode causar dano, e Ele é Quem Tudo Ouve, Quem Tudo Sabe.",
      reference = "Sunan Abi Dawud 5088",
      repetition = "3x"
    ),

    // Prayer & Mosque
    DuaItem(
      id = "dua_enter_mosque",
      title = "Entering the Mosque",
      titleBn = "মসজিদে প্রবেশের দোয়া",
      titleAr = "دعاء دخول المسجد",
      titleEs = "Al Entrar a la Mezquita",
      titlePt = "Ao Entrar na Mesquita",
      category = DuaCategory.PRAYER,
      arabic = "اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ",
      transliteration = "Allahumma-ftah li abwaba rahmatik",
      translation = "O Allah, open the gates of Your mercy for me.",
      translationBn = "হে আল্লাহ, আমার জন্য আপনার রহমতের দরজাগুলো খুলে দিন।",
      translationEs = "¡Oh Allah! Abre para mí las puertas de Tu misericordia.",
      translationPt = "Ó Allah! Abre para mim as portas da Tua misericórdia.",
      reference = "Sahih Muslim 713",
      repetition = "1x"
    ),
    DuaItem(
      id = "dua_leave_mosque",
      title = "Leaving the Mosque",
      titleBn = "মসজিদ থেকে বের হওয়ার দোয়া",
      titleAr = "دعاء الخروج من المسجد",
      titleEs = "Al Salir de la Mezquita",
      titlePt = "Ao Sair da Mesquita",
      category = DuaCategory.PRAYER,
      arabic = "اللَّهُمَّ إِنِّي أَسْأَلُكَ مِنْ فَضْلِكَ",
      transliteration = "Allahumma inni as'aluka min fadlik",
      translation = "O Allah, I ask You of Your bounty.",
      translationBn = "হে আল্লাহ, আমি আপনার অনুগ্রহ ও দান প্রার্থনা করছি।",
      translationEs = "¡Oh Allah! Te pido de Tu inmenso favor y gracia.",
      translationPt = "Ó Allah! Peço-Te da Tua imensa graça e favor.",
      reference = "Sahih Muslim 713",
      repetition = "1x"
    ),
    DuaItem(
      id = "dua_after_salah",
      title = "After Completing Prayer",
      titleBn = "নামাজ সমাপ্তির পরের দোয়া ও ইস্তিগফার",
      titleAr = "الأذكار بعد الصلاة",
      titleEs = "Súplica tras la Oración",
      titlePt = "Súplica após a Oração",
      category = DuaCategory.PRAYER,
      arabic = "أَسْتَغْفِرُ اللَّهَ، أَسْتَغْفِرُ اللَّهَ، أَسْتَغْفِرُ اللَّهَ، اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالإِكْرَامِ",
      transliteration = "Astaghfirullah (3x), Allahumma Antas-Salamu wa minkas-salam, tabarakta ya Dhal-Jalali wal-Ikram",
      translation = "I ask Allah for forgiveness (3x). O Allah, You are Peace and from You comes peace. Blessed are You, O Owner of majesty and honor.",
      translationBn = "আমি আল্লাহর কাছে ক্ষমা চাই (৩ বার)। হে আল্লাহ, আপনিই শান্তি এবং আপনার থেকেই শান্তি আসে। আপনি বরকতময়, হে মর্যাদা ও সম্মানের অধিকারী।",
      translationEs = "Pido perdón a Allah (3 veces). ¡Oh Allah! Tú eres la Paz y de Ti procede la paz. Bendito seas, ¡oh Poseedor de la Majestad y del Honor!",
      translationPt = "Peço perdão a Allah (3 vezes). Ó Allah! Tu és a Paz e de Ti procede a paz. Bendito sejas, ó Possuidor da Majestade e da Honra!",
      reference = "Sahih Muslim 591",
      repetition = "1x"
    ),

    // Daily Life
    DuaItem(
      id = "dua_before_sleep",
      title = "Before Going to Sleep",
      titleBn = "ঘুমানোর পূর্বে দোয়া",
      titleAr = "دعاء النوم",
      titleEs = "Antes de Dormir",
      titlePt = "Antes de Dormir",
      category = DuaCategory.DAILY,
      arabic = "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
      transliteration = "Bismika Allahumma amootu wa ahya",
      translation = "In Your Name, O Allah, I die and I live.",
      translationBn = "হে আল্লাহ, আপনারই নামে আমি মৃত্যুবরণ করি (ঘুমাই) এবং জীবিত হই।",
      translationEs = "En Tu Nombre, ¡oh Allah!, muero (duermo) y vivo.",
      translationPt = "Em Teu Nome, ó Allah, morro (durmo) e vivo.",
      reference = "Sahih al-Bukhari 6324",
      repetition = "1x"
    ),
    DuaItem(
      id = "dua_travel",
      title = "When Traveling",
      titleBn = "সফর বা ভ্রমণের দোয়া",
      titleAr = "دعاء السفر",
      titleEs = "Súplica para el Viaje",
      titlePt = "Súplica para a Viagem",
      category = DuaCategory.DAILY,
      arabic = "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ وَإِنَّا إِلَى رَبِّنَا لَمُنْقَلِبُونَ",
      transliteration = "Subhana alladhi sakh-khara lana hadha wa ma kunna lahu muqrinin, wa inna ila Rabbina lamunqaliboon",
      translation = "Glory to Him who has subjected this to us, and we could never have it by our own efforts. And indeed, to our Lord we will surely return.",
      translationBn = "পবিত্র সেই মহান সত্ত্বা, যিনি এটিকে আমাদের বশীভূত করে দিয়েছেন, অথচ আমরা এটিকে বশীভূত করতে সমর্থ ছিলাম না। আর নিশ্চয়ই আমরা আমাদের প্রতিপালকের দিকেই প্রত্যাবর্তনকারী।",
      translationEs = "Glorificado sea Aquel que puso esto a nuestro servicio, pues nosotros jamás habríamos podido someterlo por nosotros mismos. Y ciertamente, hacia nuestro Señor hemos de retornar.",
      translationPt = "Glorificado seja Aquele que colocou isto ao nosso serviço, pois nós jamais poderíamos tê-lo submetido por nossas próprias forças. E certamente, para o nosso Senhor retornaremos.",
      reference = "Surah Az-Zukhruf (43:13-14)",
      repetition = "1x"
    )
  )
}
