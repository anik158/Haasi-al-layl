# 2. HaasiAlLaylViewModel.kt
with open("app/src/main/java/com/example/ui/HaasiAlLaylViewModel.kt", "r") as f:
    text = f.read()
# Removing duplicate Dispatchers import
lines = text.splitlines()
out = []
seen_dispatchers = False
for line in lines:
    if "import kotlinx.coroutines.Dispatchers" in line:
        if not seen_dispatchers:
            seen_dispatchers = True
            out.append(line)
    else:
        out.append(line)
with open("app/src/main/java/com/example/ui/HaasiAlLaylViewModel.kt", "w") as f:
    f.write("\n".join(out))

# 6. DuaScreen.kt
with open("app/src/main/java/com/example/ui/screens/DuaScreen.kt", "r") as f:
    text = f.read()

lines = text.splitlines()
out = []
seen_arrangement = False
for line in lines:
    if "import androidx.compose.foundation.layout.Arrangement" in line:
        if not seen_arrangement:
            seen_arrangement = True
            out.append(line)
    else:
        out.append(line)
with open("app/src/main/java/com/example/ui/screens/DuaScreen.kt", "w") as f:
    f.write("\n".join(out))
