package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.sensor.AccelerometerProvider.SensorCallback
import javax.inject.Inject

/**
 * @author alexeystulnikov 12/28/16.
 */
class SensorController @Inject constructor(
        context: Context
) : AccelerometerProvider, SensorEventListener {

    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometerSensor: Sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var callback: SensorCallback? = null

    override fun registerListener(callback: SensorCallback?) {
        this.callback = callback
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun unregisterListener(callback: SensorCallback?) {
        sensorManager.unregisterListener(this)
        this.callback = null
    }

    override fun onSensorChanged(event: SensorEvent) {
        callback?.onSensorXChanged(event.values[AXIS_X])
        callback?.onSensorYChanged(event.values[AXIS_Y])
        callback?.onSensorZChanged(event.values[AXIS_Z])
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        callback!!.onAccuracyChanged(accuracy)
    }

    companion object {
        private const val AXIS_X = 0
        private const val AXIS_Y = 1
        private const val AXIS_Z = 2
    }
}