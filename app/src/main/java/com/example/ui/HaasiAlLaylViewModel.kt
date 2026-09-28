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
import com.example.data.location.LocationSettingsResolver
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
import kotlinx.coroutines.withTimeoutOrNull
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
 * What the app is showing while it works out where the user is.
 *
 * Deliberately coarse; [LocatingPhase] carries the detail. Asking the user to name a city is the
 * last rung of the ladder, not the opening move, and this type exists to make that ordering hard
 * to get wrong.
 */
enum class OnboardingState {
  /** Deciding what to do; nothing on screen yet. Resolves within a frame or two. */
  RESOLVING,

  /** Working on it, with the attempt visible. See [LocatingPhase]. */
  LOCATING,

  /** Already located, or the user has been asked once and declined to be asked again. */
  NOT_NEEDED,

  /** Every automatic rung failed, so the user picks a language and then a city by hand. */
  REQUIRED
}

/**
 * The detail behind [OnboardingState.LOCATING], and what the acquisition screen renders.
 *
 * Every terminal phase here is *diagnosable*: it says which rung failed and offers the action
 * that would fix it. The earlier version silently collapsed all of these into the manual
 * language-and-city dialog, which is why a permission grant, a switched-off Location toggle and
 * a genuine indoor no-fix were indistinguishable from each other on the device.
 */
enum class LocatingPhase {
  /** Deciding, for the fraction of a second that takes. Shows the animation and nothing else. */
  STARTING,

  /** The system permission dialog is up. No counter: nothing is being measured yet. */
  REQUESTING_PERMISSION,

  /** A fix is being waited on, with the seconds counting up. */
  SEARCHING,

  /** The system Location toggle is off. No amount of waiting fixes that. */
  LOCATION_SERVICES_OFF,

  /**
   * Location is on, but the **satellite** receiver is not - the phone is positioning from Wi-Fi
   * and cell towers only, which offline produces nothing.
   *
   * Its own phase because it is the case where "turn on location" is actively misleading advice:
   * the user has already done that, and what is left to change sits under a per-manufacturer
   * positioning-mode submenu. Hence the one-tap resolution offered with it.
   */
  GPS_PROVIDER_OFF,

  /** Permission was refused, so rung 1 is unavailable until the user changes their mind. */
  PERMISSION_DENIED,

  /** Tried and failed - almost always indoors with no network. Offers a retry. */
  NO_FIX
}

/**
 * How to get the user to the switch that turns the satellite receiver on.
 *
 * [Dialog] is the one-tap system dialog from Play Services, which switches on whatever high
 * accuracy needs without any per-manufacturer settings hunt. [SettingsScreen] is the fallback for
 * devices without Play Services - and the two are kept distinct precisely so the UI never does
 * both, which is what a nullable PendingIntent led to.
 */
sealed interface PositioningRequest {
  data class Dialog(val pendingIntent: android.app.PendingIntent) : PositioningRequest
  object SettingsScreen : PositioningRequest
}

class HaasiAlLaylViewModel(application: Application) : AndroidViewModel(application) {

  private val database = HaasiAlLaylDatabase.getDatabase(application)
  private val repository = HaasiAlLaylRepository(database)
  private val weatherRepository = WeatherRepository()
  private val sensorManager = QiblaSensorManager(application)

  private val userPrefs = UserPreferences(application)

  // Selected Location - restored from disk so a returning user keeps their city. Makkah is a
  // *placeholder* when there is nothing stored, not an answer; see hasResolvedLocation.
  private val _selectedCity = MutableStateFlow(
    userPrefs.selectedCity ?: PrayerCalculator.PRESET_CITIES[0]
  )
  val selectedCity: StateFlow<CityLocation> = _selectedCity.asStateFlow()

  /**
   * Whether [selectedCity] is a real answer rather than the Makkah placeholder.
   *
   * Everything that would otherwise treat the placeholder as fact has to consult this. Two bugs
   * came straight out of not having it: the location chip announced "Makkah, Saudi Arabia" to a
   * user in Helsinki, and the interface language was derived from that country, so the first-run
   * screens came up in Arabic.
   */
  private val _hasResolvedLocation = MutableStateFlow(userPrefs.selectedCity != null)
  val hasResolvedLocation: StateFlow<Boolean> = _hasResolvedLocation.asStateFlow()

