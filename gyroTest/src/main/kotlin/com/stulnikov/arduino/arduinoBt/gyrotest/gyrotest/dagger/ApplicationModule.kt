package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.dagger

import android.app.Application
import android.content.Context
import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.bluetooth.BluetoothController
import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.bluetooth.BluetoothImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * @author alexeystulnikov 2/28/17.
 */
@Module
class ApplicationModule(private val application: Application) {

    @Provides
    @Singleton
    fun provideApplication(): Application {
        return application
    }

    @Provides
    @Singleton
    fun provideApplicationContext(): Context {
        return application.applicationContext
    }

    @Provides
    fun provideBluetooth(): BluetoothController {
        return BluetoothImpl()
    }
}