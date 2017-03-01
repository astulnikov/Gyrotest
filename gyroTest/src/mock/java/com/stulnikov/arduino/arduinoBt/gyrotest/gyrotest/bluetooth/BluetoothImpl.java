package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.bluetooth;

import java.util.concurrent.TimeUnit;

import rx.Completable;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * @author alexeystulnikov 12/29/16.
 */

public class BluetoothImpl implements BluetoothController {

    private Subscription mBluetoothMessagesSubscription;
    private BlueToothManagerListener mListener;

    @Override
    public void sendData(String data) {
        Timber.d("Send data %s", data);
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
        if (mBluetoothMessagesSubscription != null) {
            mBluetoothMessagesSubscription.unsubscribe();
        }
    }

    private void connect() {
        Timber.d("Imitate connection");
        Completable.complete().delay(2000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Timber.d("Connected");
                    if (mListener != null) {
                        mListener.onDeviceConnected();
                    }
                    subscribeToMessages();
                });
    }

    private void subscribeToMessages() {
        Observable<Long> values = Observable.interval(1000, TimeUnit.MILLISECONDS);
        mBluetoothMessagesSubscription = values
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> {
                            Timber.d("Fake data received %d", data);
                            if (mListener != null) {
                                mListener.onDataReceived(String.valueOf(data));
                            }
                        },
                        e -> System.out.println("Error: " + e),
                        () -> System.out.println("Completed")
                );
    }
}
