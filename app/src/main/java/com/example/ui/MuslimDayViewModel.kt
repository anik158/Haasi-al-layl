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
import com.example.data.local.MuslimDayDatabase
import com.example.data.local.PrayerLogEntity
import com.example.data.local.TasbihEntity
import com.example.data.model.CityLocation
import com.example.data.model.DailyPrayerSchedule
import com.example.data.prayer.CalculationMethod
import com.example.data.prayer.JuristicMethod
import com.example.data.prayer.PrayerCalculator
import com.example.data.qibla.QiblaCalculator
import com.example.data.qibla.QiblaSensorManager
import com.example.data.repository.MuslimDayRepository
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
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.abs

sealed interface WeatherUiState {
  object Loading : WeatherUiState
  data class Success(val data: FullWeatherData) : WeatherUiState
  data class Error(val message: String) : WeatherUiState
}

class MuslimDayViewModel(application: Application) : AndroidViewModel(application) {

  private val database = MuslimDayDatabase.getDatabase(application)
  private val repository = MuslimDayRepository(database.muslimDayDao())
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
  val juristicMethod = MutableStateFlow(JuristicMethod.STANDARD_SHAFI_MALIKI_HANBALI)

  // Temperature unit ('C' or 'F')
  private val _isFahrenheit = MutableStateFlow(false)
  val isFahrenheit: StateFlow<Boolean> = _isFahrenheit.asStateFlow()

  // Selected Bottom Nav Tab
  private val _selectedTab = MutableStateFlow(0)
  val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

  // Live Timer trigger (updates every second for real-time countdown)
  private val _secondTicker = MutableStateFlow(System.currentTimeMillis())

  // Weather state
  private val _weatherState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
  val weatherState: StateFlow<WeatherUiState> = _weatherState.asStateFlow()

  // Compass Heading
  val compassHeading: StateFlow<Float> = sensorManager.getCompassHeadingFlow()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

  // Today's Prayer log from Room
  val todayDate: LocalDate get() = LocalDate.now()
  val todayPrayerLog: StateFlow<PrayerLogEntity?> = repository.getPrayerLogForDate(todayDate)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  // Daily Prayer Schedule (Live calculated)
  val prayerSchedule: StateFlow<DailyPrayerSchedule> = combine(
    _selectedCity,
    todayPrayerLog,
    calculationMethod,
    juristicMethod,
    _secondTicker
  ) { city, log, method, juristic, _ ->
    val completedSet = mutableSetOf<String>()
    if (log != null) {
      if (log.fajrDone) completedSet.add("FAJR")
      if (log.dhuhrDone) completedSet.add("DHUHR")
      if (log.asrDone) completedSet.add("ASR")
      if (log.maghribDone) completedSet.add("MAGHRIB")
      if (log.ishaDone) completedSet.add("ISHA")
    }
    PrayerCalculator.calculateSchedule(
      date = todayDate,
      location = city,
      now = LocalDateTime.now(),
      completedPrayerIds = completedSet,
      method = method,
      juristic = juristic
    )
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    PrayerCalculator.calculateSchedule(todayDate, _selectedCity.value)
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

  init {
    startSecondTicker()
    seedTasbihIfNeeded()
    refreshWeather()
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
      val currentDateStr = todayDate.toString()
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
