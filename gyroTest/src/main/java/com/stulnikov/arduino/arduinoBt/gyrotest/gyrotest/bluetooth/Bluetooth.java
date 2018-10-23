package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.bluetooth;

import android.bluetooth.BluetoothSocket;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * @author alexeystulnikov 12/29/16.
 */

public class Bluetooth implements BluetoothController {

    private static final String MAC_ADDRESS = "98:D3:31:B1:79:C0"; // BT module MAC-address
    private static final Character CHECK_SYMBOL = 'c';
    private final static Character END_LINE_SYMBOL = 'l';
    private BluetoothConnection mBluetoothConnection;
    private Subscription mBluetoothMessagesSubscription;
    private BlueToothManagerListener mListener;

    private Subscriber<? super String> mSendSubscriber;


    @Override
    public void sendData(String data) {
        if (mSendSubscriber != null) {
            mSendSubscriber.onNext(CHECK_SYMBOL + data + END_LINE_SYMBOL);
        }
    }

    @Override
    public void setListener(BlueToothManagerListener listener) {
        mListener = listener;
    }

    @Override
    public void start() {
        connect();
    }

    @Override
    public void stop() {
        if (mBluetoothConnection != null) {
            if (mBluetoothMessagesSubscription != null) {
                mBluetoothMessagesSubscription.unsubscribe();
            }
            mBluetoothConnection.unsubscribeFromInputData();
            mBluetoothConnection.closeConnection();
        }
    }

    private void connect() {
        BluetoothConnector bluetoothConnector = new BluetoothConnector(MAC_ADDRESS);
        bluetoothConnector.getConnectorObserver()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bluetoothSocket -> {
                    Timber.d("socket received");
                    if (mListener != null) {
                        mListener.onDeviceConnected();
                    }
                    subscribeToMessages(bluetoothSocket);
                    Observable<String> sendObservable = Observable.create(new Observable.OnSubscribe<String>() {
                        @Override
                        public void call(Subscriber<? super String> subscriber) {
                            mSendSubscriber = subscriber;
                        }
                    });
                    mBluetoothConnection.subscribeToInputData(sendObservable);
                }, throwable -> {
                    Timber.e(throwable, "Error occurred %s", throwable.toString());
                    if (mListener != null) {
                        if (throwable instanceof BluetoothMissingException) {
                            mListener.onBluetoothMissing();
                        } else if (throwable instanceof BluetoothDisabledException) {
                            mListener.onBluetoothDisabled();
                        } else {
                            mListener.onDeviceDisconnected();
                        }
                    }
                }, () -> Timber.d("Connection mission completed"));
    }

    private void subscribeToMessages(BluetoothSocket bluetoothSocket) {
        try {
            mBluetoothConnection = new BluetoothConnection(bluetoothSocket);
            mBluetoothMessagesSubscription = mBluetoothConnection.getStringStreamObservable(END_LINE_SYMBOL)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(data -> {
                        Timber.d("Data received");
                        if (mListener != null) {
                            mListener.onDataReceived(data);
                        }
                    }, throwable -> {
                        Timber.e(throwable, "Error occurred");
                        if (mListener != null) {
                            mListener.onDeviceDisconnected();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
