package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.dagger

import android.app.Application
import android.content.Context
import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.bluetooth.BluetoothController
import dagger.Component
import javax.inject.Singleton

/**
 * @author alexeystulnikov 2/28/17.
 */
@Component(modules = [ApplicationModule::class])
@Singleton
interface ApplicationComponent {
    val context: Context
    val application: Application?
    val bluetooth: BluetoothController
}