package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.dagger;

import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.BasePresenter;
import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.MainPresenter;
import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.bluetooth.BluetoothController;
import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.sensor.AccelerometerProvider;

import dagger.Module;
import dagger.Provides;

/**
 * @author alexeystulnikov 2/28/17.
 */

@Module
public class PresenterModule {

    @Provides
    BasePresenter provideMainActivityPresenter(AccelerometerProvider accelerometer,
                                               BluetoothController bluetoothController) {
        return new MainPresenter(accelerometer, bluetoothController);
    }
}
