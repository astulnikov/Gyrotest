package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest;

import android.app.Application;

import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.dagger.ApplicationComponent;
import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.dagger.ApplicationModule;
import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.dagger.DaggerApplicationComponent;

import timber.log.Timber;

/**
 * @author alexeystulnikov 1/3/17.
 */

public class GyroTestApp extends Application {
    ApplicationComponent mApplicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        initTimberTrees();
        initDaggerComponents();
    }

    public ApplicationComponent getApplicationComponent() {
        return mApplicationComponent;
    }

    private void initTimberTrees() {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new Timber.DebugTree());
        }
    }

    private void initDaggerComponents() {
        mApplicationComponent = DaggerApplicationComponent
                .builder()
                .applicationModule(new ApplicationModule(this))
                .build();
    }
}
