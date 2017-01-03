package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest;

import android.app.Application;

import timber.log.Timber;

/**
 * @author alexeystulnikov 1/3/17.
 */

public class GyroTestApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initTimberTrees();
    }

    private void initTimberTrees() {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
