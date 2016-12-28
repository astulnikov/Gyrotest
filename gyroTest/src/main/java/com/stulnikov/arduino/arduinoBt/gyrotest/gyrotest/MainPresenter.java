package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest;

import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.sensor.AccelerometerProvider;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author alexeystulnikov 12/25/16.
 */

public class MainPresenter extends BasePresenter<MainView> implements AccelerometerProvider.SensorCallback {

    private static final String TAG = MainPresenter.class.getSimpleName();
    private static final int SEND_MESSAGE_PERIOD = 500;
    private static final int START_ANGLE = 90;

    private static final String STEERING_SYMBOL = "s";
    private static final String ROBOT_SYMBOL = "r";
    private static final String DRIVE_SYMBOL = "d";
    private static final String DRIVE_FAST_FORWARD = "3";
    private static final String DRIVE_FORWARD = "1";
    private static final String DRIVE_BACK = "2";
    private static final String DRIVE_FALSE = "0";

    private BlueToothSyncManager mBlueToothManager;
    private AccelerometerProvider mAccelerometerProvider;

    private int mAverageAngle;
    private boolean mRobotMode;

    private Timer mTimer;
    private RepeatSendMessageTask mSendRepeatedMessageTask;

    public MainPresenter() {
    }

    @Override
    public void start() {
        super.start();
        if (mBlueToothManager == null) {
            mBlueToothManager = new BlueToothSyncManager(getView().getActivity());
        }
        mBlueToothManager.start();
        if (mAccelerometerProvider != null) {
            mAccelerometerProvider.registerListener(this);
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (mTimer != null) {
            mTimer.cancel();
        }

        mBlueToothManager.stop();
        if (mAccelerometerProvider != null) {
            mAccelerometerProvider.unregisterListener(this);
        }
    }

    @Override
    public void setSensorController(AccelerometerProvider provider) {
        mAccelerometerProvider = provider;
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

    /**
     * Takes angle of device rotation adn sends angle that should be applied to servo
     * e.g. average last 2 angles plus start servo point (90 deg.)
     * also applying limit to rotation (/3.3) to avoid wide rotation
     *
     * @param angle angle of device
     */
    public void setAngle(int angle) {
        int newAverageAngle = Math.round(((mAverageAngle + angle) / 2) / 2.8f);
        if (mAverageAngle != newAverageAngle) {
            mAverageAngle = newAverageAngle;
            getView().showAngle(mAverageAngle);
            int angleToSend = START_ANGLE - mAverageAngle;
            mBlueToothManager.sendData(STEERING_SYMBOL + String.valueOf(angleToSend));
        }
    }

    public void setDrive(boolean isDrive) {
        String message = DRIVE_SYMBOL + (isDrive ? DRIVE_FORWARD : DRIVE_FALSE);
        startSendingMessages(message);
    }

    public void setDriveFast() {
        startSendingMessages(DRIVE_SYMBOL + DRIVE_FAST_FORWARD);
    }

    public void setDriveBack() {
        startSendingMessages(DRIVE_SYMBOL + DRIVE_BACK);
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
        mBlueToothManager.sendData(ROBOT_SYMBOL);
    }

    private void startSendingMessages(String message) {
        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = new Timer();

        mSendRepeatedMessageTask = new RepeatSendMessageTask(mBlueToothManager, message);
        mTimer.schedule(mSendRepeatedMessageTask, 0, SEND_MESSAGE_PERIOD);
    }

    static class RepeatSendMessageTask extends TimerTask {
        private String mMessage;
        private BlueToothManager mBlueToothManager;

        public RepeatSendMessageTask(BlueToothManager blueToothManager, String message) {
            this.mMessage = message;
            this.mBlueToothManager = blueToothManager;
        }

        @Override
        public void run() {
            mBlueToothManager.sendData(mMessage);
        }
    }
}
