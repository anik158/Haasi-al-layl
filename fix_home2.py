with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    text = f.read()

text = text.replace(
"""
      if (schedule.isTahajjudTime) {
        item {
          Card(
""",
"""
      }

      if (schedule.isTahajjudTime) {
        item {
          Card(
""")
text = text.replace(
"""        }
      }


      }

      // 2. Forbidden Prayer Times Live Status Banner (Clickable to open Notification Center)
""",
"""        }
      }

      // 2. Forbidden Prayer Times Live Status Banner (Clickable to open Notification Center)
""")

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(text)
