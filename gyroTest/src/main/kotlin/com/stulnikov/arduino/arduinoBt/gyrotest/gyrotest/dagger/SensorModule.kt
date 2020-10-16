package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.dagger

import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.sensor.AccelerometerProvider
import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.sensor.SensorController
import dagger.Binds
import dagger.Module

/**
 * @author alexeystulnikov 2/28/17.
 */
@Module
interface SensorModule {

    @Binds
    @PerActivity
    fun bindAccelerometer(sensorController: SensorController): AccelerometerProvider
}