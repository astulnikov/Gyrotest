package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest

import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.bluetooth.BluetoothController
import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.sensor.AccelerometerProvider

/**
 * @author alexeystulnikov 12/25/16.
 */
abstract class BasePresenter<T : BaseView?> {
    var view: T? = null
        private set

    fun bindView(view: T) {
        this.view = view
    }

    fun unbindView() {
        view = null
    }

    abstract fun start()
    abstract fun stop()
    abstract fun setSensorController(provider: AccelerometerProvider?)
    abstract fun setBluetoothManager(controller: BluetoothController)
}