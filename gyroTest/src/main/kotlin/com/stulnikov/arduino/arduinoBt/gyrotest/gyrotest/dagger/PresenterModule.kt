package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.dagger

import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.BasePresenter
import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.MainPresenter
import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.bluetooth.BluetoothController
import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.sensor.AccelerometerProvider
import dagger.Module
import dagger.Provides

/**
 * @author alexeystulnikov 2/28/17.
 */
@Module
class PresenterModule {

    @Provides
    fun provideMainActivityPresenter(
            accelerometer: AccelerometerProvider?,
            bluetoothController: BluetoothController
    ): BasePresenter<*> {
        return MainPresenter(accelerometer, bluetoothController)
    }
}