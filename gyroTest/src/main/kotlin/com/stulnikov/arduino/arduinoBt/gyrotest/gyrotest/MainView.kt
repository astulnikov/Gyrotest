package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest

/**
 * @author alexeystulnikov 12/25/16.
 */
interface MainView : BaseView {
    fun toggleRobotMode(robotMode: Boolean)
    fun showAngle(angle: Float)
    fun showX(x: Float)
    fun showY(y: Float)
    fun showZ(z: Float)
    fun showReceivedData(data: String)
    fun onDeviceConnected()
    fun onDeviceDisconnected()
    fun showEnableBluetooth()
}