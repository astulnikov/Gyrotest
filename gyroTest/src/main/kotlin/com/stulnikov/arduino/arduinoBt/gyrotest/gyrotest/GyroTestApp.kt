package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest

import android.app.Application
import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.dagger.ApplicationComponent
import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.dagger.ApplicationModule
import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.dagger.DaggerApplicationComponent
import timber.log.Timber
import timber.log.Timber.DebugTree

/**
 * @author alexeystulnikov 1/3/17.
 */
class GyroTestApp : Application() {
    var applicationComponent: ApplicationComponent? = null
    override fun onCreate() {
        super.onCreate()
        initTimberTrees()
        initDaggerComponents()
    }

    private fun initTimberTrees() {
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        } else {
            Timber.plant(DebugTree())
        }
    }

    private fun initDaggerComponents() {
        applicationComponent = DaggerApplicationComponent
                .builder()
                .applicationModule(ApplicationModule(this))
                .build()
    }
}