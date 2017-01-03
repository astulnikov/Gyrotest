package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.bluetooth;

/**
 * @author alexeystulnikov 12/29/16.
 */

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class BluetoothConnection {

    private BluetoothSocket mSocket;

    private InputStream mInputStream;
    private OutputStream mOutputStream;

    private Observable<Byte> mObservableInputStream;
    private Subscription mInputDataSubscription;

    private boolean mIsConnected = false;

    /**
     * Container for simplifying read and write from/to {@link BluetoothSocket}.
     *
     * @param socket bluetooth socket
     * @throws Exception if can't get input/output stream from the mSocket
     */
    public BluetoothConnection(BluetoothSocket socket) throws Exception {
        if (socket == null) {
            throw new InvalidParameterException("Bluetooth mSocket can't be null");
        }

        this.mSocket = socket;

        try {
            mInputStream = socket.getInputStream();
            mOutputStream = socket.getOutputStream();

            mIsConnected = true;
        } catch (IOException e) {
            throw new Exception("Can't get stream from bluetooth mSocket");
        } finally {
            if (!mIsConnected) {
                closeConnection();
            }
        }
    }

    /**
     * Observes byte from bluetooth's {@link InputStream}. Will be emitted per byte.
     *
     * @return RxJava Observable with {@link Byte}
     */
    public Observable<Byte> getByteStreamObservable() {
        if (mObservableInputStream == null) {
            mObservableInputStream = Observable.create(new Observable.OnSubscribe<Byte>() {
                @Override
                public void call(Subscriber<? super Byte> subscriber) {
                    while (!subscriber.isUnsubscribed()) {
                        try {
                            subscriber.onNext((byte) mInputStream.read());
                        } catch (IOException e) {
                            mIsConnected = false;
                            subscriber.onError(e);
                        } finally {
                            if (!mIsConnected) {
                                closeConnection();
                            }
                        }
                    }
                }
            }).share();
        }

        return mObservableInputStream;
    }

    /**
     * Observes string from bluetooth's {@link InputStream} with '\r' (Carriage Return)
     * and '\n' (New Line) as delimiter.
     *
     * @return RxJava Observable with {@link String}
     */
    public Observable<String> getStringStreamObservable() {
        return getStringStreamObservable('\r', '\n');
    }

    /**
     * Observes string from bluetooth's {@link InputStream}.
     *
     * @param delimiters char(s) used for string delimiter
     * @return RxJava Observable with {@link String}
     */
    public Observable<String> getStringStreamObservable(final int... delimiters) {
        return getByteStreamObservable().lift(new Observable.Operator<String, Byte>() {
            @Override
            public Subscriber<? super Byte> call(final Subscriber<? super String> subscriber) {
                return new Subscriber<Byte>(subscriber) {
                    ArrayList<Byte> buffer = new ArrayList<>();

                    @Override
                    public void onCompleted() {
                        if (!buffer.isEmpty()) {
                            emit();
                        }

                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onCompleted();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!buffer.isEmpty()) {
                            emit();
                        }

                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onError(e);
                        }
                    }

                    @Override
                    public void onNext(Byte aByte) {
                        boolean found = false;
                        for (int delimiter : delimiters) {
                            if (aByte == delimiter) {
                                found = true;
                                break;
                            }
                        }

                        if (found) {
                            emit();
                        } else {
                            buffer.add(aByte);
                        }
                    }

                    private void emit() {
                        if (buffer.isEmpty()) {
                            if (!subscriber.isUnsubscribed()) {
                                subscriber.onNext("");
                            }
                            return;
                        }

                        byte[] byteArray = new byte[buffer.size()];

                        for (int i = 0; i < buffer.size(); i++) {
                            byteArray[i] = buffer.get(i);
                        }

                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(new String(byteArray));
                        }
                        buffer.clear();
                    }
                };
            }
        }).onBackpressureBuffer();
    }

    public void subscribeToInputData(Observable<String> observable) {
        mInputDataSubscription = observable
                .observeOn(Schedulers.io())
                .subscribe(this::sendData);
    }

    public void unsubscribeFromInputData() {
        mInputDataSubscription.unsubscribe();
    }

    /**
     * Send string to bluetooth output stream.
     *
     * @param message text to sendData
     */
    public void sendData(String message) {
        byte[] sBytes = message.getBytes();
        sendData(sBytes);
    }

    /**
     * Send array of bytes to bluetooth output stream.
     *
     * @param bytes data to sendData
     */
    public void sendData(byte[] bytes) {
        if (!mIsConnected) return;

        try {
            Timber.i("***Sending data: %s ***", new String(bytes));
            mOutputStream.write(bytes);
        } catch (IOException e) {
            mIsConnected = false;
            Timber.e("Fail to sendData data");
        } finally {
            if (!mIsConnected) {
                closeConnection();
            }
        }
    }

    /**
     * Close the streams and mSocket connection.
     */
    public void closeConnection() {
        try {
            mIsConnected = false;

            if (mInputStream != null) {
                mInputStream.close();
            }

            if (mOutputStream != null) {
                mOutputStream.close();
            }

            if (mSocket != null) {
                mSocket.close();
            }
        } catch (IOException e) {
            Timber.e("Fail to sendData data", e);
        }
    }
}