package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * @author alexeystulnikov 12/25/16.
 */

public class MainPresenter extends BasePresenter<MainView> implements SensorEventListener {

    private static final String TAG = MainPresenter.class.getSimpleName();
    private static final int START_ANGLE = 90;

    private static final String STEERING_SYMBOL = "s";
    private static final String ROBOT_SYMBOL = "r";
    private static final String DRIVE_SYMBOL = "d";
    private static final String DRIVE_FAST_FORWARD = "3";
    private static final String DRIVE_FORWARD = "1";
    private static final String DRIVE_BACK = "2";
    private static final String DRIVE_FALSE = "0";

    private static final int AXIS_X = 0;
    private static final int AXIS_Y = 1;
    private static final int AXIS_Z = 2;

    private BlueToothSyncManager mBlueToothManager;

    private SensorManager mSensorManager;
    private Sensor mAccelerometerSensor;

    private int mAverageAngle;
    private boolean mRobotMode;

    public MainPresenter() {
    }

    @Override
    public void start() {
        super.start();
        if (mBlueToothManager == null) {
            mBlueToothManager = new BlueToothSyncManager(getView().getActivity());
        }
        mBlueToothManager.start();
        if (mSensorManager == null) {
            initSensors();
        }
        mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void stop() {
        super.stop();
        mBlueToothManager.stop();
        mSensorManager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.i(TAG, "Sensor Type: " + event.sensor.getName());
        Log.i(TAG, "Sensor Accuracy: " + event.accuracy);
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                getView().showX(event.values[AXIS_X]);
                getView().showY(event.values[AXIS_Y]);
                getView().showZ(event.values[AXIS_Z]);
                setAngle((int) event.values[AXIS_Y] * 10);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    /**
     * Takes angle of device rotation adn sends angle that should be applied to servo
     * e.g. average last 2 angles plus start servo point (90 deg.)
     * also applying limit to rotation (/3.4) to avoid wide rotation
     *
     * @param angle angle of device
     */
    public void setAngle(int angle) {
        if (mAverageAngle != angle) {
            mAverageAngle = (mAverageAngle + angle) / 2;
            getView().showAngle(mAverageAngle);
            int angleToSend = START_ANGLE - (int) (mAverageAngle / 3.4f);
            mBlueToothManager.sendData(STEERING_SYMBOL + String.valueOf(angleToSend));
        }
    }

    public void setDrive(boolean isDrive) {
        String message = DRIVE_SYMBOL + (isDrive ? DRIVE_FORWARD : DRIVE_FALSE);
        mBlueToothManager.sendData(message);
    }

    public void setDriveFast() {
        mBlueToothManager.sendData(DRIVE_SYMBOL + DRIVE_FAST_FORWARD);
    }

    public void setDriveBack() {
        mBlueToothManager.sendData(DRIVE_SYMBOL + DRIVE_BACK);
    }

    public void toggleRobotMode() {
        mRobotMode = !mRobotMode;
        getView().toggleRobotMode(mRobotMode);

        if (mRobotMode) {
            mSensorManager.unregisterListener(this);
        } else {
            mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        mBlueToothManager.sendData(ROBOT_SYMBOL);
    }

    private void initSensors() {
        mSensorManager = (SensorManager) getView().getActivity().getSystemService(Context.SENSOR_SERVICE);
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }
}
