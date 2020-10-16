package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.bluetooth

import android.bluetooth.BluetoothSocket
import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.bluetooth.BluetoothController.BluetoothManagerListener
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import timber.log.Timber

/**
 * @author alexeystulnikov 12/29/16.
 */
class BluetoothImpl : BluetoothController {
    private lateinit var bluetoothConnection: BluetoothConnection
    private var bluetoothMessagesDisposable = CompositeDisposable()
    private var listener: BluetoothManagerListener? = null
    private val sendSubject = PublishSubject.create<String>()

    override fun sendData(data: String) {
        sendSubject.onNext(CHECK_SYMBOL.toString() + data + END_LINE_SYMBOL)
    }

    override fun setListener(listener: BluetoothManagerListener?) {
        this.listener = listener
    }

    override fun start() {
        connect()
    }

    override fun stop() {
        if (this::bluetoothConnection.isInitialized) {
            bluetoothMessagesDisposable.dispose()
            bluetoothConnection.unsubscribeFromInputData()
            bluetoothConnection.closeConnection()
        }
    }

    private fun connect() {
        val bluetoothConnector = BluetoothConnector(MAC_ADDRESS)
        bluetoothConnector.connectorObserver
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ bluetoothSocket ->
                    Timber.d("socket received")
                    listener?.onDeviceConnected()

                    subscribeToMessages(bluetoothSocket)
                    bluetoothConnection.subscribeToInputData(sendSubject.serialize())
                }, { throwable: Throwable ->
                    Timber.e(throwable, "Error occurred %s", throwable.toString())
                    when (throwable) {
                        is BluetoothMissingException -> {
                            listener?.onBluetoothMissing()
                        }
                        is BluetoothDisabledException -> {
                            listener?.onBluetoothDisabled()
                        }
                        else -> {
                            listener?.onDeviceDisconnected()
                        }
                    }
                }, { Timber.d("Connection mission completed") })
    }

    private fun subscribeToMessages(bluetoothSocket: BluetoothSocket) {
        try {
            bluetoothConnection = BluetoothConnection(bluetoothSocket)
            bluetoothMessagesDisposable.add(
                    bluetoothConnection.getStringStreamObservable(END_LINE_SYMBOL.toInt())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ data: String ->
                                listener?.onDataReceived(data)
                            }, { throwable: Throwable? ->
                                Timber.e(throwable, "Error occurred")
                                listener?.onDeviceDisconnected()
                            }))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val MAC_ADDRESS = "98:D3:31:B1:79:C0" // BT module MAC-address
        private const val CHECK_SYMBOL = 'c'
        private const val END_LINE_SYMBOL = 'l'
    }
}