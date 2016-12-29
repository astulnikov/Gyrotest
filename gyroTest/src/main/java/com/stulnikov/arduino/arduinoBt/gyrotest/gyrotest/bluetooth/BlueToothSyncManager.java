package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.bluetooth;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

public class BlueToothSyncManager extends BlueToothManager {

    private static final String TAG = "BlueToothSyncManager";
    private static final String CHECK_SYMBOL = "c";
    private static final char APPROVE_SYMBOL = 'a';

    public BlueToothSyncManager(Activity activity) {
        super(activity);
    }

    @Override
    public void sendData(String data) {
        data = CHECK_SYMBOL + data;
        super.sendData(data);
    }

    @Override
    protected void parseMessage(String message) {
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
                message = message.substring(1);
                if (!TextUtils.isEmpty(message)) {
                    Log.i(TAG, "Confirmation for " + message);
                    mListener.onDataReceived(message);
                }
            }
        }
    }
}
