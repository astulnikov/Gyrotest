package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.bluetooth

import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.bluetooth.BluetoothController.BluetoothManagerListener
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * @author alexeystulnikov 12/29/16.
 */
class BluetoothImpl : BluetoothController {
    private var mBluetoothMessagesSubscription: Disposable? = null
    private var mListener: BluetoothManagerListener? = null
    override fun sendData(data: String) {
        Timber.d("Send data %s", data)
    }

    override fun setListener(listener: BluetoothManagerListener?) {
        mListener = listener
    }

    override fun start() {
        connect()
    }

    override fun stop() {
        if (mBluetoothMessagesSubscription != null) {
            mBluetoothMessagesSubscription!!.dispose()
        }
    }

    private fun connect() {
        Timber.d("Imitate connection")
        Completable.complete().delay(2000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Timber.d("Connected")
                    if (mListener != null) {
                        mListener!!.onDeviceConnected()
                    }
                    subscribeToMessages()
                }
    }

    private fun subscribeToMessages() {
        val values = Observable.interval(1000, TimeUnit.MILLISECONDS)
        mBluetoothMessagesSubscription = values
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { data: Long ->
                            Timber.d("Fake data received %d", data)
                            if (mListener != null) {
                                mListener!!.onDataReceived(data.toString())
                            }
                        },
                        { e: Throwable -> println("Error: $e") }
                ) { println("Completed") }
    }
}