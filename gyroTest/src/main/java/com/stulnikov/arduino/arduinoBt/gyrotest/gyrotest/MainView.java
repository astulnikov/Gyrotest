package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest;

import android.app.Activity;

/**
 * @author alexeystulnikov 12/25/16.
 */

public interface MainView extends BaseView {
    void toggleRobotMode(boolean robotMode);

    void showAngle(float angle);

    void showX(float x);

    void showY(float y);

    void showZ(float z);

    Activity getActivity();
}
