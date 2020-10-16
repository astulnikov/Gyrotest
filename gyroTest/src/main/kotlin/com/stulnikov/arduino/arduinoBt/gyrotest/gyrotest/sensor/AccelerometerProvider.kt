package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.sensor

/**
 * @author alexeystulnikov 12/28/16.
 */
interface AccelerometerProvider {
    interface SensorCallback {
        fun onSensorXChanged(x: Float)
        fun onSensorYChanged(y: Float)
        fun onSensorZChanged(z: Float)
        fun onAccuracyChanged(accuracy: Int)
    }

    fun registerListener(callback: SensorCallback?)
    fun unregisterListener(callback: SensorCallback?)
}