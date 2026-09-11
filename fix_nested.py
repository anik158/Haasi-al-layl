with open("app/src/main/java/com/example/data/localization/LocalizationManager.kt", "r") as f:
    lines = f.read().splitlines()

new_lines = []
for line in lines:
    new_lines.append(line)
    if "AppLanguage.ENGLISH -> \"Isha\"" in line:
        new_lines.append("    }")
        new_lines.append("  }")
    elif "AppLanguage.ENGLISH -> \"Unknown Phase\"" in line:
        new_lines.append("    }")
        new_lines.append("  }")

with open("app/src/main/java/com/example/data/localization/LocalizationManager.kt", "w") as f:
    f.write("\n".join(new_lines) + "\n")
