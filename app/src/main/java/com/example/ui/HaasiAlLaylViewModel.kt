package com.example.ui

import android.app.Application
import android.hardware.GeomagneticField
import android.hardware.SensorManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.dua.IslamicContentProvider
import com.example.data.location.DeviceLocationProvider
import com.example.data.location.GeocodingResult
import com.example.data.location.LocationRepository
import com.example.data.local.HaasiAlLaylDatabase
import com.example.data.local.PrayerLogEntity
import com.example.data.local.TasbihEntity
import com.example.data.model.CityLocation
import com.example.data.model.DailyPrayerSchedule
import com.example.data.prayer.CalculationMethod
import com.example.data.prayer.JuristicMethod
import com.example.data.preferences.UserPreferences
import com.example.data.prayer.PrayerCalculator
import com.example.data.qibla.CompassReading
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
import kotlinx.coroutines.flow.collectLatest
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

import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
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

/**
 * Whether the first-run prompts are needed. The app asks only when it cannot work the answers
 * out for itself, and only once per installation.
 *
 * The order matters: the device's own coordinates are tried *first* and given a visible wait,
 * because they are exact and work with no network. Asking the user to name a city is the last
 * resort, not the opening move.
 */
enum class OnboardingState {
  /** Deciding what to do; nothing on screen yet. Resolves within a frame or two. */
  RESOLVING,

  /**
   * The UI should ask for location permission. A fresh install has never been asked, and
   * without it [com.example.data.location.DeviceLocationProvider.lastKnownFix] returns null
   * before it even reaches the receiver - which used to send every offline first run straight
   * to the manual language-and-city dialog.
   */
  NEEDS_LOCATION_PERMISSION,

  /** Waiting on the satellites, with the wait shown rather than hidden. */
  ACQUIRING_FIX,

  /** Already onboarded, or location and language were worked out without asking. */
  NOT_NEEDED,

  /** Every automatic rung failed, so the user picks a language and then a city by hand. */
  REQUIRED
}

class HaasiAlLaylViewModel(application: Application) : AndroidViewModel(application) {

  private val database = HaasiAlLaylDatabase.getDatabase(application)
  private val repository = HaasiAlLaylRepository(database)
  private val weatherRepository = WeatherRepository()
  private val sensorManager = QiblaSensorManager(application)

  private val userPrefs = UserPreferences(application)

  // Selected Location - restored from disk so a returning user keeps their city.
  private val _selectedCity = MutableStateFlow(
    userPrefs.selectedCity ?: PrayerCalculator.PRESET_CITIES[0] // Default to Makkah
  )
  val selectedCity: StateFlow<CityLocation> = _selectedCity.asStateFlow()

  /**
   * Whether the first-run language/location prompts still need to be shown. Resolves to
   * [OnboardingState.RESOLVING] first so no dialog flashes while auto-detection is in flight.
   */
  private val _onboardingState = MutableStateFlow(
    if (userPrefs.isOnboardingComplete) OnboardingState.NOT_NEEDED else OnboardingState.RESOLVING
  )
  val onboardingState: StateFlow<OnboardingState> = _onboardingState.asStateFlow()

  // App Language (Auto location-based by default, can be overridden)
  private val _manualLanguage = MutableStateFlow<com.example.data.localization.AppLanguage?>(
    userPrefs.manualLanguage
  )
  val manualLanguage: StateFlow<com.example.data.localization.AppLanguage?> = _manualLanguage.asStateFlow()

  val currentLanguage: StateFlow<com.example.data.localization.AppLanguage> = combine(
    _selectedCity,
    _manualLanguage
  ) { city, manual ->
    manual ?: com.example.data.localization.LocalizationManager.detectLanguageForCountry(city.country)
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    // Seed from the restored choices, so a returning user never sees a frame in the wrong
    // language before the flow emits.
    _manualLanguage.value
      ?: com.example.data.localization.LocalizationManager
        .detectLanguageForCountry(_selectedCity.value.country)
  )