  /**
   * Where the phone physically is, as distinct from the city whose prayer times are on screen.
   *
   * These are usually the same and occasionally must not be. Prayer times legitimately follow a
   * chosen city - checking Makkah's times from Dhaka is a reasonable thing to want - but the
   * **Qibla compass cannot**. A bearing is a property of where the needle is, so computing it
   * from a chosen city points it somewhere the user is not standing, and with the Makkah
   * placeholder in play it pointed at Makkah from Makkah: a bearing of nothing, 0 km away.
   *
   * Null until a fix is seen. Populated by every rung that produces real device coordinates.
   */
  private val _deviceLocation = MutableStateFlow<CityLocation?>(null)

  /**
   * What to show while the location is worked out.
   *
   * Seeded from whether a location is *stored*, not from `isOnboardingComplete`. Those are
   * different questions, and conflating them is what left a user permanently on Makkah: they
   * dismissed the welcome dialog, which set the completion flag, and the GPS ladder was never
   * run again on any later launch.
   */
  private val _onboardingState = MutableStateFlow(
    if (userPrefs.selectedCity != null) OnboardingState.NOT_NEEDED else OnboardingState.RESOLVING
  )
  val onboardingState: StateFlow<OnboardingState> = _onboardingState.asStateFlow()

  private val _locatingPhase = MutableStateFlow(LocatingPhase.STARTING)
  val locatingPhase: StateFlow<LocatingPhase> = _locatingPhase.asStateFlow()

  // App Language (Auto location-based by default, can be overridden)
  private val _manualLanguage = MutableStateFlow<com.example.data.localization.AppLanguage?>(
    userPrefs.manualLanguage
  )
  val manualLanguage: StateFlow<com.example.data.localization.AppLanguage?> = _manualLanguage.asStateFlow()

  /**
   * The interface language: an explicit choice, else the resolved country, else the device.
   *
   * The middle rung is gated on [hasResolvedLocation] for a reason. The placeholder city is
   * Makkah, so deriving language from `_selectedCity` unconditionally meant every first run -
   * including the permission prompt and the acquisition screen - rendered in Arabic for a user
   * whose location was not yet known. The device's own locale is a far better guess than a
   * placeholder's country, and it is what the user already reads their phone in.
   */
  val currentLanguage: StateFlow<com.example.data.localization.AppLanguage> = combine(
    _selectedCity,
    _manualLanguage,
    _hasResolvedLocation
  ) { city, manual, resolved ->
    languageFor(city, manual, resolved)
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    // Seed from the restored choices, so a returning user never sees a frame in the wrong
    // language before the flow emits.
    languageFor(_selectedCity.value, _manualLanguage.value, _hasResolvedLocation.value)
  )

  /**
   * The device's own language, mapped onto the five the app speaks.
   *
   * `AppLanguage.fromCode` already falls back to English for anything unrecognised, which is
   * exactly the wanted behaviour for the roughly seven thousand languages not in that list.
   */
  private fun deviceLocaleLanguage(): com.example.data.localization.AppLanguage =
    com.example.data.localization.AppLanguage.fromCode(java.util.Locale.getDefault().language)

  private fun languageFor(
    city: CityLocation,
    manual: com.example.data.localization.AppLanguage?,
    resolved: Boolean
  ): com.example.data.localization.AppLanguage = when {
    manual != null -> manual
    resolved -> com.example.data.localization.LocalizationManager
      .detectLanguageForCountry(city.country)
    else -> deviceLocaleLanguage()
  }

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

