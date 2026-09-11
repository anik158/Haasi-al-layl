import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    text = f.read()

tahajjud_banner = """
      if (schedule.isTahajjudTime) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp)
          ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
              Spacer(modifier = Modifier.width(16.dp))
              Text(
                text = if (language == com.example.data.localization.AppLanguage.BENGALI) "আল্লাহ সুবহানাহু ওয়া তায়ালা প্রথম আসমানে নেমে এসেছেন, আপনার যা খুশি চাইতে পারেন।"
                       else if (language == com.example.data.localization.AppLanguage.ARABIC) "لقد نزل الله سبحانه وتعالى إلى السماء الدنيا، فاسألوا ما شئتم."
                       else "Allah swt has come down to 1st heaven, ask whatever you want.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
              )
            }
          }
        }
      }

"""

text = re.sub(
    r'(PrayerNextHeroCard\([\s\S]*?\n\s*\))',
    r'\1\n' + tahajjud_banner,
    text
)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(text)
