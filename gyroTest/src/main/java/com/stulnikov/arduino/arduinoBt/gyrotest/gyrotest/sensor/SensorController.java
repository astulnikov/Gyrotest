package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * @author alexeystulnikov 12/28/16.
 */

public class SensorController implements AccelerometerProvider, SensorEventListener {

    private static final int AXIS_X = 0;
    private static final int AXIS_Y = 1;
    private static final int AXIS_Z = 2;

    private SensorManager mSensorManager;
    private Sensor mAccelerometerSensor;
    private AccelerometerProvider.SensorCallback mCallback;

    public SensorController(Context context) {
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    public void registerListener(SensorCallback callback) {
        mCallback = callback;
        mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void unregisterListener(SensorCallback callback) {
        mSensorManager.unregisterListener(this);
        mCallback = null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        mCallback.onSensorXChanged(event.values[AXIS_X]);
        mCallback.onSensorYChanged(event.values[AXIS_Y]);
        mCallback.onSensorZChanged(event.values[AXIS_Z]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        mCallback.onAccuracyChanged(accuracy);
    }
}
