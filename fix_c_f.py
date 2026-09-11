import re

# WeatherScreen.kt
with open("app/src/main/java/com/example/ui/screens/WeatherScreen.kt", "r") as f:
    ws_text = f.read()

# Change FilterChip to Row with two buttons (C and F)
new_buttons = """
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.height(32.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .background(if (!isFahrenheit) MaterialTheme.colorScheme.primary else Color.Transparent)
                .clickable { if (isFahrenheit) onToggleUnit() }
                .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Text("°C", fontWeight = FontWeight.Bold, color = if (!isFahrenheit) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box(
              modifier = Modifier
                .background(if (isFahrenheit) MaterialTheme.colorScheme.primary else Color.Transparent)
                .clickable { if (!isFahrenheit) onToggleUnit() }
                .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Text("°F", fontWeight = FontWeight.Bold, color = if (isFahrenheit) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }
"""
# Replace FilterChip
ws_text = re.sub(r'FilterChip\([\s\S]*?modifier = Modifier.testTag\("temp_unit_chip"\)\n\s*\)', new_buttons.strip(), ws_text)

with open("app/src/main/java/com/example/ui/screens/WeatherScreen.kt", "w") as f:
    f.write(ws_text)

