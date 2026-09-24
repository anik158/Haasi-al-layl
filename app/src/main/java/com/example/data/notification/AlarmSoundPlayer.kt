package com.example.data.notification

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

/**
 * The ringing part of an alarm: a looping tone and a repeating vibration that keep going until
 * something stops them.
 *
 * This is what separates an alarm from a notification. A notification's sound is a one-shot a few
 * seconds long, played by the system when the notification posts; miss those seconds and there is
 * nothing left but a silent row in the shade. A prayer alert has to be able to wake someone, so it
 * rings until it is dismissed.
 *
 * A process-wide singleton rather than something the activity owns, because the activity can be
 * recreated by a rotation or a configuration change mid-ring and must not end up with two players
 * on the same tone.
 *
 * `USAGE_ALARM` routes the tone to the alarm stream, which is exempt from ring-silent and from
 * Do Not Disturb by default - the same reasoning that put the notification channels on that usage.
 */
object AlarmSoundPlayer {

  private const val TAG = "AlarmSoundPlayer"

  /**
   * How long the ring is allowed to run unattended.
   *
   * A real alarm gives up rather than draining the battery flat into an empty room. Three minutes
   * is long enough to wake someone and short enough to be forgivable if it fires by mistake.
   *
   * Enforced here rather than by the alarm screen, because the screen is not guaranteed to open:
   * the system downgrades a full-screen intent to a heads-up banner whenever the device is
   * already unlocked and in use. A timeout that lived in the activity would then never run, and
   * a banner nobody taps would ring until the process died.
   */
  const val MAX_RING_MILLIS = 3L * 60L * 1000L

  /** Buzz, pause, buzz - repeated from index 0, which is what makes it a loop. */
  private val VIBRATION_PATTERN = longArrayOf(0, 700, 900, 700, 1500)

  private var player: MediaPlayer? = null
  private var vibrator: Vibrator? = null

  /**
   * The auto-stop, posted to the main looper. Safe to rely on: the process cannot be reclaimed
   * while a MediaPlayer is actively playing, which is exactly the window this has to cover.
   */
  private val timeoutHandler = Handler(Looper.getMainLooper())
  private val stopOnTimeout = Runnable {
    Log.i(TAG, "alarm reached its ${MAX_RING_MILLIS / 1000}s limit unattended; stopping")
    stop()
  }

  /** Idempotent: a second call while already ringing is a no-op, not a second tone. */
  fun start(context: Context) {
    synchronized(this) {
      if (player != null) return
      player = buildPlayer(context)
      vibrator = startVibration(context)
      timeoutHandler.removeCallbacks(stopOnTimeout)
      timeoutHandler.postDelayed(stopOnTimeout, MAX_RING_MILLIS)
    }
  }

  fun stop() {
    synchronized(this) {
      timeoutHandler.removeCallbacks(stopOnTimeout)
      player?.let { active ->
        runCatching {
          if (active.isPlaying) active.stop()
        }.onFailure { Log.w(TAG, "could not stop the tone", it) }
        runCatching { active.release() }
      }
      player = null
      runCatching { vibrator?.cancel() }
      vibrator = null
    }
  }

  private fun buildPlayer(context: Context): MediaPlayer? = try {
    MediaPlayer().apply {
      setAudioAttributes(
        AudioAttributes.Builder()
          .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
          .setUsage(AudioAttributes.USAGE_ALARM)
          .build()
      )
      // Same URI the channels play, so the full-screen alarm and the notification cannot drift
      // onto different tones.
      setDataSource(context, PrayerNotificationManager.alertSoundUri(context))
      isLooping = true
      // Deliberately not raising the user's alarm volume: an alarm app that overrides the
      // volume the user chose is worse than one that is occasionally too quiet.
      prepare()
      start()
    }
  } catch (e: Exception) {
    // A dead audio path must not take the visible alarm down with it - the screen is still the
    // part that says which prayer it is.
    Log.e(TAG, "could not start the alarm tone", e)
    null
  }

  private fun startVibration(context: Context): Vibrator? {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)
        ?.defaultVibrator
    } else {
      @Suppress("DEPRECATION")
      context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    } ?: return null

    return try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val attributes = AudioAttributes.Builder()
          .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
          .setUsage(AudioAttributes.USAGE_ALARM)
          .build()
        vibrator.vibrate(
          VibrationEffect.createWaveform(VIBRATION_PATTERN, /* repeat = */ 0),
          attributes
        )
      } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(VIBRATION_PATTERN, /* repeat = */ 0)
      }
      vibrator
    } catch (e: Exception) {
      Log.w(TAG, "could not start the alarm vibration", e)
      null
    }
  }
}
