package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.dua.IslamicContentProvider
import com.example.data.local.HaasiAlLaylDatabase
import com.example.data.local.PrayerLogEntity
import com.example.data.local.TasbihEntity
import com.example.data.model.CityLocation
import com.example.data.model.DailyPrayerSchedule
import com.example.data.prayer.CalculationMethod
import com.example.data.prayer.JuristicMethod
import com.example.data.prayer.PrayerCalculator
import com.example.data.qibla.QiblaCalculator
import com.example.data.qibla.QiblaSensorManager
import com.example.data.repository.HaasiAlLaylRepository
import com.example.data.weather.FullWeatherData
import com.example.data.weather.WeatherRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.URL
import java.net.HttpURLConnection
import java.io.InputStreamReader
import java.io.BufferedReader
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

import kotlinx.coroutines.flow.flatMapLatest
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.abs

sealed interface WeatherUiState {
  object Loading : WeatherUiState
  data class Success(val data: FullWeatherData) : WeatherUiState
  data class Error(val message: String) : WeatherUiState
}

class HaasiAlLaylViewModel(application: Application) : AndroidViewModel(application) {

  private val database = HaasiAlLaylDatabase.getDatabase(application)
  private val repository = HaasiAlLaylRepository(database.haasiAlLaylDao())
  private val weatherRepository = WeatherRepository()
  private val sensorManager = QiblaSensorManager(application)

  // Selected Location
  private val _selectedCity = MutableStateFlow(PrayerCalculator.PRESET_CITIES[0]) // Default to Makkah
  val selectedCity: StateFlow<CityLocation> = _selectedCity.asStateFlow()

  // App Language (Auto location-based by default, can be overridden)
  private val _manualLanguage = MutableStateFlow<com.example.data.localization.AppLanguage?>(null)
  val manualLanguage: StateFlow<com.example.data.localization.AppLanguage?> = _manualLanguage.asStateFlow()

  val currentLanguage: StateFlow<com.example.data.localization.AppLanguage> = combine(
    _selectedCity,
    _manualLanguage
  ) { city, manual ->
    manual ?: com.example.data.localization.LocalizationManager.detectLanguageForCountry(city.country)
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    com.example.data.localization.LocalizationManager.detectLanguageForCountry(PrayerCalculator.PRESET_CITIES[0].country)
  )

  // Calculation Settings
  val calculationMethod = MutableStateFlow(CalculationMethod.MUSLIM_WORLD_LEAGUE)
  val juristicMethod = MutableStateFlow(com.example.data.prayer.IslamicMadhab.SHAFI)
  val selectedMadhab: StateFlow<com.example.data.prayer.IslamicMadhab> = juristicMethod.asStateFlow()

  // Temperature unit ('C' or 'F')
  private val _isFahrenheit = MutableStateFlow(false)
  val isFahrenheit: StateFlow<Boolean> = _isFahrenheit.asStateFlow()

  // Selected Bottom Nav Tab
  private val _selectedTab = MutableStateFlow(0)
  val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

  // Live Timer trigger (updates every second for real-time countdown)
  private val _secondTicker = MutableStateFlow(System.currentTimeMillis())

  // Hisn Al-Muslim Duas
  private val _hisnDuas = MutableStateFlow<List<com.example.data.dua.DuaItem>>(emptyList())
  val allDuas: StateFlow<List<com.example.data.dua.DuaItem>> = combine(
    _hisnDuas,
    MutableStateFlow(com.example.data.dua.IslamicContentProvider.DUAS)
  ) { hisn, builtIn ->
    builtIn + hisn
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.data.dua.IslamicContentProvider.DUAS)

  // Weather state
  private val _weatherState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
  val weatherState: StateFlow<WeatherUiState> = _weatherState.asStateFlow()

  // Compass Heading
  val compassHeading: StateFlow<Float> = sensorManager.getCompassHeadingFlow()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

  // Get current ZonedDateTime for a given city
  fun getCityZonedDateTime(city: CityLocation): ZonedDateTime {
    val zone = try {
      ZoneId.of(city.timeZoneId)
    } catch (e: Exception) {
      ZoneId.systemDefault()
    }
    return ZonedDateTime.now(zone)
  }

