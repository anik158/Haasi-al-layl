package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.data.localization.AppLanguage
import com.example.data.model.CityLocation
import com.example.data.prayer.CalculationMethod
import com.example.data.prayer.IslamicMadhab

/**
 * User choices that must survive leaving the app.
 *
 * Everything here is written to SharedPreferences, so it lives for the whole installation
 * lifecycle - across process death, restarts and reboots - and is only cleared when the user
 * uninstalls or clears app data. Onboarding asks once, not on every launch.
 *
 * Every setter commits synchronously rather than using `apply()`. `apply()` already survives
 * ordinary process death, but a force-stop inside its async flush window loses the write - which
 * is exactly the "my setting reset itself" report these values exist to prevent. These are rare,
 * user-initiated writes of a handful of keys, so the blocking write is sub-millisecond.
 */
class UserPreferences(context: Context) {

  private val prefs: SharedPreferences =
    context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  /** True once the user has a language and a location, whether auto-detected or chosen by hand. */
  var isOnboardingComplete: Boolean
    get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false)
    set(value) {
      prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETE, value).commit()
    }

  /** True once the POST_NOTIFICATIONS prompt has been shown, so a denial is not re-asked forever. */
  var hasAskedNotificationPermission: Boolean
    get() = prefs.getBoolean(KEY_ASKED_NOTIF_PERMISSION, false)
    set(value) {
      prefs.edit().putBoolean(KEY_ASKED_NOTIF_PERMISSION, value).commit()
    }

  /**
   * True once the location prompt has been shown.
   *
   * Location is asked for on the first launch rather than when the user finds the "detect"
   * button, because the device's own coordinates are the only rung of the location ladder that
   * works offline, and without the permission the cached-fix read returns null without ever
   * reaching the receiver. This flag keeps a denial from becoming a prompt on every launch.
   */
  var hasAskedLocationPermission: Boolean
    get() = prefs.getBoolean(KEY_ASKED_LOCATION_PERMISSION, false)
    set(value) {
      prefs.edit().putBoolean(KEY_ASKED_LOCATION_PERMISSION, value).commit()
    }

  /**
   * True once the user has picked a city by hand.
   *
   * The distinction matters because the app keeps a GPS listener running while it has no location
   * of its own, and adopts the first fix that arrives. A city the user chose deliberately must
   * not be silently overwritten by that - so this is the flag that stops the watch.
   */
  var cityChosenManually: Boolean
    get() = prefs.getBoolean(KEY_CITY_CHOSEN_MANUALLY, false)
    set(value) {
      prefs.edit().putBoolean(KEY_CITY_CHOSEN_MANUALLY, value).commit()
    }

  /** An explicit language choice. Null means "follow the location". */
  var manualLanguage: AppLanguage?
    get() = prefs.getString(KEY_MANUAL_LANGUAGE, null)?.let { AppLanguage.fromCode(it) }
    set(value) = writeString(KEY_MANUAL_LANGUAGE, value?.code)

  /**
   * Which convention prayer times are calculated with. Defaults to Umm al-Qura, the standard
   * published by Saudi Arabia and used for Makkah and Madinah.
   */
  var calculationMethod: CalculationMethod
    get() = prefs.getString(KEY_CALCULATION_METHOD, null)
      ?.let { name -> CalculationMethod.entries.find { it.name == name } }
      ?: CalculationMethod.UMM_AL_QURA
    set(value) = writeString(KEY_CALCULATION_METHOD, value.name)

  /** Which school of jurisprudence sets the Asr shadow factor. */
  var madhab: IslamicMadhab
    get() = prefs.getString(KEY_MADHAB, null)
      ?.let { name -> IslamicMadhab.entries.find { it.name == name } }
      ?: IslamicMadhab.DEFAULT
    set(value) = writeString(KEY_MADHAB, value.name)

  /** The dhikr the user was last counting, so the Tasbih screen reopens where they left off. */
  var lastTasbihId: String?
    get() = prefs.getString(KEY_LAST_TASBIH_ID, null)
    set(value) = writeString(KEY_LAST_TASBIH_ID, value)

  /**
   * Hides the status-bar alarm icon by using a quota-limited alarm type. Honest cost: in deep
   * Doze the five-minute follow-up can arrive up to nine minutes late.
   */
  var discreetAlarmMode: Boolean
    get() = prefs.getBoolean(KEY_DISCREET_ALARMS, false)
    set(value) {
      prefs.edit().putBoolean(KEY_DISCREET_ALARMS, value).commit()
    }

  /** Bookkeeping so a process start can skip rescheduling when nothing relevant changed. */
  var lastScheduledEpochDay: Long
    get() = prefs.getLong(KEY_LAST_SCHEDULED_DAY, 0L)
    set(value) {
      prefs.edit().putLong(KEY_LAST_SCHEDULED_DAY, value).commit()
    }

  var lastScheduleSignature: String
    get() = prefs.getString(KEY_LAST_SCHEDULE_SIGNATURE, "") ?: ""
    set(value) = writeString(KEY_LAST_SCHEDULE_SIGNATURE, value)

  /**
   * The location the app has actually resolved, or null when it has none.
   *
   * Null is meaningful and must stay that way: the UI falls back to Makkah as a *placeholder* so
   * there is something to render, and code that cannot tell the placeholder from a real answer
   * will confidently tell a user in Helsinki that they are in Makkah - and derive an Arabic
   * interface from it. Read [com.example.ui.HaasiAlLaylViewModel.hasResolvedLocation] rather than
   * inferring anything from the rendered city.
   */
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
    set(value) {
      prefs.edit().run {
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
        commit()
      }
    }

  private fun writeString(key: String, value: String?) {
    prefs.edit().run {
      if (value == null) remove(key) else putString(key, value)
      commit()
    }
  }

  private companion object {
    const val PREFS_NAME = "haasi_user_prefs"
    const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
    const val KEY_ASKED_NOTIF_PERMISSION = "asked_notification_permission"
    const val KEY_ASKED_LOCATION_PERMISSION = "asked_location_permission"
    const val KEY_CITY_CHOSEN_MANUALLY = "city_chosen_manually"
    const val KEY_MANUAL_LANGUAGE = "manual_language"
    const val KEY_CALCULATION_METHOD = "calculation_method"
    const val KEY_MADHAB = "madhab"
    const val KEY_LAST_TASBIH_ID = "last_tasbih_id"
    const val KEY_DISCREET_ALARMS = "discreet_alarms"
    const val KEY_LAST_SCHEDULED_DAY = "last_scheduled_epoch_day"
    const val KEY_LAST_SCHEDULE_SIGNATURE = "last_schedule_signature"
    const val KEY_CITY_NAME = "city_name"
    const val KEY_CITY_COUNTRY = "city_country"
    const val KEY_CITY_LAT = "city_lat"
    const val KEY_CITY_LON = "city_lon"
    const val KEY_CITY_TZ = "city_tz"
  }
}
