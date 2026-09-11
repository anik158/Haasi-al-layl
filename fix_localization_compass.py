with open("app/src/main/java/com/example/data/localization/LocalizationManager.kt", "r") as f:
    text = f.read()

text = text.replace(
    'AppLanguage.BENGALI -> "কম্পাসের নির্ভুলতার জন্য ফোনটিকে ৮ এর মতো করে ঘোরান"',
    'AppLanguage.BENGALI -> "কম্পাসের নির্ভুলতার জন্য ফোনটিকে ইংরেজি \\\'8\\\' (আট) আকৃতিতে ঘোরান"'
)

with open("app/src/main/java/com/example/data/localization/LocalizationManager.kt", "w") as f:
    f.write(text)

