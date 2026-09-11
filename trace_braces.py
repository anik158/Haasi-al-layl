with open("app/src/main/java/com/example/data/localization/LocalizationManager.kt", "r") as f:
    text = f.read()

lines = text.splitlines()
depth = 0
in_localization_manager = False

for i, line in enumerate(lines):
    if "object LocalizationManager {" in line:
        in_localization_manager = True
    
    depth += line.count("{")
    depth -= line.count("}")
    
    if in_localization_manager and depth == 0:
        print(f"LocalizationManager closed at line {i+1}: {line}")
        break