  // Today's Prayer log from Room for selected city's local date
  val todayPrayerLog: StateFlow<PrayerLogEntity?> = _selectedCity.flatMapLatest { city ->
    val cityDate = getCityZonedDateTime(city).toLocalDate()
    repository.getPrayerLogForDate(cityDate)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  // Daily Prayer Schedule (Live calculated using city's local timezone)
  val prayerSchedule: StateFlow<DailyPrayerSchedule> = combine(
    _selectedCity,
    todayPrayerLog,
    calculationMethod,
    juristicMethod,
    _secondTicker
  ) { city, log, method, juristic, _ ->
    val cityZoned = getCityZonedDateTime(city)
    val cityDate = cityZoned.toLocalDate()
    val cityDateTime = cityZoned.toLocalDateTime()

    val completedSet = mutableSetOf<String>()
    if (log != null) {
      if (log.fajrDone) completedSet.add("FAJR")
      if (log.dhuhrDone) completedSet.add("DHUHR")
      if (log.asrDone) completedSet.add("ASR")
      if (log.maghribDone) completedSet.add("MAGHRIB")
      if (log.ishaDone) completedSet.add("ISHA")
    }
    PrayerCalculator.calculateSchedule(
      date = cityDate,
      location = city,
      now = cityDateTime,
      completedPrayerIds = completedSet,
      method = method,
      madhab = juristic
    )
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    PrayerCalculator.calculateSchedule(
      date = getCityZonedDateTime(_selectedCity.value).toLocalDate(),
      location = _selectedCity.value,
      now = getCityZonedDateTime(_selectedCity.value).toLocalDateTime(),
      madhab = com.example.data.prayer.IslamicMadhab.SHAFI
    )
  )

  // Qibla bearing & distance
  val qiblaBearing: StateFlow<Float> = _selectedCity.map { city ->
    QiblaCalculator.calculateQiblaBearing(city.latitude, city.longitude)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

  val distanceToKaabaKm: StateFlow<Int> = _selectedCity.map { city ->
    QiblaCalculator.calculateDistanceToKaabaKm(city.latitude, city.longitude)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  // True if user device is oriented towards Qibla (within +/- 3 degrees)
  val isFacingQibla: StateFlow<Boolean> = combine(compassHeading, qiblaBearing) { heading, bearing ->
    val diff = abs((bearing - heading + 540f) % 360f - 180f)
    diff < 4.0f
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

  // Tasbih list from Room
  val tasbihList: StateFlow<List<TasbihEntity>> = repository.getAllTasbihItems()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _selectedTasbihId = MutableStateFlow<String?>("subhanallah")
  val selectedTasbih: StateFlow<TasbihEntity?> = combine(tasbihList, _selectedTasbihId) { list, id ->
    list.find { it.id == id } ?: list.firstOrNull()
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  // Du'a Bookmarks from Room
  val bookmarkedDuaIds: StateFlow<Set<String>> = repository.getAllBookmarks()
    .map { list -> list.map { it.duaId }.toSet() }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

  private var tickerJob: Job? = null

  // State to prevent spamming the Tahajjud notification
  private var lastTahajjudNotifiedDate: java.time.LocalDate? = null

  init {
    viewModelScope.launch {
      prayerSchedule.collect { schedule ->
        if (schedule.isTahajjudTime) {
          val today = schedule.date
          if (lastTahajjudNotifiedDate != today) {
            lastTahajjudNotifiedDate = today
            com.example.data.notification.PrayerNotificationManager.showTahajjudNotification(getApplication<android.app.Application>(), currentLanguage.value)
            // Show emotional message via notification (since we are reusing the Prayer Notification, 
            // maybe we can send a custom one instead, but let's build a custom notification method for Tahajjud)
          }
        }
      }
    }

    startSecondTicker()
    seedTasbihIfNeeded()
    refreshWeather()
    loadHisnAlMuslimData()
  }

  private fun loadHisnAlMuslimData() {
    viewModelScope.launch(Dispatchers.IO) {
      try {
        val jsonString = getApplication<Application>().assets.open("hisn_al_muslim.json").bufferedReader().use { it.readText() }
        val root = org.json.JSONObject(jsonString)
        val englishArray = root.getJSONArray("English")
        
        val items = mutableListOf<com.example.data.dua.DuaItem>()
        for (i in 0 until englishArray.length()) {
          val categoryObj = englishArray.getJSONObject(i)
          val categoryTitle = categoryObj.optString("TITLE", "Category")
          val textArray = categoryObj.optJSONArray("TEXT") ?: continue
          
          for (j in 0 until textArray.length()) {
            val duaObj = textArray.getJSONObject(j)
            val id = duaObj.optString("ID")
            val arabic = duaObj.optString("ARABIC_TEXT")
            val transliteration = duaObj.optString("LANGUAGE_ARABIC_TRANSLATED_TEXT")
            val translation = duaObj.optString("TRANSLATED_TEXT")
            val repeat = duaObj.optInt("REPEAT", 1)
            
            items.add(
              com.example.data.dua.DuaItem(
                id = "hisn_${i}_${id}",
                title = categoryTitle,
                category = com.example.data.dua.DuaCategory.ALL,
                arabic = arabic,
                transliteration = transliteration,
                translation = translation,
                reference = "Hisn al-Muslim",
                repetition = "${repeat}x"
              )
            )
          }
        }
        _hisnDuas.value = items
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  private fun startSecondTicker() {
    tickerJob?.cancel()
    tickerJob = viewModelScope.launch {
      while (isActive) {
        delay(1000)
        _secondTicker.value = System.currentTimeMillis()
      }
    }
  }

  private fun seedTasbihIfNeeded() {
    viewModelScope.launch {
      repository.seedDefaultTasbihIfEmpty()
    }
  }

  fun setTab(index: Int) {
    _selectedTab.value = index
  }


  fun autoDetectLocation() {
    viewModelScope.launch {
      try {
        val jsonString = withContext(Dispatchers.IO) {
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
          
          val detectedLocation = CityLocation(city, country, lat, lon, timezone)
          _selectedCity.value = detectedLocation
        }
      } catch (e: Exception) {
        Log.e("HaasiAlLaylViewModel", "Error auto-detecting location", e)
      }
    }
  }

  fun selectCity(city: CityLocation) {
    _selectedCity.value = city
    refreshWeather()
  }

  fun setLanguage(language: com.example.data.localization.AppLanguage?) {
    _manualLanguage.value = language
  }

  fun resetLanguageToLocation() {
    _manualLanguage.value = null
  }

  fun setMadhab(madhab: com.example.data.prayer.IslamicMadhab) {
    juristicMethod.value = madhab
  }

  fun toggleTemperatureUnit() {
    _isFahrenheit.value = !_isFahrenheit.value
  }

  fun refreshWeather() {
    viewModelScope.launch {
      _weatherState.value = WeatherUiState.Loading
      val result = weatherRepository.fetchWeather(_selectedCity.value)
      result.fold(
        onSuccess = { _weatherState.value = WeatherUiState.Success(it) },
        onFailure = { _weatherState.value = WeatherUiState.Error(it.message ?: "Failed to load weather") }
      )
    }
  }

  // Prayer Completion Tracking in Room
  fun togglePrayerCheck(prayerKey: String) {
    viewModelScope.launch {
      val currentDateStr = getCityZonedDateTime(_selectedCity.value).toLocalDate().toString()
      val current = todayPrayerLog.value ?: PrayerLogEntity(date = currentDateStr)
      val updated = when (prayerKey.uppercase()) {
        "FAJR" -> current.copy(fajrDone = !current.fajrDone)
        "DHUHR" -> current.copy(dhuhrDone = !current.dhuhrDone)
        "ASR" -> current.copy(asrDone = !current.asrDone)
        "MAGHRIB" -> current.copy(maghribDone = !current.maghribDone)
        "ISHA" -> current.copy(ishaDone = !current.ishaDone)
        else -> current
      }
      repository.savePrayerLog(updated)
    }
  }

  // Tasbih actions
  fun selectTasbih(id: String) {
    _selectedTasbihId.value = id
  }

  fun incrementTasbih() {
    val current = selectedTasbih.value ?: return
    viewModelScope.launch {
      val newCount = current.currentCount + 1
      repository.incrementTasbih(current.id, newCount)
      vibrateFeedback(newCount % current.targetCount == 0)
    }
  }

  fun resetTasbih() {
    val current = selectedTasbih.value ?: return
    viewModelScope.launch {
      repository.resetTasbih(current.id)
    }
  }

  private fun vibrateFeedback(isGoalReached: Boolean) {
    val context = getApplication<Application>()
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
      vibratorManager?.defaultVibrator
    } else {
      @Suppress("DEPRECATION")
      context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    vibrator?.let {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val effect = if (isGoalReached) {
          VibrationEffect.createWaveform(longArrayOf(0, 80, 80, 150), -1)
        } else {
          VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE)
        }
        it.vibrate(effect)
      } else {
        @Suppress("DEPRECATION")
        it.vibrate(if (isGoalReached) 200 else 30)
      }
    }
  }

  // Bookmark actions
  fun toggleDuaBookmark(duaId: String) {
    viewModelScope.launch {
      val isBookmarked = bookmarkedDuaIds.value.contains(duaId)
      repository.toggleBookmark(duaId, isBookmarked)
    }
  }

  fun formatTemperature(tempC: Double): String {
    return if (_isFahrenheit.value) {
      val tempF = (tempC * 9.0 / 5.0) + 32.0
      "%.0f°F".format(tempF)
    } else {
      "%.0f°C".format(tempC)
    }
  }
}