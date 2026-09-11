with open("app/src/main/java/com/example/data/localization/LocalizationManager.kt", "r") as f:
    text = f.read()

text = text.rstrip()
if text.endswith("}"):
    text = text[:-1]

additions = """
  fun getWeatherConditionTitle(code: Int, language: AppLanguage): String = when (code) {
    0 -> when(language) {
      AppLanguage.ARABIC -> "سماء صافية"
      AppLanguage.BENGALI -> "পরিষ্কার আকাশ"
      AppLanguage.SPANISH -> "Cielo Despejado"
      AppLanguage.PORTUGUESE -> "Céu Limpo"
      AppLanguage.ENGLISH -> "Clear Sky"
    }
    1, 2, 3 -> when(language) {
      AppLanguage.ARABIC -> "غائم"
      AppLanguage.BENGALI -> "মেঘলা"
      AppLanguage.SPANISH -> "Nublado"
      AppLanguage.PORTUGUESE -> "Nublado"
      AppLanguage.ENGLISH -> "Cloudy"
    }
    45, 48 -> when(language) {
      AppLanguage.ARABIC -> "ضبابي"
      AppLanguage.BENGALI -> "কুয়াশাচ্ছন্ন"
      AppLanguage.SPANISH -> "Niebla"
      AppLanguage.PORTUGUESE -> "Névoa"
      AppLanguage.ENGLISH -> "Foggy"
    }
    51, 53, 55, 61, 63, 65, 80, 81, 82 -> when(language) {
      AppLanguage.ARABIC -> "ممطر"
      AppLanguage.BENGALI -> "বৃষ্টি"
      AppLanguage.SPANISH -> "Lluvia"
      AppLanguage.PORTUGUESE -> "Chuva"
      AppLanguage.ENGLISH -> "Rain"
    }
    71, 73, 75, 77, 85, 86 -> when(language) {
      AppLanguage.ARABIC -> "مثلج"
      AppLanguage.BENGALI -> "তুষারপাত"
      AppLanguage.SPANISH -> "Nieve"
      AppLanguage.PORTUGUESE -> "Neve"
      AppLanguage.ENGLISH -> "Snow"
    }
    95, 96, 99 -> when(language) {
      AppLanguage.ARABIC -> "عاصفة رعدية"
      AppLanguage.BENGALI -> "বজ্রঝড়"
      AppLanguage.SPANISH -> "Tormenta Eléctrica"
      AppLanguage.PORTUGUESE -> "Trovoada"
      AppLanguage.ENGLISH -> "Thunderstorm"
    }
    else -> when(language) {
      AppLanguage.ARABIC -> "معتدل"
      AppLanguage.BENGALI -> "স্বাভাবিক"
      AppLanguage.SPANISH -> "Justo"
      AppLanguage.PORTUGUESE -> "Justo"
      AppLanguage.ENGLISH -> "Fair"
    }
  }

  fun getIslamicWeatherAdvice(code: Int, tempC: Double, language: AppLanguage): String = when {
    code in listOf(95, 96, 99) -> when (language) {
      AppLanguage.ARABIC -> "عاصفة رعدية في المنطقة. احتمي بالداخل واقرأ دعاء الرعد."
      AppLanguage.BENGALI -> "এলাকায় বজ্রঝড়। নিরাপদ স্থানে থাকুন এবং বজ্রপাতের দোয়া পড়ুন।"
      AppLanguage.SPANISH -> "Tormenta en la zona. Refúgiate y recita la Du'a para truenos."
      AppLanguage.PORTUGUESE -> "Tempestade na área. Abrigue-se e recite a Du'a para trovões."
      AppLanguage.ENGLISH -> "Thunderstorm in the area. Take shelter indoors and recite the Du'a for lightning and thunder."
    }
    code in listOf(51, 53, 55, 61, 63, 65, 80, 81, 82) -> when (language) {
      AppLanguage.ARABIC -> "المطر يتساقط — أشار النبي (ﷺ) إلى أن هذا وقت مبارك لإجابة الدعاء!"
      AppLanguage.BENGALI -> "বৃষ্টি পড়ছে — নবী (সাঃ) বলেছেন এটি দোয়া কবুলের বরকতময় সময়!"
      AppLanguage.SPANISH -> "Cae lluvia — ¡El Profeta (ﷺ) notó que es un tiempo bendito para orar!"
      AppLanguage.PORTUGUESE -> "A chuva cai — O Profeta (ﷺ) notou ser um tempo abençoado para orar!"
      AppLanguage.ENGLISH -> "Rain is descending — the Prophet (ﷺ) noted this is a blessed time for prayers to be answered!"
    }
    tempC > 36 -> when (language) {
      AppLanguage.ARABIC -> "درجات حرارة عالية اليوم. حافظ على رطوبة جسمك واسلك طرقًا مظللة إلى المسجد."
      AppLanguage.BENGALI -> "আজ তাপমাত্রা বেশি। পর্যাপ্ত পানি পান করুন এবং মসজিদে যাওয়ার সময় ছায়াযুক্ত পথ ব্যবহার করুন।"
      AppLanguage.SPANISH -> "Altas temperaturas. Mantente hidratado y busca sombra al ir a la mezquita."
      AppLanguage.PORTUGUESE -> "Altas temperaturas. Mantenha-se hidratado e busque sombra ao ir à mesquita."
      AppLanguage.ENGLISH -> "High temperatures today. Keep well-hydrated and seek shaded routes to the mosque."
    }
    tempC < 5 -> when (language) {
      AppLanguage.ARABIC -> "ظروف باردة اليوم. توضأ جيدًا بالماء الدافئ وارتدِ ملابس دافئة لصلاة الفجر والعشاء."
      AppLanguage.BENGALI -> "আজ ঠান্ডা আবহাওয়া। গরম পানি দিয়ে ভালোভাবে ওযু করুন এবং ফজর/এশার জন্য গরম কাপড় পরুন।"
      AppLanguage.SPANISH -> "Condiciones frías. Haz el wudhu completo y abrígate para Fajr/Isha."
      AppLanguage.PORTUGUESE -> "Condições frias. Faça o wudhu completo e agasalhe-se para Fajr/Isha."
      AppLanguage.ENGLISH -> "Cold conditions today. Perform wudhu thoroughly with warmth and dress warmly for Fajr/Isha."
    }
    else -> when (language) {
      AppLanguage.ARABIC -> "طقس مريح وهادئ — فرصة ممتازة للمشي إلى المسجد لأداء صلاة الجماعة."
      AppLanguage.BENGALI -> "আরামদায়ক এবং শান্ত আবহাওয়া — জামাতে নামাজ পড়ার জন্য মসজিদে হেঁটে যাওয়ার চমৎকার সুযোগ।"
      AppLanguage.SPANISH -> "Clima cómodo y sereno — excelente oportunidad para caminar a la Mezquita."
      AppLanguage.PORTUGUESE -> "Clima confortável e sereno — excelente oportunidade para caminhar até a Mesquita."
      AppLanguage.ENGLISH -> "Comfortable and serene weather — an excellent opportunity to walk to the Masjid for congregational prayer."
    }
  }
}
"""
text = text + additions

with open("app/src/main/java/com/example/data/localization/LocalizationManager.kt", "w") as f:
    f.write(text)
