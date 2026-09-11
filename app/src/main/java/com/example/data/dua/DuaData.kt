package com.example.data.dua

enum class DuaCategory(val displayName: String) {
  ALL("All"),
  WEATHER("Weather & Nature"),
  MORNING("Morning Adhkar"),
  EVENING("Evening Adhkar"),
  PRAYER("Prayer & Mosque"),
  DAILY("Daily Life")
}

data class DuaItem(
  val id: String,
  val title: String,
  val category: DuaCategory,
  val arabic: String,
  val transliteration: String,
  val translation: String,
  val reference: String,
  val repetition: String = "1x"
)

data class DailyInspiration(
  val ayahArabic: String,
  val ayahTranslation: String,
  val ayahSurah: String,
  val hadithArabic: String,
  val hadithTranslation: String,
  val hadithNarrator: String,
  val hadithSource: String
)

object IslamicContentProvider {

  val DAILY_INSPIRATION = DailyInspiration(
    ayahArabic = "إِنَّ الصَّلَاةَ كَانَتْ عَلَى الْمُؤْمِنِينَ كِتَابًا مَّوْقُوتًا",
    ayahTranslation = "Indeed, prayer has been decreed upon the believers a decree of specified times.",
    ayahSurah = "Surah An-Nisa (4:103)",
    hadithArabic = "مَنْ سَلَكَ طَرِيقًا يَلْتَمِسُ فِيهِ عِلْمًا سَهَّلَ اللَّهُ لَهُ بِهِ طَرِيقًا إِلَى الْجَنَّةِ",
    hadithTranslation = "Whoever takes a path upon which he seeks knowledge, Allah makes the path to Paradise easy for him.",
    hadithNarrator = "Abu Hurairah (RA)",
    hadithSource = "Sahih Muslim 2699"
  )

