package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.dagger;

import android.app.Application;

import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.bluetooth.BluetoothController;
import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.bluetooth.BluetoothImpl;
import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.sensor.AccelerometerProvider;
import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.sensor.SensorController;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * @author alexeystulnikov 2/28/17.
 */
@Module
public class ApplicationModule {

    private Application mApplication;

    public ApplicationModule(Application application) {
        mApplication = application;
    }

    @Provides
    @Singleton
    Application providesApplication() {
        return mApplication;
    }

    @Provides
    AccelerometerProvider provideAccelerometer(Application application) {
        return new SensorController(application);
    }

    @Provides
    BluetoothController privideBluetooth(){
        return new BluetoothImpl();
    }
}
