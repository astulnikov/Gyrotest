package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.bluetooth;

/**
 * @author alexeystulnikov 1/2/17.
 */

public class BluetoothDisabledException extends IllegalStateException {
    public BluetoothDisabledException() {
        super("Bluetooth module should be enabled");
    }
}
