package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.sensor;

/**
 * @author alexeystulnikov 12/28/16.
 */

public interface AccelerometerProvider {

    interface SensorCallback {
        void onSensorXChanged(float x);
        void onSensorYChanged(float y);
        void onSensorZChanged(float z);
        void onAccuracyChanged(int accuracy);
    }

    void registerListener(SensorCallback callback);
    void unregisterListener(SensorCallback callback);
}