  // Hisn al-Muslim, loaded from assets/hisn/ (generated by tools/hisn/build.py)
  private val _hisn = MutableStateFlow(com.example.data.dua.HisnCollection.EMPTY)
  val hisnChapters: StateFlow<List<com.example.data.dua.HisnChapter>> = _hisn
    .map { it.chapters }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val allDuas: StateFlow<List<com.example.data.dua.DuaItem>> = _hisn
    .map { com.example.data.dua.IslamicContentProvider.DUAS + it.duas }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.data.dua.IslamicContentProvider.DUAS)

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
   * Where the Qibla is measured from: the device's own position when it is known, otherwise the
   * selected city as the only thing left to go on.
   *
   * [qiblaUsesDeviceLocation] says which of the two it is, so the compass screen can be honest
   * about it rather than implying a precision it does not have.
   */
  val qiblaOrigin: StateFlow<CityLocation> =
    combine(_deviceLocation, _selectedCity) { device, city -> device ?: city }
      .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        _deviceLocation.value ?: _selectedCity.value
      )

  /** False when the compass is falling back to the selected city, which the screen should say. */
  val qiblaUsesDeviceLocation: StateFlow<Boolean> = _deviceLocation.map { it != null }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

  /**
   * The gap between magnetic and true north at the compass origin, from the offline World
   * Magnetic Model. Under a degree in Dhaka, but 5 to 20 degrees across the UK and North America.
   */
  val magneticDeclination: StateFlow<Float> = qiblaOrigin.map { city ->
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
  val qiblaBearing: StateFlow<Float> = qiblaOrigin.map { city ->
    QiblaCalculator.calculateQiblaBearing(city.latitude, city.longitude)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

  val distanceToKaabaKm: StateFlow<Int> = qiblaOrigin.map { city ->
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

  private var fixJob: Job? = null
  private var fixTickerJob: Job? = null
  private var locationWatchJob: Job? = null
  private var qiblaFixJob: Job? = null

  private val _placeResults = MutableStateFlow<List<GeocodingResult>>(emptyList())
  val placeResults: StateFlow<List<GeocodingResult>> = _placeResults.asStateFlow()

  private val _isSearchingPlaces = MutableStateFlow(false)
  val isSearchingPlaces: StateFlow<Boolean> = _isSearchingPlaces.asStateFlow()

  init {
    // The Tahajjud notification used to be driven from a prayerSchedule collector here, which
    // meant it could only ever fire while the app was open. PrayerAlarmScheduler owns it now,
    // along with every other alert, so it works with the app closed.
    resolveOnboarding()
    // Covers the launch where onboarding has already been dismissed once, and the one where all
    // we hold is an IP estimate: either way the app should still be listening for a real fix.
    if (!userPrefs.locationFromDevice) startBackgroundLocationWatch()
    // refreshLocationIfMoved runs from onAppResumed, which also fires on the first resume.
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

  /**
   * Loads Hisn al-Muslim from `assets/hisn/<code>.json`, one file per language.
   *
   * The files are generated by `tools/hisn/build.py`, see `assets/hisn/README.md`. If they are
   * missing the Duas screen shows the 14 built-in supplications. That is logged once rather than
   * swallowed: a quiet
   * printStackTrace here previously changed what the app shipped with nothing to show for it.
   */
  private fun loadHisnAlMuslimData() {
    viewModelScope.launch(Dispatchers.IO) {
      val assets = getApplication<Application>().assets
      val present = runCatching { assets.list(HISN_ASSET_DIR)?.toSet() }.getOrNull().orEmpty()
      val files = com.example.data.localization.AppLanguage.entries.mapNotNull { language ->
        val name = "${language.code}.json"
        if (name !in present) return@mapNotNull null
        runCatching {
          language to assets.open("$HISN_ASSET_DIR/$name").bufferedReader().use { it.readText() }
        }.onFailure { Log.e(TAG, "could not read $HISN_ASSET_DIR/$name", it) }.getOrNull()
      }.toMap()

      if (files.isEmpty()) {
        Log.w(TAG, "no Hisn al-Muslim files in assets/$HISN_ASSET_DIR; showing the built-in supplications only")
        return@launch
      }
      val collection = com.example.data.dua.HisnParser.parse(files)
      Log.i(
        TAG,
        "Hisn al-Muslim: ${collection.chapters.size} chapters, ${collection.duas.size} supplications " +
          "from ${files.keys.joinToString { it.code }}"
      )
      _hisn.value = collection
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
   * Works out where the user is, device first, and keeps trying after it gives up looking.
   *
   * The rungs: a cached fix, then a live one behind a visible wait, then IP geolocation, then
   * asking. The first two are the only ones that work with no network, which is why they come
   * first and why they get the time they need.
   *
   * Runs whenever **no location is stored**, not only on a nominal "first run". Two bugs came out
   * of gating it on a completion flag instead: the flag was set when the user merely dismissed
   * the welcome dialog, so an installation that never resolved a location stayed on the Makkah
   * placeholder forever, and no later launch - outdoors, on wifi, anywhere - ever retried.
   */
  private fun resolveOnboarding() {
    if (userPrefs.selectedCity != null) return
    viewModelScope.launch {
      // When there is a network, the IP rung answers in under a second and needs no permission
      // at all - so take it and get out of the way. Putting the satellite wait first meant an
      // online user was shown a "turn on location" screen to solve a problem the network had
      // already solved; the background watch then upgrades this to real coordinates.
      val fromIp = if (isNetworkAvailable()) fetchIpLocation() else null
      if (fromIp != null) {
        settleOn(fromIp)
        return@launch
      }
      // No network, or the IP rung failed. Now the receiver is the only answer there is, and it
      // is worth waiting for visibly.
      if (!DeviceLocationProvider.hasPermission(getApplication<Application>())) {
        _onboardingState.value = OnboardingState.LOCATING
        _locatingPhase.value = LocatingPhase.REQUESTING_PERMISSION
      } else {
        startDetection()
      }
    }
  }

  /**
   * The permission dialog's answer, reported back by the UI.
   *
   * A denial is not a dead end, but it is worth *saying* rather than silently dropping to a city
   * picker - rung 1 is the only offline one, and the user is the only one who can restore it.
   */
  fun onLocationPermissionResult(granted: Boolean) {
    userPrefs.hasAskedLocationPermission = true
    if (userPrefs.selectedCity != null) {
      // Onboarding already settled - usually on an IP estimate, which names the ISP's hub (a user
      // in Panchagarh was shown Rangpur, 80 km off, and got Rangpur's times). The watch had been
      // skipped for want of this permission, so start it now that it exists.
      if (granted) {
        startBackgroundLocationWatch()
        refreshLocationIfMoved()
      }
      return
    }
    if (granted) {
      startDetection()
    } else {
      Log.i(TAG, "location permission denied")
      _onboardingState.value = OnboardingState.LOCATING
      _locatingPhase.value = LocatingPhase.PERMISSION_DENIED
      // Still worth a try: IP geolocation needs no permission at all.
      fallBackWithoutDevice(showPhaseOnFailure = LocatingPhase.PERMISSION_DENIED)
    }
  }

  /**
   * Runs the device rungs as one cancellable job, with the wait on screen throughout.
   *
   * Cached fix first, then a live one, both inside the same visible state - the cached attempt is
   * not actually instant, because it reverse-geocodes and can spend eight seconds on an HTTP
   * timeout, and a blank screen for that long is the thing being fixed.
   */
  fun startDetection() {
    val app = getApplication<Application>()
    if (!DeviceLocationProvider.isLocationEnabled(app)) {
      // No amount of waiting fixes a system toggle that is off, and quietly falling through to a
      // city picker tells the user nothing about why. Say it, and offer the settings screen.
      Log.i(TAG, "system location is off")
      _onboardingState.value = OnboardingState.LOCATING
      _locatingPhase.value = LocatingPhase.LOCATION_SERVICES_OFF
      fallBackWithoutDevice(showPhaseOnFailure = LocatingPhase.LOCATION_SERVICES_OFF)
      return
    }
    if (!DeviceLocationProvider.isGpsProviderEnabled(app)) {
      // Location is on and GPS is not. Waiting would burn ninety seconds on a receiver that is
      // switched off and then report "no signal", which is both true and useless - the user
      // would reasonably conclude the app is broken, having already turned location on.
      Log.i(TAG, "location is on but the GPS provider is off")
      _onboardingState.value = OnboardingState.LOCATING
      _locatingPhase.value = LocatingPhase.GPS_PROVIDER_OFF
      fallBackWithoutDevice(showPhaseOnFailure = LocatingPhase.GPS_PROVIDER_OFF)
      return
    }
    _onboardingState.value = OnboardingState.LOCATING
    _locatingPhase.value = LocatingPhase.SEARCHING
    startFixTicker()
    fixJob?.cancel()
    fixJob = viewModelScope.launch {
      try {
        val cached = LocationRepository.detectFromDevice(app, allowSlowFix = false)
        if (cached != null) {
          settleOnDeviceFix(cached)
          return@launch
        }
        val live = LocationRepository.detectFromDevice(
          app,
          allowSlowFix = true,
          fixTimeoutMs = DeviceLocationProvider.COLD_FIX_TIMEOUT_MS
        )
        if (live != null) {
          settleOnDeviceFix(live)
        } else {
          Log.i(TAG, "no fix within ${DeviceLocationProvider.COLD_FIX_TIMEOUT_MS / 1000}s")
          fallBackWithoutDevice(showPhaseOnFailure = LocatingPhase.NO_FIX)
        }
      } finally {
        stopFixTicker()
      }
    }
  }

  /**
   * Gives up on the visible wait at the user's request, and goes straight to the prompts.
   *
   * Offered because a wait nobody can escape is worse than no wait at all: indoors the fix may
   * simply never arrive, and the user knows that before the timeout does. The background watch
   * keeps running regardless, so leaving here does not forfeit the GPS answer.
   */
  fun skipToManualEntry() {
    fixJob?.cancel()
    stopFixTicker()
    _onboardingState.value = OnboardingState.REQUIRED
    startBackgroundLocationWatch()
  }

  /**
   * The rungs below the device: IP geolocation, then asking.
   *
   * [showPhaseOnFailure] is the phase to sit on if the IP rung fails too - the diagnosis the
   * acquisition screen shows, with the action that would fix it. The manual prompts follow only
   * if the user has not already been through them once.
   */
  private fun fallBackWithoutDevice(showPhaseOnFailure: LocatingPhase) {
    viewModelScope.launch {
      val fromIp = if (isNetworkAvailable()) fetchIpLocation() else null
      if (fromIp != null) {
        settleOn(fromIp)
        return@launch
      }
      // Rung 1 is still the best answer available; it is only slow. Keep listening for it in the
      // background whatever the user does next, so walking outside is enough to fix this.
      startBackgroundLocationWatch()
      if (userPrefs.isOnboardingComplete) {
        // Already asked once. Do not corner them with the same dialog on every launch; the
        // location chip says it is unset, and the watch is still running.
        _onboardingState.value = OnboardingState.NOT_NEEDED
      } else {
        _onboardingState.value = OnboardingState.LOCATING
        _locatingPhase.value = showPhaseOnFailure
      }
    }
  }

  /**
   * Keeps listening for a GPS fix for as long as the app has no location of its own.
   *
   * This is the part that makes "rely on the coordinates" true rather than aspirational. A first
   * fix indoors with no network does not arrive in ninety seconds; it arrives when the user next
   * steps outside. A timeout cannot wait that long, but a listener can - so the app stops
   * *blocking* on the fix without giving up on it, and adopts it whenever it lands.
   *
   * Stops the moment a location is resolved, and never overrides a city the user chose by hand.
   */
  private fun startBackgroundLocationWatch() {
    if (userPrefs.cityChosenManually) return
    // Deliberately NOT gated on hasResolvedLocation. An IP estimate resolves the location well
    // enough to render, but it names the ISP's hub and can be 150 km out - so the watch keeps
    // running to upgrade it to real coordinates. It stops only once those have arrived, or once
    // the user picks a city by hand.
    if (userPrefs.locationFromDevice) return
    if (locationWatchJob?.isActive == true) return
    if (!DeviceLocationProvider.hasPermission(getApplication<Application>())) return
    locationWatchJob = viewModelScope.launch {
      Log.i(TAG, "watching for a GPS fix in the background")
      // Bounded: indoors with no sky the GPS would otherwise stay on for the whole session. The
      // next resume tries again. It already stops at the first fix, via settleOnDeviceFix.
      val finished = withTimeoutOrNull(WATCH_TIMEOUT_MILLIS) {
        LocationRepository.observeDeviceLocation(getApplication<Application>())
          .collect { city ->
            if (userPrefs.cityChosenManually) return@collect
            Log.i(TAG, "background fix arrived; adopting it")
            settleOnDeviceFix(city)
          }
      }
      if (finished == null) Log.i(TAG, "no GPS fix within the watch window; will retry on resume")
    }
  }

  /**
   * How the UI should get the user to a positioning switch, once asked.
   *
   * Three outcomes rather than a nullable PendingIntent, because "not asked yet" and "asked, and
   * Play Services cannot help" need different responses and a null cannot tell them apart. The
   * first version conflated them and consequently opened the settings screen *and* then threw the
   * dialog on top of it.
   */
  private val _positioningRequest = MutableStateFlow<PositioningRequest?>(null)
  val positioningRequest: StateFlow<PositioningRequest?> = _positioningRequest.asStateFlow()

  /**
   * Works out the best available route to the positioning switches.
   *
   * Emitted rather than acted on, because only an Activity can launch an IntentSender.
   */
  fun requestPositioningFix() {
    viewModelScope.launch {
      val resolution =
        LocationSettingsResolver.resolutionForHighAccuracy(getApplication<Application>())
      _positioningRequest.value = if (resolution != null) {
        PositioningRequest.Dialog(resolution)
      } else {
        // Either the settings are already satisfied or Play Services is unavailable. The user is
        // looking at a screen that says positioning is off, so send them somewhere either way.
        PositioningRequest.SettingsScreen
      }
    }
  }

  /** Consumed by the UI once it has acted, so the same request is not handled twice. */
  fun clearPositioningRequest() {
    _positioningRequest.value = null
  }

  /**
   * Called when the user comes back from the positioning dialog or the settings screen.
   *
   * Re-reads the switches rather than trusting the dialog's result code: the user may have
   * changed things by hand, or changed their mind halfway.
   */
  fun onReturnedFromLocationSettings() {
    if (userPrefs.selectedCity != null && userPrefs.locationFromDevice) return
    val app = getApplication<Application>()
    if (DeviceLocationProvider.hasPermission(app) &&
      DeviceLocationProvider.isLocationEnabled(app) &&
      DeviceLocationProvider.isGpsProviderEnabled(app)
    ) {
      startDetection()
    }
  }

  private fun stopBackgroundLocationWatch() {
    locationWatchJob?.cancel()
    locationWatchJob = null
  }

  /**
   * Re-checks a stored location on launch, in case the user is no longer where it was taken.
   *
   * Nothing did this before, and the gap was total: `resolveOnboarding` returns immediately when
   * a city is stored and the background watch only runs while one is missing, so the very first
   * location an installation resolved was the last one it ever had. Someone who set the app up in
   * Dhaka and then moved kept Dhaka's prayer times indefinitely - with no network, no prompt and
   * nothing on screen suggesting anything was wrong.
   *
   * Cheap enough to run on every launch: `lastKnownFix` reads the fix the system already has,
   * waking no radio and needing no network, so the offline case is covered as well as the online
   * one. If it is far from where the app thinks it is, that is adopted; if there is no cached fix
   * at all but the stored one has gone stale, the watch is restarted so the next real fix lands.
   *
   * A city the user chose by hand is never overridden - they may well be checking another city's
   * times deliberately.
   */
  /**
   * Called on every resume, including the first. The re-check is cheap - it reads the fix the
   * system already holds - and running it only on a cold start meant a phone that kept the app
   * in memory for days never noticed it had moved.
   */
  fun onAppResumed() {
    refreshLocationIfMoved()
  }

  private fun refreshLocationIfMoved() {
    val stored = userPrefs.selectedCity ?: return
    if (userPrefs.cityChosenManually) return
    val app = getApplication<Application>()
    if (!DeviceLocationProvider.hasPermission(app)) return

    // A timezone change is its own kind of move, and a bigger one for prayer times than distance.
    // The zone is captured from the system at fix time and then frozen, so flying to another
    // country left every time computed in the zone left behind.
    val systemZone = java.time.ZoneId.systemDefault().id
    if (userPrefs.locationFromDevice && stored.timeZoneId != systemZone) {
      Log.i(TAG, "system timezone changed: ${stored.timeZoneId} -> $systemZone")
      applyCity(stored.copy(timeZoneId = systemZone))
    }

    viewModelScope.launch {
      val fix = withContext(Dispatchers.IO) { DeviceLocationProvider.lastKnownFix(app) }
      if (fix == null) {
        // Nothing cached to compare against. Listen for the next fix - whenever the user next has
        // a view of the sky - if what we hold is only an IP estimate, or is old.
        if (!userPrefs.locationFromDevice || isLocationStale()) startBackgroundLocationWatch()
        return@launch
      }
      val current = _selectedCity.value
      val movedKm = QiblaCalculator.distanceKm(
        current.latitude, current.longitude, fix.latitude, fix.longitude
      )
      // An IP estimate is replaced by any real fix, however close: its coordinates are the ISP's,
      // so "under 25 km" says nothing about how wrong it is.
      if (movedKm >= MOVED_THRESHOLD_KM || !userPrefs.locationFromDevice) {
        Log.i(TAG, "moved %.0f km since the stored fix; adopting the new one".format(movedKm))
        val named = LocationRepository.reverseGeocode(app, fix.latitude, fix.longitude)
        settleOnDeviceFix(
          CityLocation(
            name = named?.first ?: LocationRepository.formatCoordinates(fix.latitude, fix.longitude),
            country = named?.second.orEmpty(),
            latitude = fix.latitude,
            longitude = fix.longitude,
            timeZoneId = systemZone
          )
        )
      } else if (isLocationStale()) {
        // Same place, but the fix on file is ancient - refresh the timestamp by adopting the
        // cached one, so staleness does not accumulate forever in one spot.
        userPrefs.locationUpdatedAt = System.currentTimeMillis()
      }
    }
  }

  private fun isLocationStale(): Boolean {
    val updatedAt = userPrefs.locationUpdatedAt
    if (updatedAt == 0L) return true
    return System.currentTimeMillis() - updatedAt >= STALE_LOCATION_MILLIS
  }

  /**
   * Accepts an **IP** estimate as the working answer.
   *
   * Good enough to stop blocking on - the app is usable, prayer times are within a few minutes -
   * but not good enough to be final, so the background watch is left running to replace it with
   * real coordinates, and [UserPreferences.locationFromDevice] stays false to say so.
   */
  private fun settleOn(city: CityLocation) {
    userPrefs.locationFromDevice = false
    applyCity(city)
    userPrefs.isOnboardingComplete = true
    _onboardingState.value = OnboardingState.NOT_NEEDED
    startBackgroundLocationWatch()
  }

  /**
   * Records the device's own coordinates, which the Qibla compass measures from.
   *
   * Only ever called with coordinates that came off the receiver - never with an IP estimate,
   * which names the ISP's hub and can be 150 km out, and never with a city the user typed. A
   * compass is the one place where a rough answer is worse than an honest fallback.
   */
  private fun rememberDeviceLocation(city: CityLocation) {
    _deviceLocation.value = city
  }

  /**
   * Refreshes the device fix for the compass, without disturbing the selected city.
   *
   * Called when the Qibla screen opens: the bearing should be measured from where the user is
   * standing now, and a fix from an hour and one bus ride ago is not that.
   */
  fun refreshDeviceLocationForQibla() {
    if (!DeviceLocationProvider.hasPermission(getApplication<Application>())) return
    // One at a time. This waits up to ninety seconds, and the screen it serves is a tab the user
    // can leave and re-enter freely - without the guard, a few taps would stack up several
    // concurrent fix requests all waiting on the same receiver.
    if (qiblaFixJob?.isActive == true) return
    qiblaFixJob = viewModelScope.launch {
      LocationRepository.detectFromDevice(
        getApplication<Application>(),
        allowSlowFix = true,
        fixTimeoutMs = DeviceLocationProvider.COLD_FIX_TIMEOUT_MS
      )?.let { rememberDeviceLocation(it) }
    }
  }

  /** Accepts real receiver coordinates: the top of the ladder, and the end of the search. */
  private fun settleOnDeviceFix(city: CityLocation) {
    userPrefs.locationFromDevice = true
    rememberDeviceLocation(city)
    applyCity(city)
    userPrefs.isOnboardingComplete = true
    _onboardingState.value = OnboardingState.NOT_NEEDED
    stopBackgroundLocationWatch()
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

  /**
   * The user dismissed the welcome dialog. It records only that they have been asked - never
   * that a location was found.
   *
   * Those two were the same flag once, and the cost was severe: dismissing this dialog made the
   * app believe it knew where the user was, so it rendered "Makkah, Saudi Arabia" as fact,
   * derived an Arabic interface from it, and never ran the GPS ladder again on any later launch.
   * The background watch is left running, so stepping outside still fixes it.
   */
  fun markOnboardingComplete() {
    userPrefs.isOnboardingComplete = true
    _onboardingState.value = OnboardingState.NOT_NEEDED
    startBackgroundLocationWatch()
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

  /**
   * The "detect" button in the city picker. Same rungs, but driven by an explicit tap, so it
   * reports progress through [isDetectingLocation] rather than taking the screen over.
   */
  fun autoDetectLocation() {
    userPrefs.cityChosenManually = false
    fixJob?.cancel()
    fixJob = viewModelScope.launch {
      _isDetectingLocation.value = true
      startFixTicker()
      try {
        // Device coordinates are exact and work offline; the IP only ever names the ISP's hub,
        // which can be a hundred kilometres out, so it is the fallback rather than the source.
        // The full cold-fix budget applies here too: the user asked for this and can see the
        // counter, and twenty seconds gives up mid-ephemeris-download.
        //
        // The two rungs stay distinct rather than collapsing into one nullable: only the first
        // produces coordinates good enough to point a compass with.
        val fromDevice = LocationRepository.detectFromDevice(
          getApplication<Application>(),
          allowSlowFix = true,
          fixTimeoutMs = DeviceLocationProvider.COLD_FIX_TIMEOUT_MS
        )
        when {
          fromDevice != null -> settleOnDeviceFix(fromDevice)
          else -> {
            val fromIp = fetchIpLocation()
            if (fromIp != null) {
              settleOn(fromIp)
            } else {
              // Nothing to show for it, but the watch will take the fix whenever it lands.
              startBackgroundLocationWatch()
            }
          }
        }
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
    // Writing the city is what turns the Makkah placeholder into a real answer, so this is the
    // one place that may set the flag - and the one place that can honestly say the location was
    // confirmed just now, which is what the staleness check reads.
    _hasResolvedLocation.value = true
    userPrefs.locationUpdatedAt = System.currentTimeMillis()
    rescheduleAlarms()
    // A fix taken offline arrives named after its own coordinates. Try to name it here rather
    // than only for the city restored at startup, so a detection that lands while the network
    // is up gets a real place name immediately instead of waiting for the next launch. No
    // recursion: a successful lookup yields a city with a country, which is not coordinates-only.
    nameCurrentCityIfNeeded()
  }

  /**
   * The user picked a city. That is authoritative: it stops the background GPS watch, so a fix
   * arriving later never silently overwrites a deliberate choice.
   */
  fun selectCity(city: CityLocation) {
    userPrefs.cityChosenManually = true
    // No refreshWeather() here: applyCity moves _selectedCity, and observeCityForWeather is
    // watching it. Calling both raced two requests for the same forecast.
    applyCity(city)
    stopBackgroundLocationWatch()
    userPrefs.isOnboardingComplete = true
    _onboardingState.value = OnboardingState.NOT_NEEDED
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
     * How far the user has to have moved before the stored location is replaced.
     *
     * Prayer times move with longitude at roughly four minutes per degree, so 25 km - about a
     * quarter of a degree at these latitudes - is a minute or so of drift: the point where a
     * different answer is worth the cost of re-arming every alarm. Smaller thresholds would
     * re-arm the schedule for someone crossing their own city.
     */
    const val MOVED_THRESHOLD_KM = 25.0
    const val WATCH_TIMEOUT_MILLIS = 2L * 60L * 1000L

    /** After this long without confirmation, a stored fix is worth re-establishing. */
    const val STALE_LOCATION_MILLIS = 7L * 24 * 60 * 60 * 1000

    /**
     * Consumer magnetometers are good to roughly 5-10 degrees. The old 4-degree gate was
     * narrower than that, and narrower than the declination error it was masking, so it
     * essentially never latched.
     */
    const val HISN_ASSET_DIR = "hisn"
    const val QIBLA_FACING_TOLERANCE_DEGREES = 7.5f
  }

}