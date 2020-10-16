package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import timber.log.Timber
import java.io.IOException
import java.util.*


/**
 * @author alexeystulnikov 12/29/16.
 */
class BluetoothConnector(private val macAddress: String) {

    private var bluetoothAdapter: BluetoothAdapter? = null

    val connectorObserver: Observable<BluetoothSocket>
        get() = Observable.create { subscriber ->
            if (!initBtAdapter(subscriber)) {
                Timber.d("Bluetooth init failed. Return.")
                return@create
            }
            Timber.i("***Connect to device with address : $macAddress ***")
            val device = bluetoothAdapter!!.getRemoteDevice(macAddress)
            Timber.i("***Got remote  Device*** ${device.name}")
            var socket: BluetoothSocket? = null
            try {
                socket = device.createInsecureRfcommSocketToServiceRecord(SPP_UUID)
                Timber.i("***Create socket***")
            } catch (e: IOException) {
                subscriber.onError(e)
            }
            Timber.i("***Cancel device discovery***")
            bluetoothAdapter!!.cancelDiscovery()
            if (socket != null) {
                try {
                    Timber.i("***Connecting...***")
                    socket.connect()
                } catch (e: IOException) {
                    try {
                        socket.close()
                    } catch (e2: IOException) {
                        subscriber.onError(e2)
                    }
                    subscriber.onError(e)
                    return@create
                }
                if (subscriber.isDisposed) {
                    try {
                        socket.close()
                    } catch (e: Exception) {
                        subscriber.onError(e)
                    }
                }
            }
            Timber.i("***Connection successful***")
            subscriber.onNext(socket)
            subscriber.onComplete()
        }

    private fun initBtAdapter(observableEmitter: ObservableEmitter<BluetoothSocket>): Boolean {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return if (bluetoothAdapter != null) {
            if (bluetoothAdapter!!.isEnabled) {
                Timber.d("Bluetooth enabled. Fine.")
                true
            } else {
                Timber.d("Bluetooth disabled. Need to enable.")
                observableEmitter.onError(BluetoothDisabledException())
                false
            }
        } else {
            Timber.d("Bluetooth missing.")
            observableEmitter.onError(BluetoothMissingException())
            false
        }
    }

    companion object {
        private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }
}