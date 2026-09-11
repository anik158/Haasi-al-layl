with open("app/src/main/java/com/example/data/localization/LocalizationManager.kt", "r") as f:
    text = f.read()

# Remove double braces `  }\n  }`
while "\n  }\n  }" in text:
    text = text.replace("\n  }\n  }", "\n  }")

# Ensure the file ends with a single `}`
text = text.strip()
if not text.endswith("}"):
    text += "\n}"

with open("app/src/main/java/com/example/data/localization/LocalizationManager.kt", "w") as f:
    f.write(text + "\n")
