package com.example.data.qibla

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class QiblaSensorManager(context: Context) {
  private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

  fun getCompassHeadingFlow(): Flow<Float> = callbackFlow {
    if (sensorManager == null) {
      trySend(0f)
      close()
      return@callbackFlow
    }

    val rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    val rMatrix = FloatArray(9)
    val orientation = FloatArray(3)
    val lastAccelerometer = FloatArray(3)
    val lastMagnetometer = FloatArray(3)
    var hasAccel = false
    var hasMag = false

    val listener = object : SensorEventListener {
      override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
          SensorManager.getRotationMatrixFromVector(rMatrix, event.values)
          SensorManager.getOrientation(rMatrix, orientation)
          val azimuthRad = orientation[0]
          var azimuthDeg = Math.toDegrees(azimuthRad.toDouble()).toFloat()
          azimuthDeg = (azimuthDeg + 360f) % 360f
          trySend(azimuthDeg)
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
          System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
          hasAccel = true
          computeFallback()
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
          System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.size)
          hasMag = true
          computeFallback()
        }
      }

      private fun computeFallback() {
        if (hasAccel && hasMag) {
          val success = SensorManager.getRotationMatrix(rMatrix, null, lastAccelerometer, lastMagnetometer)
          if (success) {
            SensorManager.getOrientation(rMatrix, orientation)
            var azimuthDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
            azimuthDeg = (azimuthDeg + 360f) % 360f
            trySend(azimuthDeg)
          }
        }
      }

      override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    if (rotationVector != null) {
      sensorManager.registerListener(listener, rotationVector, SensorManager.SENSOR_DELAY_UI)
    } else {
      if (accelerometer != null) {
        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
      }
      if (magnetometer != null) {
        sensorManager.registerListener(listener, magnetometer, SensorManager.SENSOR_DELAY_UI)
      }
    }

    awaitClose {
      sensorManager.unregisterListener(listener)
    }
  }
}
