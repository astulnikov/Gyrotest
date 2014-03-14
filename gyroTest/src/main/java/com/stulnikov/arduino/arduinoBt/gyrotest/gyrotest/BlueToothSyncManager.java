package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest;

import android.app.Activity;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class BlueToothSyncManager extends BlueToothManager {

    private static final String TAG = "BlueToothManager";
    private static final String CHECK_SYMBOL = "c";
    private static final String APPROOVE_SYMBOL = "a";
    private static final int RESPONSE_CHECK_DELAY = 100;
    private static final int ARDUINO_DATA = 1;
    private static final int ATTEMPTS_TOTAL = 3;

    private String mDataBuffer;
    private int attemptCount;
    private boolean mSendInProgress;
    private Timer mWaitApproveTimer;

    public BlueToothSyncManager(Activity activity) {
        super(activity);
        mWaitApproveTimer = new Timer();
        mMessageHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case ARDUINO_DATA:
                        byte[] readBuf = (byte[]) msg.obj;
                        String strIn = new String(readBuf, 0, msg.arg1);
                        parseMessage(strIn);
                        break;
                }
            }
        };
    }

    public void safeSendData(String data) {
        if (!mSendInProgress) {
            mSendInProgress = true;
            attemptSend(data);
        }
    }

    private void attemptSend(String data) {
        if (attemptCount < ATTEMPTS_TOTAL) {
            attemptCount++;
            mDataBuffer = data;
            sendData(CHECK_SYMBOL + data);
            mWaitApproveTimer.cancel();
            mWaitApproveTimer = new Timer();
            mWaitApproveTimer.schedule(new WaitApproveTask(), RESPONSE_CHECK_DELAY);
        } else {
            attemptCount = 0;
            mSendInProgress = false;
            mWaitApproveTimer.cancel();
            mWaitApproveTimer = new Timer();
        }
    }

    private void parseMessage(String message) {
        Log.i(TAG, "Row data: " + message);
        if (message.substring(0, 1).equals(CHECK_SYMBOL)) {
            message = message.substring(1, message.length());
            Log.i(TAG, "Данные от Arduino: " + message);
            if (!mSendInProgress) {
                if (!TextUtils.isEmpty(message)) {
                    mListener.onDataReceived(message);
                    sendData(CHECK_SYMBOL + APPROOVE_SYMBOL);
                }
            } else if (message.equals(APPROOVE_SYMBOL)) {
                mDataBuffer = null;
                mSendInProgress = false;
                mWaitApproveTimer.cancel();
                mWaitApproveTimer = new Timer();
            }
        }
    }


    /**
     * Only after this check it will be available to safe send another message
     */
    private void checkStatus() {
        Log.d(TAG, "checkStatus " + (mDataBuffer != null));
        if (mDataBuffer != null) {
            attemptSend(mDataBuffer);
        }
    }

    class WaitApproveTask extends TimerTask {
        @Override
        public void run() {
            checkStatus();
        }
    }
}
