with open("app/src/main/java/com/example/ui/components/CelestialHorizonCard.kt", "r") as f:
    text = f.read()

# Only keep one import
text = text.replace('import androidx.compose.material3.ExperimentalMaterial3Api\n\nimport androidx.compose.material3.ExperimentalMaterial3Api', 'import androidx.compose.material3.ExperimentalMaterial3Api')
text = text.replace('import androidx.compose.material3.ExperimentalMaterial3Api\nimport androidx.compose.material3.ExperimentalMaterial3Api', 'import androidx.compose.material3.ExperimentalMaterial3Api')

with open("app/src/main/java/com/example/ui/components/CelestialHorizonCard.kt", "w") as f:
    f.write(text)