  val DUAS = listOf(
    // Weather & Nature Duas
    DuaItem(
      id = "dua_rain",
      title = "When Rain Descends",
      category = DuaCategory.WEATHER,
      arabic = "اللَّهُمَّ صَيِّبًا نَافِعًا",
      transliteration = "Allahumma sayyiban nafi'an",
      translation = "O Allah, make it a beneficial downpour.",
      reference = "Sahih al-Bukhari 1032",
      repetition = "1x"
    ),
    DuaItem(
      id = "dua_thunder",
      title = "When Hearing Thunder",
      category = DuaCategory.WEATHER,
      arabic = "سُبْحَانَ الَّذِي يُسَبِّحُ الرَّعْدُ بِحَمْدِهِ وَالْمَلَائِكَةُ مِنْ خِيفَتِهِ",
      transliteration = "Subhana alladhi yusabbihu ar-ra'du bihamdihi wal-mala'ikatu min kheefatih",
      translation = "Glory is to Him Whom thunder glorifies with His praise, and the angels out of fear of Him.",
      reference = "Muwatta Malik 1808",
      repetition = "1x"
    ),
    DuaItem(
      id = "dua_wind",
      title = "When Strong Winds Blow",
      category = DuaCategory.WEATHER,
      arabic = "اللَّهُمَّ إِنِّي أَسْأَلُكَ خَيْرَهَا وَخَيْرَ مَا فِيهَا وَخَيْرَ مَا أُرْسِلَتْ بِهِ، وَأَعُوذُ بِكَ مِنْ شَرِّهَا وَشَرِّ مَا فِيهَا وَشَرِّ مَا أُرْسِلَتْ بِهِ",
      transliteration = "Allahumma inni as'aluka khayraha wa khayra ma feeha wa khayra ma ursilat bihi, wa a'udhu bika min sharriha wa sharri ma feeha wa sharri ma ursilat bih",
      translation = "O Allah, I ask You for its good, the good that is in it, and the good with which it was sent; and I seek refuge in You from its evil, the evil that is in it, and the evil with which it was sent.",
      reference = "Sahih Muslim 899",
      repetition = "1x"
    ),
    DuaItem(
      id = "dua_heat",
      title = "During Severe Heat",
      category = DuaCategory.WEATHER,
      arabic = "لَا إِلَهَ إِلَّا اللَّهُ، مَا أَشَدَّ حَرَّ هَذَا الْيَوْمِ، اللَّهُمَّ أَجِرْنِي مِنْ حَرِّ جَهَنَّمَ",
      transliteration = "La ilaha illallah, ma ashadda harra hadhal-yawm, Allahumma ajirni min harri Jahannam",
      translation = "There is no deity worthy of worship except Allah; how intense is the heat of this day! O Allah, protect me from the heat of Hellfire.",
      reference = "Ibn as-Sunni 306",
      repetition = "1x"
    ),

    // Morning Adhkar
    DuaItem(
      id = "dua_morning_waking",
      title = "Upon Waking Up",
      category = DuaCategory.MORNING,
      arabic = "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
      transliteration = "Alhamdu lillahil-ladhi ahyana ba'da ma amatana wa ilayhin-nushoor",
      translation = "All praise is for Allah who gave us life after having given us death, and unto Him is the resurrection.",
      reference = "Sahih al-Bukhari 6312",
      repetition = "1x"
    ),
    DuaItem(
      id = "dua_morning_sayyid",
      title = "Sayyid al-Istighfar (Chief Prayer for Forgiveness)",
      category = DuaCategory.MORNING,
      arabic = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
      transliteration = "Allahumma Anta Rabbi la ilaha illa Ant, khalaqtani wa ana 'abduk, wa ana 'ala 'ahdika wa wa'dika mastata't, a'udhu bika min sharri ma sana't, aboo'u laka bini'matika 'alayya, wa aboo'u bidhanbi faghfir li, fa'innahu la yaghfirudh-dhunooba illa Ant",
      translation = "O Allah, You are my Lord, none has the right to be worshiped but You. You created me and I am Your servant. I am committed to Your covenant and promise as much as I can. I seek refuge in You from the evil of what I have done. I acknowledge Your favors upon me and I acknowledge my sins, so forgive me, for none forgives sins except You.",
      reference = "Sahih al-Bukhari 6306",
      repetition = "Morning & Evening"
    ),
    DuaItem(
      id = "dua_morning_protection",
      title = "Protection in the Morning",
      category = DuaCategory.MORNING,
      arabic = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
      transliteration = "Asbahna wa asbahal-mulku lillah, wal-hamdu lillah, la ilaha illallahu wahdahu la shareeka lah, lahul-mulku wa lahul-hamd, wa Huwa 'ala kulli shay'in qadeer",
      translation = "We have entered a new morning and the dominion belongs to Allah, and all praise is for Allah. There is no deity worthy of worship except Allah alone, without partner. To Him belongs sovereignty and to Him belongs all praise, and He has power over all things.",
      reference = "Sahih Muslim 2723",
      repetition = "1x"
    ),

    // Evening Adhkar
    DuaItem(
      id = "dua_evening_start",
      title = "Entering the Evening",
      category = DuaCategory.EVENING,
      arabic = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
      transliteration = "Amsayna wa amsal-mulku lillah, wal-hamdu lillah, la ilaha illallahu wahdahu la shareeka lah",
      translation = "We have reached the evening and the kingdom belongs to Allah, and all praise is for Allah. None has the right to be worshiped except Allah alone without associate.",
      reference = "Sahih Muslim 2723",
      repetition = "1x"
    ),
    DuaItem(
      id = "dua_evening_protection",
      title = "Evening Protection from Harm",
      category = DuaCategory.EVENING,
      arabic = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
      transliteration = "Bismillahi alladhi la yadurru ma'as-mihi shay'un fil-ardi wa la fis-sama'i wa Huwas-Samee'ul-'Aleem",
      translation = "In the Name of Allah, with Whose Name nothing can cause harm in the earth nor in the heavens, and He is the All-Hearing, the All-Knowing.",
      reference = "Sunan Abi Dawud 5088",
      repetition = "3x"
    ),

    // Prayer & Mosque
    DuaItem(
      id = "dua_enter_mosque",
      title = "Entering the Mosque",
      category = DuaCategory.PRAYER,
      arabic = "اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ",
      transliteration = "Allahumma-ftah li abwaba rahmatik",
      translation = "O Allah, open the gates of Your mercy for me.",
      reference = "Sahih Muslim 713",
      repetition = "1x"
    ),
    DuaItem(
      id = "dua_leave_mosque",
      title = "Leaving the Mosque",
      category = DuaCategory.PRAYER,
      arabic = "اللَّهُمَّ إِنِّي أَسْأَلُكَ مِنْ فَضْلِكَ",
      transliteration = "Allahumma inni as'aluka min fadlik",
      translation = "O Allah, I ask You of Your bounty.",
      reference = "Sahih Muslim 713",
      repetition = "1x"
    ),
    DuaItem(
      id = "dua_after_salah",
      title = "After Completing Prayer",
      category = DuaCategory.PRAYER,
      arabic = "أَسْتَغْفِرُ اللَّهَ، أَسْتَغْفِرُ اللَّهَ، أَسْتَغْفِرُ اللَّهَ، اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالإِكْرَامِ",
      transliteration = "Astaghfirullah (3x), Allahumma Antas-Salamu wa minkas-salam, tabarakta ya Dhal-Jalali wal-Ikram",
      translation = "I ask Allah for forgiveness (3x). O Allah, You are Peace and from You comes peace. Blessed are You, O Owner of majesty and honor.",
      reference = "Sahih Muslim 591",
      repetition = "1x"
    ),

    // Daily Life
    DuaItem(
      id = "dua_before_sleep",
      title = "Before Going to Sleep",
      category = DuaCategory.DAILY,
      arabic = "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
      transliteration = "Bismika Allahumma amootu wa ahya",
      translation = "In Your Name, O Allah, I die and I live.",
      reference = "Sahih al-Bukhari 6324",
      repetition = "1x"
    ),
    DuaItem(
      id = "dua_travel",
      title = "When Traveling",
      category = DuaCategory.DAILY,
      arabic = "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ وَإِنَّا إِلَى رَبِّنَا لَمُنْقَلِبُونَ",
      transliteration = "Subhana alladhi sakh-khara lana hadha wa ma kunna lahu muqrinin, wa inna ila Rabbina lamunqaliboon",
      translation = "Glory to Him who has subjected this to us, and we could never have it by our own efforts. And indeed, to our Lord we will surely return.",
      reference = "Surah Az-Zukhruf (43:13-14)",
      repetition = "1x"
    )
  )
}
