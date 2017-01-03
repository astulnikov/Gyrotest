package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.bluetooth;

/**
 * @author alexeystulnikov 12/29/16.
 */
public interface BluetoothController {
    /**
     * Send string to bluetooth output stream.
     *
     * @param data text to sendData
     */
    void sendData(String data);

    /**
     * Registers listener for Bluetooth related events
     *
     * @param listener instance to listen to events
     */
    void setListener(BlueToothManagerListener listener);

    /**
     * Establishes bluetooth connection & will notify about result if listener was set
     */
    void start();

    /**
     * Stops bluetooth connections & free resources
     */
    void stop();

    interface BlueToothManagerListener {
        /**
         * Notifies device has no BT module
         */
        void onBluetoothMissing();

        /**
         * Notifies BT module is disabled. Need to enable & try to start again
         */
        void onBluetoothDisabled();

        /**
         * Notifies BT check has been passed. Wait.
         */
        void onBlueToothReady();

        /**
         * Notifies connection is established. It is possible to send messages until now &
         * messages could be received
         */
        void onDeviceConnected();

        /**
         * Notifies connection is lost
         */
        void onDeviceDisconnected();

        /**
         * Notifies data over bluetooth has been found
         */
        void onDataReceived(String data);
    }
}
