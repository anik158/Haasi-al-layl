package com.example.data.qibla

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * A compass reading straight from the sensors.
 *
 * [magneticAzimuth] is relative to **magnetic** north, which is what every Android orientation
 * API reports. Converting it to true north needs the local magnetic declination, and only the
 * caller knows the location - see the ViewModel, which folds in `GeomagneticField`.
 */
data class CompassReading(
  /** Degrees clockwise from magnetic north, 0 until 360. */
  val magneticAzimuth: Float,
  /** The latest `SensorManager.SENSOR_STATUS_*` value reported for the magnetometer. */
  val accuracy: Int
)

class QiblaSensorManager(context: Context) {
  private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

  fun getCompassReadingFlow(): Flow<CompassReading> = callbackFlow {
    if (sensorManager == null) {
      trySend(CompassReading(0f, SensorManager.SENSOR_STATUS_UNRELIABLE))
      close()
      return@callbackFlow
    }

    val rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    val rMatrix = FloatArray(9)
    val remapped = FloatArray(9)
    val orientation = FloatArray(3)
    val lastAccelerometer = FloatArray(3)
    val lastMagnetometer = FloatArray(3)
    var hasAccel = false
    var hasMag = false

    var accuracy = SensorManager.SENSOR_STATUS_UNRELIABLE

    // Smoothing runs on the unit vector, not on degrees: averaging degrees tears at the 0/360
    // seam, where 359 and 1 are two degrees apart but average to 180.
    var smoothedSin = Float.NaN
    var smoothedCos = Float.NaN

    fun emitFrom(matrix: FloatArray) {
      SensorManager.getOrientation(matrix, orientation)
      // Valid only while the phone is flat. Sighting a direction means holding it upright, so
      // once pitched past 45 degrees, remap the axes and read the orientation again.
      if (abs(orientation[1]) > PITCH_REMAP_THRESHOLD_RAD) {
        SensorManager.remapCoordinateSystem(
          matrix,
          SensorManager.AXIS_X,
          SensorManager.AXIS_Z,
          remapped
        )
        SensorManager.getOrientation(remapped, orientation)
      }

      val azimuthRad = orientation[0]
      val s = sin(azimuthRad)
      val c = cos(azimuthRad)
      if (smoothedSin.isNaN()) {
        smoothedSin = s
        smoothedCos = c
      } else {
        smoothedSin += SMOOTHING_ALPHA * (s - smoothedSin)
        smoothedCos += SMOOTHING_ALPHA * (c - smoothedCos)
      }
      val degrees = Math.toDegrees(atan2(smoothedSin, smoothedCos).toDouble()).toFloat()
      trySend(CompassReading((degrees + 360f) % 360f, accuracy))
    }

    val listener = object : SensorEventListener {
      override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        when (event.sensor.type) {
          Sensor.TYPE_ROTATION_VECTOR -> {
            SensorManager.getRotationMatrixFromVector(rMatrix, event.values)
            emitFrom(rMatrix)
          }

          Sensor.TYPE_ACCELEROMETER -> {
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
            hasAccel = true
            computeFallback()
          }

          Sensor.TYPE_MAGNETIC_FIELD -> {
            System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.size)
            hasMag = true
            // The rotation vector, when present, is the better source; this sensor is registered
            // alongside it only so accuracy callbacks arrive reliably.
            if (rotationVector == null) computeFallback()
          }

          else -> Unit
        }
      }

      private fun computeFallback() {
        if (!hasAccel || !hasMag) return
        val ok = SensorManager.getRotationMatrix(rMatrix, null, lastAccelerometer, lastMagnetometer)
        if (ok) emitFrom(rMatrix)
      }

      override fun onAccuracyChanged(sensor: Sensor?, newAccuracy: Int) {
        if (sensor?.type == Sensor.TYPE_MAGNETIC_FIELD ||
          sensor?.type == Sensor.TYPE_ROTATION_VECTOR
        ) {
          accuracy = newAccuracy
        }
      }
    }

    if (rotationVector != null) {
      sensorManager.registerListener(listener, rotationVector, SensorManager.SENSOR_DELAY_GAME)
      // Registered purely for its accuracy callbacks; rotation-vector accuracy reporting is
      // inconsistent across OEMs, and an uncalibrated compass is worth telling the user about.
      if (magnetometer != null) {
        sensorManager.registerListener(listener, magnetometer, SensorManager.SENSOR_DELAY_NORMAL)
      }
    } else {
      if (accelerometer != null) {
        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
      }
      if (magnetometer != null) {
        sensorManager.registerListener(listener, magnetometer, SensorManager.SENSOR_DELAY_GAME)
      }
    }

    awaitClose {
      sensorManager.unregisterListener(listener)
    }
  }

  private companion object {
    /** Roughly 45 degrees, past which the phone is upright rather than flat. */
    const val PITCH_REMAP_THRESHOLD_RAD = 0.785f
    const val SMOOTHING_ALPHA = 0.15f
  }
}
