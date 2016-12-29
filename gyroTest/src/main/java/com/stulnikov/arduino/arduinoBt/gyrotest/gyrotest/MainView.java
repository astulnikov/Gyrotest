package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest;

/**
 * @author alexeystulnikov 12/25/16.
 */

public interface MainView extends BaseView {
    void toggleRobotMode(boolean robotMode);

    void showAngle(float angle);

    void showX(float x);

    void showY(float y);

    void showZ(float z);

    void showReceivedData(String data);

    void onDeviceConnected();

    void onDeviceDisconnected();
}
