package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.bluetooth

import android.bluetooth.BluetoothSocket
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.InvalidParameterException
import java.util.*

/**
 * @author alexeystulnikov 12/29/16.
 */
class BluetoothConnection(socket: BluetoothSocket?) {
    private val socket: BluetoothSocket?
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    private var observableInputStream: Flowable<Byte>? = null
    private var inputDataDisposable: Disposable? = null
    private var isConnected = false

    /**
     * Observes byte from bluetooth's [InputStream]. Will be emitted per byte.
     *
     * @return RxJava Observable with [Byte]
     */
    private val byteStreamObservable: Observable<Byte>
        get() {
            if (observableInputStream == null) {
                observableInputStream = Flowable.create({ subscriber ->
                    while (!subscriber.isCancelled) {
                        try {
                            inputStream?.let {
                                subscriber.onNext(it.read().toByte())
                            }
                        } catch (e: IOException) {
                            isConnected = false
                            subscriber.onError(e)
                        } finally {
                            if (!isConnected) {
                                closeConnection()
                            }
                        }
                    }
                }, BackpressureStrategy.BUFFER)
            }
            return observableInputStream!!.toObservable()
        }

    /**
     * Observes string from bluetooth's [InputStream].
     *
     * @param delimiters char(s) used for string delimiter
     * @return RxJava Observable with [String]
     */
    fun getStringStreamObservable(vararg delimiters: Int): Observable<String> {
        return byteStreamObservable.lift { observer ->
            object : Observer<Byte> {
                var buffer = ArrayList<Byte>()
                lateinit var disposable: Disposable

                override fun onSubscribe(newDisposable: Disposable) {
                    disposable = newDisposable
                }

                override fun onNext(aByte: Byte) {
                    var found = false
                    for (delimiter in delimiters) {
                        if (aByte.toInt() == delimiter) {
                            found = true
                            break
                        }
                    }

                    if (found) {
                        emit()
                    } else {
                        buffer.add(aByte)
                    }
                }

                override fun onError(e: Throwable) {
                    if (buffer.isNotEmpty()) {
                        emit()
                    }

                    if (!disposable.isDisposed) {
                        observer.onError(e)
                    }
                }

                override fun onComplete() {
                    if (buffer.isNotEmpty()) {
                        emit()
                    }
                    if (!disposable.isDisposed) {
                        observer.onComplete()
                    }
                }

                private fun emit() {
                    if (buffer.isEmpty()) {
                        if (!disposable.isDisposed) {
                            observer.onNext("")
                        }
                        return
                    }
                    val byteArray = ByteArray(buffer.size)
                    for (i in buffer.indices) {
                        byteArray[i] = buffer[i]
                    }
                    if (!disposable.isDisposed) {
                        observer.onNext(String(byteArray))
                    }
                    buffer.clear()
                }
            }
        }
    }

    fun subscribeToInputData(observable: Observable<String>) {
        inputDataDisposable = observable
                .observeOn(Schedulers.io())
                .subscribe { sendData(it) }
    }

    fun unsubscribeFromInputData() {
        inputDataDisposable?.dispose()
    }

    /**
     * Send string to bluetooth output stream.
     *
     * @param message text to sendData
     */
    private fun sendData(message: String) {
        val sBytes = message.toByteArray()
        sendData(sBytes)
    }

    /**
     * Send array of bytes to bluetooth output stream.
     *
     * @param bytes data to sendData
     */
    private fun sendData(bytes: ByteArray?) {
        if (!isConnected) return
        try {
            Timber.i("***Sending data: %s ***", String(bytes!!))
            outputStream!!.write(bytes)
        } catch (e: IOException) {
            isConnected = false
            Timber.e("Fail to sendData data")
        } finally {
            if (!isConnected) {
                closeConnection()
            }
        }
    }

    /**
     * Close the streams and mSocket connection.
     */
    fun closeConnection() {
        try {
            isConnected = false
            inputStream?.close()
            outputStream?.close()
            socket?.close()
        } catch (e: IOException) {
            Timber.e(e, "Fail to sendData data")
        }
    }

    init {
        if (socket == null) {
            throw InvalidParameterException("Bluetooth mSocket can't be null")
        }
        this.socket = socket
        try {
            inputStream = socket.inputStream
            outputStream = socket.outputStream
            isConnected = true
        } catch (e: IOException) {
            throw Exception("Can't get stream from bluetooth mSocket")
        } finally {
            if (!isConnected) {
                closeConnection()
            }
        }
    }
}