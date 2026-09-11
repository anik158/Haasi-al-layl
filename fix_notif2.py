with open("app/src/main/java/com/example/data/notification/PrayerNotificationManager.kt", "r") as f:
    text = f.read()

text = text.replace(
"""
    val title = when (language) {
      AppLanguage.BENGALI -> "তাহাজ্জুদের সময় হয়েছে"
      AppLanguage.ARABIC -> "حان وقت التهجد"
      AppLanguage.ENGLISH -> "Tahajjud Time"
    }

    val message = when (language) {
      AppLanguage.BENGALI -> "আল্লাহ সুবহানাহু ওয়া তায়ালা প্রথম আসমানে নেমে এসেছেন, আপনার যা খুশি চাইতে পারেন।"
      AppLanguage.ARABIC -> "لقد نزل الله سبحانه وتعالى إلى السماء الدنيا، فاسألوا ما شئتم."
      AppLanguage.ENGLISH -> "Allah swt has come down to 1st heaven, ask whatever you want."
    }
""",
"""
    val title = when (language) {
      AppLanguage.BENGALI -> "তাহাজ্জুদের সময় হয়েছে"
      AppLanguage.ARABIC -> "حان وقت التهجد"
      AppLanguage.ENGLISH -> "Tahajjud Time"
      else -> "Tahajjud Time"
    }

    val message = when (language) {
      AppLanguage.BENGALI -> "আল্লাহ সুবহানাহু ওয়া তায়ালা প্রথম আসমানে নেমে এসেছেন, আপনার যা খুশি চাইতে পারেন।"
      AppLanguage.ARABIC -> "لقد نزل الله سبحانه وتعالى إلى السماء الدنيا، فاسألوا ما شئتم."
      AppLanguage.ENGLISH -> "Allah swt has come down to 1st heaven, ask whatever you want."
      else -> "Allah swt has come down to 1st heaven, ask whatever you want."
    }
"""
)

with open("app/src/main/java/com/example/data/notification/PrayerNotificationManager.kt", "w") as f:
    f.write(text)
