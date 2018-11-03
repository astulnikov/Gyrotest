package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest;

import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.bluetooth.BluetoothController;
import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.sensor.AccelerometerProvider;

import javax.inject.Inject;

/**
 * @author alexeystulnikov 12/25/16.
 */

public class MainPresenter extends BasePresenter<MainView> implements AccelerometerProvider.SensorCallback,
        BluetoothController.BlueToothManagerListener {

    private static final int START_ANGLE = 90;

    private static final String STEERING_SYMBOL = "s";
    private static final String ROBOT_SYMBOL = "r";
    private static final String DRIVE_SYMBOL = "d";
    private static final String DRIVE_FAST_FORWARD = "3";
    private static final String RUN_FORWARD_PERCENT_SYMBOL = "4";
    private static final String DRIVE_FORWARD = "1";
    private static final String DRIVE_BACK = "2";
    private static final String DRIVE_FALSE = "0";

    public static final int SEND_THRESHOLD = 50;

    private BluetoothController mBlueToothController;

    private AccelerometerProvider mAccelerometerProvider;

    private int mAverageAngle;
    private boolean mRobotMode;

    private long mLastAngleSentTimestamp;
    private long mLastPowerSentTimestamp;

    @Inject
    public MainPresenter(AccelerometerProvider accelerometer, BluetoothController bluetoothController) {
        mAccelerometerProvider = accelerometer;
        mBlueToothController = bluetoothController;
    }

    @Override
    public void start() {
        mBlueToothController.start();
        if (mAccelerometerProvider != null) {
            mAccelerometerProvider.registerListener(this);
        }
        mBlueToothController.setListener(this);
    }

    @Override
    public void stop() {
        mBlueToothController.stop();
        if (mAccelerometerProvider != null) {
            mAccelerometerProvider.unregisterListener(this);
        }
        mBlueToothController.setListener(null);
    }

    @Override
    public void setSensorController(AccelerometerProvider provider) {
        mAccelerometerProvider = provider;
    }

    @Override
    public void setBluetoothManager(BluetoothController controller) {
        mBlueToothController = controller;
    }

    @Override
    public void onSensorXChanged(float x) {
        getView().showX(x);
    }

    @Override
    public void onSensorYChanged(float y) {
        getView().showY(y);
        setAngle((int) y * 10);
    }

    @Override
    public void onSensorZChanged(float z) {
        getView().showZ(z);
    }

    @Override
    public void onAccuracyChanged(int accuracy) {
    }

    @Override
    public void onBluetoothMissing() {
        getView().onDeviceDisconnected();
    }

    @Override
    public void onBluetoothDisabled() {
        getView().showEnableBluetooth();
    }

    @Override
    public void onBlueToothReady() {
    }

    @Override
    public void onDeviceConnected() {
        getView().onDeviceConnected();
    }

    @Override
    public void onDeviceDisconnected() {
        getView().onDeviceDisconnected();
    }

    @Override
    public void onDataReceived(String data) {
        getView().showReceivedData(data);
    }

    public void setDrive(boolean isDrive) {
        String message = DRIVE_SYMBOL + (isDrive ? DRIVE_FORWARD : DRIVE_FALSE);
        mBlueToothController.sendData(message);
    }

    public void setDrivePercent(int percent) {
        if (System.currentTimeMillis() - mLastPowerSentTimestamp > SEND_THRESHOLD) {
            mBlueToothController.sendData(DRIVE_SYMBOL + RUN_FORWARD_PERCENT_SYMBOL + percent);
            mLastPowerSentTimestamp = System.currentTimeMillis();
        }
    }

    public void setDriveBack() {
        mBlueToothController.sendData(DRIVE_SYMBOL + DRIVE_BACK);
    }

    public void toggleRobotMode() {
        mRobotMode = !mRobotMode;
        getView().toggleRobotMode(mRobotMode);

        if (mRobotMode) {
            if (mAccelerometerProvider != null) {
                mAccelerometerProvider.registerListener(this);
            }
        } else {
            if (mAccelerometerProvider != null) {
                mAccelerometerProvider.unregisterListener(this);
            }
        }
        mBlueToothController.sendData(ROBOT_SYMBOL);
    }

    /**
     * Takes angle of device rotation adn sends angle that should be applied to servo
     * e.g. average last 2 angles plus start servo point (90 deg.)
     * also applying limit to rotation (/3.3) to avoid wide rotation
     *
     * @param angle angle of device
     */
    private void setAngle(int angle) {
        int newAverageAngle = Math.round(((mAverageAngle + angle) / 2) / 2.8f);
        if (System.currentTimeMillis() - mLastAngleSentTimestamp > SEND_THRESHOLD) {
            if (mAverageAngle != newAverageAngle) {
                mAverageAngle = newAverageAngle;
                getView().showAngle(mAverageAngle);
                int angleToSend = START_ANGLE - mAverageAngle;
                mBlueToothController.sendData(STEERING_SYMBOL + String.valueOf(angleToSend));
            }
            mLastAngleSentTimestamp = System.currentTimeMillis();
        }
    }
}
