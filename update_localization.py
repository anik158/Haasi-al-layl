with open("app/src/main/java/com/example/data/localization/LocalizationManager.kt", "r") as f:
    text = f.read()

# Remove the trailing brace
text = text.rstrip()
if text.endswith("}"):
    text = text[:-1]

additions = """
  fun getNightSkyActiveLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "تتبع السماء ليلاً نشط"
    AppLanguage.BENGALI -> "রাতের আকাশ ও চাঁদ ট্র্যাকিং সক্রিয়"
    AppLanguage.SPANISH -> "Seguimiento Nocturno Activo"
    AppLanguage.PORTUGUESE -> "Rastreamento Noturno Ativo"
    AppLanguage.ENGLISH -> "Night Sky & Lunar Tracking Active"
  }

  fun getDaytimeActiveLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "تتبع الشمس نهاراً نشط"
    AppLanguage.BENGALI -> "দিনের সূর্য ট্র্যাকিং সক্রিয়"
    AppLanguage.SPANISH -> "Seguimiento Solar Activo"
    AppLanguage.PORTUGUESE -> "Rastreamento Solar Ativo"
    AppLanguage.ENGLISH -> "Daytime Solar Tracking Active"
  }

  fun getTimeLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "الوقت:"
    AppLanguage.BENGALI -> "সময়:"
    AppLanguage.SPANISH -> "Hora:"
    AppLanguage.PORTUGUESE -> "Tempo:"
    AppLanguage.ENGLISH -> "Time:"
  }

  fun getSolarMarkersLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "علامات الشمس:"
    AppLanguage.BENGALI -> "সূর্যের অবস্থান:"
    AppLanguage.SPANISH -> "Marcadores Solares:"
    AppLanguage.PORTUGUESE -> "Marcadores Solares:"
    AppLanguage.ENGLISH -> "Solar Markers:"
  }

  fun getLunarDetailsLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "تفاصيل القمر:"
    AppLanguage.BENGALI -> "চাঁদের বিবরণ:"
    AppLanguage.SPANISH -> "Detalles Lunares:"
    AppLanguage.PORTUGUESE -> "Detalhes Lunares:"
    AppLanguage.ENGLISH -> "Lunar Details:"
  }

  fun getHijriDayLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "اليوم الهجري:"
    AppLanguage.BENGALI -> "হিজরি দিন:"
    AppLanguage.SPANISH -> "Día Hijri:"
    AppLanguage.PORTUGUESE -> "Dia Hijri:"
    AppLanguage.ENGLISH -> "Hijri Day:"
  }

  fun getGotItLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "حسناً"
    AppLanguage.BENGALI -> "ঠিক আছে"
    AppLanguage.SPANISH -> "Entendido"
    AppLanguage.PORTUGUESE -> "Entendi"
    AppLanguage.ENGLISH -> "Got it"
  }

  fun getAutoDetectLocationLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "تحديد الموقع تلقائيًا (يتطلب إنترنت)"
    AppLanguage.BENGALI -> "স্বয়ংক্রিয়ভাবে অবস্থান নির্ণয় করুন (ইন্টারনেট প্রয়োজন)"
    AppLanguage.SPANISH -> "Detección Automática (Requiere Internet)"
    AppLanguage.PORTUGUESE -> "Detecção Automática (Requer Internet)"
    AppLanguage.ENGLISH -> "Auto-Detect Location (Requires Internet)"
  }

  fun getChooseLanguageLabel(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "اختر اللغة"
    AppLanguage.BENGALI -> "ভাষা নির্বাচন করুন"
    AppLanguage.SPANISH -> "Elegir Idioma"
    AppLanguage.PORTUGUESE -> "Escolher Idioma"
    AppLanguage.ENGLISH -> "Choose Language"
  }

  fun getWelcomeSetupTitle(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "مرحباً بك في حصي الليل"
    AppLanguage.BENGALI -> "হাসী আল-লাইল এ স্বাগতম"
    AppLanguage.SPANISH -> "Bienvenido a Haasi al-layl"
    AppLanguage.PORTUGUESE -> "Bem-vindo ao Haasi al-layl"
    AppLanguage.ENGLISH -> "Welcome to Haasi al-layl"
  }

  fun getWelcomeSetupSubtitle(language: AppLanguage): String = when (language) {
    AppLanguage.ARABIC -> "يرجى تحديد موقعك ولغتك للبدء"
    AppLanguage.BENGALI -> "শুরু করতে আপনার অবস্থান ও ভাষা নির্বাচন করুন"
    AppLanguage.SPANISH -> "Seleccione su ubicación e idioma para comenzar"
    AppLanguage.PORTUGUESE -> "Selecione sua localização e idioma para começar"
    AppLanguage.ENGLISH -> "Please select your location and language to begin"
  }
}
"""

text = text + additions

with open("app/src/main/java/com/example/data/localization/LocalizationManager.kt", "w") as f:
    f.write(text)
