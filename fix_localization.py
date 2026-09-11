import re

with open("app/src/main/java/com/example/data/localization/LocalizationManager.kt", "r") as f:
    lines = f.read().splitlines()

new_lines = []
for i, line in enumerate(lines):
    if line.strip() == "}":
        # Check if previous was also "}"
        if new_lines and new_lines[-1].strip() == "}":
            continue # skip duplicate
    new_lines.append(line)

# Add closing brace for enum class before object LocalizationManager
for i in range(len(new_lines)):
    if new_lines[i].strip() == "object LocalizationManager {":
        new_lines.insert(i, "}")
        break

# Add final closing brace if missing
if new_lines[-1].strip() != "}":
    new_lines.append("}")

with open("app/src/main/java/com/example/data/localization/LocalizationManager.kt", "w") as f:
    f.write("\n".join(new_lines) + "\n")
