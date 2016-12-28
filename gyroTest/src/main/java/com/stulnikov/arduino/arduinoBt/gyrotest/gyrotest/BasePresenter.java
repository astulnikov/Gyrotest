package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest;

import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.sensor.AccelerometerProvider;

/**
 * @author alexeystulnikov 12/25/16.
 */

public abstract class BasePresenter<T extends BaseView> {
    protected T mView;

    public void bindView(T view) {
        mView = view;
    }

    public void unbindView() {
        mView = null;
    }

    public T getView() {
        return mView;
    }

    public void start() {

    }

    public void stop() {

    }

    public abstract void setSensorController(AccelerometerProvider provider);
}
