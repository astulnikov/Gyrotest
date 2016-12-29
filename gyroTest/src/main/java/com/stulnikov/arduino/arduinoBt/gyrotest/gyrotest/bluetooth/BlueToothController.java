package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.bluetooth;

/**
 * @author alexeystulnikov 12/29/16.
 */

public interface BlueToothController {
    void sendData(String data);

    void setListener(BlueToothManagerListener listener);

    void start();

    void stop();

    interface BlueToothManagerListener {
        void onBlueToothReady();

        void onDeviceConnected();

        void onDeviceDisconnected();

        void onDataReceived(String data);
    }
}
