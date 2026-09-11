with open("app/src/main/java/com/example/ui/HaasiAlLaylViewModel.kt", "r") as f:
    text = f.read()

import_lines = """import java.net.URL
import java.net.HttpURLConnection
import java.io.InputStreamReader
import java.io.BufferedReader
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
"""
text = text.replace("import kotlinx.coroutines.launch", "import kotlinx.coroutines.launch\n" + import_lines)

sync_method = """
  fun autoDetectLocation() {
    viewModelScope.launch {
      try {
        val jsonString = withContext(Dispatchers.IO) {
          val url = URL("http://ip-api.com/json")
          val connection = url.openConnection() as HttpURLConnection
          connection.requestMethod = "GET"
          connection.connectTimeout = 5000
          connection.readTimeout = 5000
          
          if (connection.responseCode == 200) {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
              response.append(line)
            }
            reader.close()
            response.toString()
          } else {
            null
          }
        }
        
        if (jsonString != null) {
          val json = JSONObject(jsonString)
          val city = json.getString("city")
          val country = json.getString("country")
          val lat = json.getDouble("lat")
          val lon = json.getDouble("lon")
          val timezone = json.getString("timezone")
          
          val detectedLocation = CityLocation(city, country, lat, lon, timezone)
          _selectedCity.value = detectedLocation
        }
      } catch (e: Exception) {
        Log.e("HaasiAlLaylViewModel", "Error auto-detecting location", e)
      }
    }
  }
"""

text = text.replace("fun updateCity(city: CityLocation) {", sync_method + "\n  fun updateCity(city: CityLocation) {")

with open("app/src/main/java/com/example/ui/HaasiAlLaylViewModel.kt", "w") as f:
    f.write(text)

