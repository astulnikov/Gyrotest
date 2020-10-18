package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest

import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.bluetooth.BluetoothController
import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.bluetooth.BluetoothController.BluetoothManagerListener
import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.sensor.AccelerometerProvider
import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.sensor.AccelerometerProvider.SensorCallback
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * @author alexeystulnikov 12/25/16.
 */
class MainPresenter @Inject constructor(
        private var accelerometerProvider: AccelerometerProvider?,
        private var blueToothController: BluetoothController
) : BasePresenter<MainView?>(), SensorCallback, BluetoothManagerListener {

    private var averageAngle = 0
    private var lastSentPower = 0
    private var robotMode = false
    private var lastAngleSentTimestamp: Long = 0
    private var lastPowerSentTimestamp: Long = 0

    override fun start() {
        blueToothController.start()
        if (accelerometerProvider != null) {
            accelerometerProvider!!.registerListener(this)
        }
        blueToothController.setListener(this)
    }

    override fun stop() {
        blueToothController.stop()
        if (accelerometerProvider != null) {
            accelerometerProvider!!.unregisterListener(this)
        }
        blueToothController.setListener(null)
    }

    override fun setSensorController(provider: AccelerometerProvider?) {
        accelerometerProvider = provider
    }

    override fun setBluetoothManager(controller: BluetoothController) {
        blueToothController = controller
    }

    override fun onSensorXChanged(x: Float) {
        view?.showX(x)
    }

    override fun onSensorYChanged(y: Float) {
        view?.showY(y)
        setAngle(y.toInt() * 10)
    }

    override fun onSensorZChanged(z: Float) {
        view?.showZ(z)
    }

    override fun onAccuracyChanged(accuracy: Int) {}

    override fun onBluetoothMissing() {
        view?.onDeviceDisconnected()
    }

    override fun onBluetoothDisabled() {
        view?.showEnableBluetooth()
    }

    override fun onBlueToothReady() {}
    override fun onDeviceConnected() {
        view?.onDeviceConnected()
    }

    override fun onDeviceDisconnected() {
        view?.onDeviceDisconnected()
    }

    override fun onDataReceived(data: String) {
        if (data.startsWith(CHECK_SYMBOL)) {
            val payload = data.substring(1)
            when {
                payload.startsWith("F:") -> {
                    val frontDistance = payload.filter { it.isDigit() }.toInt()
                    view?.showFrontDistance(mapDistance(frontDistance))
                }
                payload.startsWith("R:") -> {
                    val rearDistance = payload.filter { it.isDigit() }.toInt()
                    view?.showRearDistance(mapDistance(rearDistance))
                }
            }
        } else {
            view?.showReceivedData(data)
        }
    }

    private fun mapDistance(distance: Int): Int =
            when (distance) {
                in 0..25 -> DISTANCE_LEVEL_0
                in 26..50 -> DISTANCE_LEVEL_1
                in 51..100 -> DISTANCE_LEVEL_2
                else -> DISTANCE_LEVEL_3
            }

    fun setDrive(isDrive: Boolean) {
        val message = DRIVE_SYMBOL + if (isDrive) DRIVE_FORWARD else DRIVE_FALSE
        blueToothController.sendData(message)
        lastSentPower = 0
    }

    fun setDrivePercent(percent: Int) {
        if (System.currentTimeMillis() - lastPowerSentTimestamp > SEND_THRESHOLD && abs(lastSentPower - percent) > POWER_THRESHOLD) {
            blueToothController.sendData(DRIVE_SYMBOL + RUN_FORWARD_PERCENT_SYMBOL + percent)
            lastPowerSentTimestamp = System.currentTimeMillis()
            lastSentPower = percent
        }
    }

    fun setDriveBack() {
        blueToothController.sendData(DRIVE_SYMBOL + DRIVE_BACK)
    }

    fun toggleRobotMode() {
        robotMode = !robotMode
        view?.toggleRobotMode(robotMode)
        if (robotMode) {
            if (accelerometerProvider != null) {
                accelerometerProvider!!.registerListener(this)
            }
        } else {
            if (accelerometerProvider != null) {
                accelerometerProvider!!.unregisterListener(this)
            }
        }
        blueToothController.sendData(ROBOT_SYMBOL)
    }

    /**
     * Takes angle of device rotation adn sends angle that should be applied to servo
     * e.g. average last 2 angles plus start servo point (90 deg.)
     * also applying limit to rotation (/3.3) to avoid wide rotation
     *
     * @param angle angle of device
     */
    private fun setAngle(angle: Int) {
        val newAverageAngle = ((averageAngle + angle) / 2 / 2.8f).roundToInt()
        if (System.currentTimeMillis() - lastAngleSentTimestamp > SEND_THRESHOLD) {
            if (averageAngle != newAverageAngle) {
                averageAngle = newAverageAngle
                view?.showAngle(averageAngle.toFloat())
                val angleToSend = START_ANGLE - averageAngle
                blueToothController.sendData(STEERING_SYMBOL + angleToSend.toString())
            }
            lastAngleSentTimestamp = System.currentTimeMillis()
        }
    }

    companion object {
        private const val START_ANGLE = 90
        private const val CHECK_SYMBOL = "c"
        private const val STEERING_SYMBOL = "s"
        private const val ROBOT_SYMBOL = "r"
        private const val DRIVE_SYMBOL = "d"
        private const val DRIVE_FAST_FORWARD = "3"
        private const val RUN_FORWARD_PERCENT_SYMBOL = "4"
        private const val DRIVE_FORWARD = "1"
        private const val DRIVE_BACK = "2"
        private const val DRIVE_FALSE = "0"
        const val SEND_THRESHOLD = 50
        const val POWER_THRESHOLD = 3

        private const val DISTANCE_LEVEL_0 = 0
        private const val DISTANCE_LEVEL_1 = 1
        private const val DISTANCE_LEVEL_2 = 2
        private const val DISTANCE_LEVEL_3 = 3
    }
}