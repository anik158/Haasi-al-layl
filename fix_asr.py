with open("app/src/main/java/com/example/data/prayer/PrayerCalculator.kt", "r") as f:
    text = f.read()

text = text.replace(
    'val asrAngle = -Math.toDegrees(',
    'val asrAngle = Math.toDegrees('
)

with open("app/src/main/java/com/example/data/prayer/PrayerCalculator.kt", "w") as f:
    f.write(text)
