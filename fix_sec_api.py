import re

with open("app/src/main/java/com/example/ui/HaasiAlLaylViewModel.kt", "r") as f:
    vm = f.read()

new_logic = """
          val url = URL("https://ipinfo.io/json")
          val connection = url.openConnection() as java.net.HttpURLConnection
          connection.requestMethod = "GET"
          connection.setRequestProperty("User-Agent", "Mozilla/5.0")
          connection.connectTimeout = 5000
          connection.readTimeout = 5000
          
          if (connection.responseCode == 200) {
            val reader = java.io.BufferedReader(java.io.InputStreamReader(connection.inputStream))
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
          val json = org.json.JSONObject(jsonString)
          val city = json.optString("city", "Unknown")
          val country = json.optString("country", "Unknown")
          val loc = json.optString("loc", "0.0,0.0").split(",")
          val lat = loc.getOrNull(0)?.toDoubleOrNull() ?: 0.0
          val lon = loc.getOrNull(1)?.toDoubleOrNull() ?: 0.0
          val timezone = json.optString("timezone", "UTC")
"""
vm = re.sub(r'val url = URL\("https://ipapi\.co/json/"\)[\s\S]*?val timezone = json\.optString\("timezone", "UTC"\)', new_logic.strip(), vm)

with open("app/src/main/java/com/example/ui/HaasiAlLaylViewModel.kt", "w") as f:
    f.write(vm)