  // Calculation Settings
  // Private-backed so nothing outside this class can change a setting without writing it through.
  private val _calculationMethod = MutableStateFlow(userPrefs.calculationMethod)
  val selectedCalculationMethod: StateFlow<CalculationMethod> = _calculationMethod.asStateFlow()
  private val _juristicMethod = MutableStateFlow(userPrefs.madhab)
  val selectedMadhab: StateFlow<com.example.data.prayer.IslamicMadhab> = _juristicMethod.asStateFlow()

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
  private val compassReading: StateFlow<CompassReading> = sensorManager.getCompassReadingFlow()
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      CompassReading(0f, SensorManager.SENSOR_STATUS_UNRELIABLE)
    )

  /**
   * The gap between magnetic and true north at the selected city, from the offline World Magnetic
   * Model. Under a degree in Dhaka, but 5 to 20 degrees across the UK and North America.
   */
  val magneticDeclination: StateFlow<Float> = _selectedCity.map { city ->
    GeomagneticField(
      city.latitude.toFloat(),
      city.longitude.toFloat(),
      0f,
      System.currentTimeMillis()
    ).declination
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

  /**
   * Heading clockwise from **true** north.
   *
   * The sensors report magnetic north while [qiblaBearing] is a true great-circle bearing, so
   * subtracting one from the other left the needle wrong by the local declination. Correcting
   * here keeps both sides of that subtraction in the same reference frame.
   */
  val compassHeading: StateFlow<Float> =
    combine(compassReading, magneticDeclination) { reading, declination ->
      (reading.magneticAzimuth + declination + 360f) % 360f
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

  /** True while the magnetometer is uncalibrated, so the figure-of-eight hint means something. */
  val needsCompassCalibration: StateFlow<Boolean> = compassReading.map { reading ->
    reading.accuracy <= SensorManager.SENSOR_STATUS_ACCURACY_LOW
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

  // Get current ZonedDateTime for a given city
  fun getCityZonedDateTime(city: CityLocation): ZonedDateTime {
    val zone = try {
      ZoneId.of(city.timeZoneId)
    } catch (e: Exception) {
      ZoneId.systemDefault()
    }
    return ZonedDateTime.now(zone)
  }

  /**
   * The selected city's current local date, re-emitting only when the date actually rolls over.
   *
   * The reader and the writer have to agree on this. Deriving it once per city emission left the
   * query watching yesterday while a tap wrote to today, so across local midnight the circles
   * stopped responding.
   */
  private val currentCityDate: StateFlow<LocalDate> = combine(_selectedCity, _secondTicker) { city, _ ->
    getCityZonedDateTime(city).toLocalDate()
  }.distinctUntilChanged()
    .stateIn(
      viewModelScope,
      SharingStarted.Eagerly,
      getCityZonedDateTime(_selectedCity.value).toLocalDate()
    )

  // Today's Prayer log from Room for the selected city's local date
  val todayPrayerLog: StateFlow<PrayerLogEntity?> = currentCityDate.flatMapLatest { date ->
    repository.getPrayerLogForDate(date)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  /**
   * Today's ayah and hadith, for the selected city's local date.
   *
   * Keyed on [currentCityDate] rather than the system date so the card turns over at the same
   * midnight the prayer log does - and so it turns over at all while the app is open, which a
   * value read once at construction would not.
   */
  val dailyInspiration: StateFlow<com.example.data.dua.DailyInspiration> = currentCityDate
    .map { date -> com.example.data.dua.DailyInspirationData.forDate(date) }
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      com.example.data.dua.DailyInspirationData.forDate(
        getCityZonedDateTime(_selectedCity.value).toLocalDate()
      )
    )

  // Daily Prayer Schedule (Live calculated using city's local timezone)
  val prayerSchedule: StateFlow<DailyPrayerSchedule> = combine(
    _selectedCity,
    todayPrayerLog,
    _calculationMethod,
    _juristicMethod,
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
      method = _calculationMethod.value,
      madhab = _juristicMethod.value
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
    diff < QIBLA_FACING_TOLERANCE_DEGREES
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

  // Tasbih list from Room
  val tasbihList: StateFlow<List<TasbihEntity>> = repository.getAllTasbihItems()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _selectedTasbihId = MutableStateFlow(userPrefs.lastTasbihId ?: "subhanallah")
  val selectedTasbih: StateFlow<TasbihEntity?> = combine(tasbihList, _selectedTasbihId) { list, id ->
    list.find { it.id == id } ?: list.firstOrNull()
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  // Du'a Bookmarks from Room
  val bookmarkedDuaIds: StateFlow<Set<String>> = repository.getAllBookmarks()
    .map { list -> list.map { it.duaId }.toSet() }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

  private var tickerJob: Job? = null
  private var placeSearchJob: Job? = null

  private val _isDetectingLocation = MutableStateFlow(false)
  val isDetectingLocation: StateFlow<Boolean> = _isDetectingLocation.asStateFlow()

  /**
   * Seconds spent waiting on the current fix.
   *
   * A cold GPS fix reads the ephemeris off the satellites at 50 bits per second and can take a
   * minute, so a bare spinner reads as a hang. This is the number the acquisition screen counts
   * up, and the reason the wait is tolerable.
   */
  private val _fixElapsedSeconds = MutableStateFlow(0)
  val fixElapsedSeconds: StateFlow<Int> = _fixElapsedSeconds.asStateFlow()

  /** How long the counter runs before the manual prompts take over. */
  val fixTimeoutSeconds: Int = (DeviceLocationProvider.COLD_FIX_TIMEOUT_MS / 1000L).toInt()

  /** True when the system Location toggle is off, which no amount of waiting will fix. */
  private val _locationServicesOff = MutableStateFlow(false)
  val locationServicesOff: StateFlow<Boolean> = _locationServicesOff.asStateFlow()

  private var fixJob: Job? = null
  private var fixTickerJob: Job? = null

  private val _placeResults = MutableStateFlow<List<GeocodingResult>>(emptyList())
  val placeResults: StateFlow<List<GeocodingResult>> = _placeResults.asStateFlow()

  private val _isSearchingPlaces = MutableStateFlow(false)
  val isSearchingPlaces: StateFlow<Boolean> = _isSearchingPlaces.asStateFlow()

  init {
    // The Tahajjud notification used to be driven from a prayerSchedule collector here, which
    // meant it could only ever fire while the app was open. PrayerAlarmScheduler owns it now,
    // along with every other alert, so it works with the app closed.
    resolveOnboarding()
    nameCurrentCityIfNeeded()
    startSecondTicker()
    seedTasbih()
    observeCityForWeather()
    loadHisnAlMuslimData()
  }

  /**
   * Keeps the weather pointed at the selected city.
   *
   * This replaces a single `refreshWeather()` in `init`, which fired once against whatever city
   * was restored from disk - Makkah on a fresh install - and was never re-run when detection
   * landed a moment later. The forecast then stayed Makkah's, under the right city's name, until
   * the user pulled to refresh.
   *
   * Keyed on coordinates rather than the whole city so that naming a coordinates-only fix, which
   * rewrites `name` and `country` and nothing else, does not spend a second request on an
   * identical forecast.
   */
  private fun observeCityForWeather() {
    viewModelScope.launch {
      _selectedCity
        .distinctUntilChangedBy { city -> city.latitude to city.longitude }
        .collectLatest { city -> loadWeatherFor(city) }
    }
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

  private fun seedTasbih() {
    viewModelScope.launch {
      repository.seedDefaultTasbih()
    }
  }

  fun setTab(index: Int) {
    _selectedTab.value = index
  }


  /**
   * Resolves the first run, preferring the device's own coordinates over everything else.
   *
   * The rungs, in order: a cached fix (instant), then a live one behind a visible wait, then IP
   * geolocation, then the manual prompts. The first two are the ones that work with no network,
   * which is the whole reason they come first.
   *
   * This used to start at the cached fix and give up immediately if there was not one - but on a
   * fresh install `ACCESS_FINE_LOCATION` had never been requested, so the cached read returned
   * null without ever consulting the receiver. Offline, that meant every first launch fell
   * through to the manual language-and-city dialog, which is exactly what the GPS path exists to
   * avoid. Asking for the permission is therefore the first thing that happens, not something
   * deferred until the user finds the "detect" button.
   */
  private fun resolveOnboarding() {
    if (userPrefs.isOnboardingComplete) return
    if (!DeviceLocationProvider.hasPermission(getApplication<Application>())) {
      _onboardingState.value = OnboardingState.NEEDS_LOCATION_PERMISSION
      return
    }
    startDetection()
  }

  /**
   * The permission dialog's answer, reported back by the UI.
   *
   * A denial is not a dead end - it drops to IP geolocation, and then to the manual prompts.
   */
  fun onLocationPermissionResult(granted: Boolean) {
    userPrefs.hasAskedLocationPermission = true
    if (userPrefs.isOnboardingComplete) return
    if (granted) {
      startDetection()
    } else {
      Log.i(TAG, "location permission denied; falling back to IP geolocation")
      fallBackWithoutDevice()
    }
  }

  /**
   * Runs the whole device ladder as one cancellable job, with the wait on screen throughout.
   *
   * Cached fix first, then a live one. Both are inside the same job and the same visible state,
   * because the cached attempt is not actually instant - it reverse-geocodes, which can spend
   * eight seconds on an HTTP timeout - and a blank screen for that long is the thing being fixed.
   *
   * Nothing here is hidden: a cold fix genuinely takes up to a minute and may never arrive
   * indoors, so the honest thing is to show the attempt, show how long it has been going, and
   * offer a way out of it.
   */
  private fun startDetection() {
    if (!DeviceLocationProvider.isLocationEnabled(getApplication<Application>())) {
      // No amount of waiting fixes a system toggle that is off.
      _locationServicesOff.value = true
      Log.i(TAG, "system location is off; skipping the satellite wait")
      fallBackWithoutDevice()
      return
    }
    _locationServicesOff.value = false
    _onboardingState.value = OnboardingState.ACQUIRING_FIX
    startFixTicker()
    fixJob?.cancel()
    fixJob = viewModelScope.launch {
      try {
        val app = getApplication<Application>()
        val cached = LocationRepository.detectFromDevice(app, allowSlowFix = false)
        if (cached != null) {
          settleOn(cached)
          return@launch
        }
        val live = LocationRepository.detectFromDevice(
          app,
          allowSlowFix = true,
          fixTimeoutMs = DeviceLocationProvider.COLD_FIX_TIMEOUT_MS
        )
        if (live != null) settleOn(live) else fallBackWithoutDevice()
      } finally {
        stopFixTicker()
      }
    }
  }

  /**
   * Abandons the satellite wait at the user's request.
   *
   * Offered because a wait nobody can escape is worse than no wait at all - indoors the fix may
   * simply never arrive, and the user knows that before the timeout does.
   */
  fun skipFixAcquisition() {
    fixJob?.cancel()
    stopFixTicker()
    fallBackWithoutDevice()
  }

  /** The rungs below the device: IP geolocation, then asking. */
  private fun fallBackWithoutDevice() {
    viewModelScope.launch {
      val fromIp = if (isNetworkAvailable()) fetchIpLocation() else null
      if (fromIp != null) {
        settleOn(fromIp)
      } else {
        _onboardingState.value = OnboardingState.REQUIRED
      }
    }
  }

  /** Accepts a detected location as the answer to the first run. */
  private fun settleOn(city: CityLocation) {
    applyCity(city)
    markOnboardingComplete()
  }

  private fun startFixTicker() {
    fixTickerJob?.cancel()
    _fixElapsedSeconds.value = 0
    fixTickerJob = viewModelScope.launch {
      while (isActive) {
        delay(1000)
        _fixElapsedSeconds.value += 1
      }
    }
  }

  private fun stopFixTicker() {
    fixTickerJob?.cancel()
    fixTickerJob = null
  }

  /** Marks the first run settled so the prompts never reappear for this installation. */
  fun markOnboardingComplete() {
    userPrefs.isOnboardingComplete = true
    _onboardingState.value = OnboardingState.NOT_NEEDED
  }

  private fun isNetworkAvailable(): Boolean {
    val manager = getApplication<Application>()
      .getSystemService(ConnectivityManager::class.java) ?: return false
    val capabilities = manager.getNetworkCapabilities(manager.activeNetwork) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
      capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
  }

  private suspend fun fetchIpLocation(): CityLocation? = withContext(Dispatchers.IO) {
    try {
      val url = URL("https://ipinfo.io/json")
      val connection = url.openConnection() as java.net.HttpURLConnection
      connection.requestMethod = "GET"
      connection.setRequestProperty("User-Agent", "Mozilla/5.0")
      connection.connectTimeout = 5000
      connection.readTimeout = 5000

      val body = if (connection.responseCode == 200) {
        connection.inputStream.bufferedReader().use { it.readText() }
      } else {
        null
      } ?: return@withContext null

      val json = org.json.JSONObject(body)
      val city = json.optString("city", "").ifBlank { return@withContext null }
      // ipinfo reports the country as an ISO 3166-1 alpha-2 code; expand it so the UI reads
      // "Dhaka, Bangladesh" rather than "Dhaka, BD".
      val countryCode = json.optString("country", "")
      val country = if (countryCode.length == 2) {
        java.util.Locale("", countryCode).getDisplayCountry(java.util.Locale.ENGLISH)
          .ifBlank { countryCode }
      } else {
        countryCode
      }
      val loc = json.optString("loc", "0.0,0.0").split(",")
      val lat = loc.getOrNull(0)?.toDoubleOrNull() ?: return@withContext null
      val lon = loc.getOrNull(1)?.toDoubleOrNull() ?: return@withContext null
      val timezone = json.optString("timezone", "").ifBlank { "UTC" }

      CityLocation(city, country, lat, lon, timezone)
    } catch (e: Exception) {
      Log.e("HaasiAlLaylViewModel", "Error auto-detecting location", e)
      null
    }
  }

  fun autoDetectLocation() {
    fixJob?.cancel()
    fixJob = viewModelScope.launch {
      _isDetectingLocation.value = true
      startFixTicker()
      try {
        // Device coordinates are exact and work offline; the IP only ever names the ISP's hub,
        // which can be a hundred kilometres out, so it is the fallback rather than the source.
        // The full cold-fix budget applies here too: the user asked for this and can see the
        // counter, and twenty seconds gives up mid-ephemeris-download.
        val detected = LocationRepository.detectFromDevice(
          getApplication<Application>(),
          allowSlowFix = true,
          fixTimeoutMs = DeviceLocationProvider.COLD_FIX_TIMEOUT_MS
        ) ?: fetchIpLocation()
        detected?.let { applyCity(it) }
      } finally {
        stopFixTicker()
        _isDetectingLocation.value = false
      }
    }
  }

  /**
   * Puts a name to a location that was detected offline.
   *
   * A fix taken with no network stores coordinates as its name. Once there is a network, this
   * fills in the real place name without touching the coordinates the times are computed from.
   */
  private fun nameCurrentCityIfNeeded() {
    val city = _selectedCity.value
    if (!city.isCoordinatesOnly || !isNetworkAvailable()) return
    viewModelScope.launch {
      val named = LocationRepository.reverseGeocode(
        getApplication<Application>(),
        city.latitude,
        city.longitude
      ) ?: return@launch
      // Guard against the city having changed while the lookup was in flight.
      if (_selectedCity.value == city) {
        applyCity(city.copy(name = named.first, country = named.second))
      }
    }
  }

  /** Free-text place search for the city picker, so it is not limited to the presets. */
  fun searchPlaces(query: String) {
    placeSearchJob?.cancel()
    if (query.isBlank()) {
      _placeResults.value = emptyList()
      _isSearchingPlaces.value = false
      return
    }
    placeSearchJob = viewModelScope.launch {
      _isSearchingPlaces.value = true
      // Debounce, so a lookup does not fire on every keystroke.
      kotlinx.coroutines.delay(350)
      _placeResults.value = LocationRepository.searchPlaces(query)
      _isSearchingPlaces.value = false
    }
  }

  /** Applies a city and writes it through, so it survives the next launch. */
  private fun applyCity(city: CityLocation) {
    _selectedCity.value = city
    userPrefs.selectedCity = city
    rescheduleAlarms()
    // A fix taken offline arrives named after its own coordinates. Try to name it here rather
    // than only for the city restored at startup, so a detection that lands while the network
    // is up gets a real place name immediately instead of waiting for the next launch. No
    // recursion: a successful lookup yields a city with a country, which is not coordinates-only.
    nameCurrentCityIfNeeded()
  }

  fun selectCity(city: CityLocation) {
    // No refreshWeather() here: applyCity moves _selectedCity, and observeCityForWeather is
    // watching it. Calling both raced two requests for the same forecast.
    applyCity(city)
  }

  fun setLanguage(language: com.example.data.localization.AppLanguage?) {
    _manualLanguage.value = language
    userPrefs.manualLanguage = language
  }

  fun resetLanguageToLocation() {
    _manualLanguage.value = null
    userPrefs.manualLanguage = null
  }

  fun setMadhab(madhab: com.example.data.prayer.IslamicMadhab) {
    _juristicMethod.value = madhab
    userPrefs.madhab = madhab
    rescheduleAlarms()
  }

  fun setCalculationMethod(method: CalculationMethod) {
    _calculationMethod.value = method
    userPrefs.calculationMethod = method
    rescheduleAlarms()
  }

  /**
   * Re-arms the alarm schedule after a change that moves prayer times.
   *
   * Deliberately hooked to the setters rather than to prayerSchedule: that flow is driven by the
   * one-second ticker and would re-arm roughly thirty alarms every second the app is open.
   */
  fun rescheduleAlarms() {
    viewModelScope.launch(Dispatchers.IO) {
      com.example.data.notification.PrayerAlarmScheduler.rescheduleAll(
        getApplication<Application>()
      )
    }
  }

  fun toggleTemperatureUnit() {
    _isFahrenheit.value = !_isFahrenheit.value
  }

  /** Pull-to-refresh and the refresh button. The city collector handles every other case. */
  fun refreshWeather() {
    viewModelScope.launch { loadWeatherFor(_selectedCity.value) }
  }

  private suspend fun loadWeatherFor(city: CityLocation) {
    _weatherState.value = WeatherUiState.Loading
    val result = weatherRepository.fetchWeather(city)
    result.fold(
      onSuccess = { _weatherState.value = WeatherUiState.Success(it) },
      onFailure = {
        _weatherState.value = WeatherUiState.Error(it.message ?: "Failed to load weather")
      }
    )
  }

  // Prayer Completion Tracking in Room
  fun togglePrayerCheck(prayerKey: String) {
    viewModelScope.launch {
      repository.togglePrayerCompletion(currentCityDate.value, prayerKey)
    }
  }

  // Tasbih actions
  fun selectTasbih(id: String) {
    _selectedTasbihId.value = id
    userPrefs.lastTasbihId = id
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
  private companion object {
    const val TAG = "HaasiAlLaylViewModel"

    /**
     * Consumer magnetometers are good to roughly 5-10 degrees. The old 4-degree gate was
     * narrower than that, and narrower than the declination error it was masking, so it
     * essentially never latched.
     */
    const val QIBLA_FACING_TOLERANCE_DEGREES = 7.5f
  }

}