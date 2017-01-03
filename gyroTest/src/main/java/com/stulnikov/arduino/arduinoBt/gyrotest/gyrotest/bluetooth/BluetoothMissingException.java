package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.bluetooth;

/**
 * @author alexeystulnikov 1/2/17.
 */

public class BluetoothMissingException extends IllegalStateException {
    public BluetoothMissingException() {
        super("Device should has bluetooth module");
    }
}
