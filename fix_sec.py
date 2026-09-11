import re

with open("app/src/main/AndroidManifest.xml", "r") as f:
    text = f.read()
text = text.replace('android:usesCleartextTraffic="true"', '')
with open("app/src/main/AndroidManifest.xml", "w") as f:
    f.write(text)

with open("app/src/main/java/com/example/ui/HaasiAlLaylViewModel.kt", "r") as f:
    vm = f.read()

new_logic = """
          val url = URL("https://ipapi.co/json/")
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
          val country = json.optString("country_name", "Unknown")
          val lat = json.optDouble("latitude", 0.0)
          val lon = json.optDouble("longitude", 0.0)
          val timezone = json.optString("timezone", "UTC")
"""
vm = re.sub(r'val url = URL\("http://ip-api\.com/json"\)[\s\S]*?val timezone = json\.getString\("timezone"\)', new_logic.strip(), vm)

with open("app/src/main/java/com/example/ui/HaasiAlLaylViewModel.kt", "w") as f:
    f.write(vm)

