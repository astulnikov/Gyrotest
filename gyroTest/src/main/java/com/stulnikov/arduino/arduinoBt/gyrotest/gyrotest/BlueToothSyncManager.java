package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest;

import android.app.Activity;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

public class BlueToothSyncManager extends BlueToothManager {

    private static final String TAG = "BlueToothSyncManager";
    private static final String CHECK_SYMBOL = "c";
    private static final char APPROVE_SYMBOL = 'a';
    private static final int RESPONSE_CHECK_DELAY = 50;
    private static final int ARDUINO_DATA = 1;

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

    @Override
    protected void sendData(String data) {
        data = CHECK_SYMBOL + data;
        super.sendData(data);
    }

    private void parseMessage(String message) {
        Log.i(TAG, "Row data: " + message);
        if (message.substring(0, 1).equals(CHECK_SYMBOL)) {
            message = message.substring(1, message.length());
            Log.i(TAG, "Data from Arduino: " + message);
            char[] approveChar = new char[1];
            message.getChars(0, 1, approveChar, 0);
            if (approveChar[0] != APPROVE_SYMBOL) {
                if (!TextUtils.isEmpty(message)) {
                    mListener.onDataReceived(message);
                }
            } else {
                Log.i(TAG, "Approve received");
            }
        }
    }
}
