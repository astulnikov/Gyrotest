package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.dagger;

import android.app.Application;

import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.bluetooth.BluetoothController;
import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.sensor.AccelerometerProvider;

import javax.inject.Singleton;

import dagger.Component;

/**
 * @author alexeystulnikov 2/28/17.
 */
@Component(modules = ApplicationModule.class)
@Singleton
public interface ApplicationComponent {
    Application getApplication();

    AccelerometerProvider getAccelerometer();

    BluetoothController getBluetooth();
}