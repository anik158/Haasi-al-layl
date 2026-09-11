import subprocess
try:
    subprocess.run(["kotlinc", "app/src/main/java/com/example/data/localization/LocalizationManager.kt", "-d", "/tmp/loc.jar"], check=True)
    print("Compiled successfully!")
except Exception as e:
    print(f"Failed to compile: {e}")
