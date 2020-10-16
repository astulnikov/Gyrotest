package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.bluetooth

/**
 * @author alexeystulnikov 12/29/16.
 */
interface BluetoothController {
    /**
     * Send string to bluetooth output stream.
     *
     * @param data text to sendData
     */
    fun sendData(data: String)

    /**
     * Registers listener for Bluetooth related events
     *
     * @param listener instance to listen to events
     */
    fun setListener(listener: BluetoothManagerListener?)

    /**
     * Establishes bluetooth connection & will notify about result if listener was set
     */
    fun start()

    /**
     * Stops bluetooth connections & free resources
     */
    fun stop()
    interface BluetoothManagerListener {
        /**
         * Notifies device has no BT module
         */
        fun onBluetoothMissing()

        /**
         * Notifies BT module is disabled. Need to enable & try to start again
         */
        fun onBluetoothDisabled()

        /**
         * Notifies BT check has been passed. Wait.
         */
        fun onBlueToothReady()

        /**
         * Notifies connection is established. It is possible to send messages until now &
         * messages could be received
         */
        fun onDeviceConnected()

        /**
         * Notifies connection is lost
         */
        fun onDeviceDisconnected()

        /**
         * Notifies data over bluetooth has been found
         */
        fun onDataReceived(data: String)
    }
}