package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.data.localization.AppLanguage
import com.example.data.model.CityLocation

/**
 * User choices that must survive leaving the app.
 *
 * Everything here is written to SharedPreferences, so it lives for the whole installation
 * lifecycle - across process death, restarts and reboots - and is only cleared when the user
 * uninstalls or clears app data. Onboarding asks once, not on every launch.
 */
class UserPreferences(context: Context) {

  private val prefs: SharedPreferences =
    context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  /** True once the user has a language and a location, whether auto-detected or chosen by hand. */
  var isOnboardingComplete: Boolean
    get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false)
    set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETE, value).apply()

  /** An explicit language choice. Null means "follow the location". */
  var manualLanguage: AppLanguage?
    get() = prefs.getString(KEY_MANUAL_LANGUAGE, null)?.let { AppLanguage.fromCode(it) }
    set(value) = prefs.edit().run {
      if (value == null) remove(KEY_MANUAL_LANGUAGE) else putString(KEY_MANUAL_LANGUAGE, value.code)
      apply()
    }

  var selectedCity: CityLocation?
    get() {
      val name = prefs.getString(KEY_CITY_NAME, null) ?: return null
      val country = prefs.getString(KEY_CITY_COUNTRY, null) ?: return null
      val timeZoneId = prefs.getString(KEY_CITY_TZ, null) ?: return null
      if (!prefs.contains(KEY_CITY_LAT) || !prefs.contains(KEY_CITY_LON)) return null
      return CityLocation(
        name = name,
        country = country,
        latitude = Double.fromBits(prefs.getLong(KEY_CITY_LAT, 0L)),
        longitude = Double.fromBits(prefs.getLong(KEY_CITY_LON, 0L)),
        timeZoneId = timeZoneId
      )
    }
    set(value) = prefs.edit().run {
      if (value == null) {
        remove(KEY_CITY_NAME); remove(KEY_CITY_COUNTRY)
        remove(KEY_CITY_LAT); remove(KEY_CITY_LON); remove(KEY_CITY_TZ)
      } else {
        putString(KEY_CITY_NAME, value.name)
        putString(KEY_CITY_COUNTRY, value.country)
        // SharedPreferences has no double, and storing the raw bits round-trips exactly.
        putLong(KEY_CITY_LAT, value.latitude.toRawBits())
        putLong(KEY_CITY_LON, value.longitude.toRawBits())
        putString(KEY_CITY_TZ, value.timeZoneId)
      }
      apply()
    }

  private companion object {
    const val PREFS_NAME = "haasi_user_prefs"
    const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
    const val KEY_MANUAL_LANGUAGE = "manual_language"
    const val KEY_CITY_NAME = "city_name"
    const val KEY_CITY_COUNTRY = "city_country"
    const val KEY_CITY_LAT = "city_lat"
    const val KEY_CITY_LON = "city_lon"
    const val KEY_CITY_TZ = "city_tz"
  }
}
