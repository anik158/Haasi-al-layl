with open("app/src/main/java/com/example/ui/components/CelestialHorizonCard.kt", "r") as f:
    text = f.read()
    
# In case sed didn't work for some reason
text = text.replace(
    'Text("Got it", color = DarkForestInk, fontWeight = FontWeight.Bold)',
    'Text(LocalizationManager.getGotItLabel(language), color = DarkForestInk, fontWeight = FontWeight.Bold)'
)
with open("app/src/main/java/com/example/ui/components/CelestialHorizonCard.kt", "w") as f:
    f.write(text)
