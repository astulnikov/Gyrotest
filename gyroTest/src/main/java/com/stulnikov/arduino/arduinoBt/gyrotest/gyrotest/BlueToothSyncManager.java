package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

public class BlueToothSyncManager extends BlueToothManager {

    private static final String TAG = "BlueToothManager";
    private static final String CHECK_SYMBOL = "c";
    private static final String APPROOVE_SYMBOL = "a";
    private static final int RESPONSE_CHECK_DELAY = 50;
    private static final int ARDUINO_DATA = 1;

    private String mDataBuffer;

    public BlueToothSyncManager(Activity activity) {
        super(activity);
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
        mDataBuffer = data;
        sendData(CHECK_SYMBOL);
        sendData(data);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkStatus();
            }
        }, RESPONSE_CHECK_DELAY);
    }

    private void parseMessage(String message) {
        if (message.substring(0, 1).equals(CHECK_SYMBOL)) {
            message = message.substring(1, message.length());
            Log.i(TAG, "Данные от Arduino: " + message);
            if (mDataBuffer == null) {
                mListener.onDataReceived(message);
                sendData(CHECK_SYMBOL);
                sendData(APPROOVE_SYMBOL);
            } else if (message.equals(APPROOVE_SYMBOL)) {
                mDataBuffer = null;
            }
        }
    }

    private void checkStatus() {
        if (mDataBuffer != null) {
            safeSendData(mDataBuffer);
        }
    }
}
