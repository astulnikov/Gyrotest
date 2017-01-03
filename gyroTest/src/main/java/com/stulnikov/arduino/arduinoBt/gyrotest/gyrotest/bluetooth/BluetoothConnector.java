package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.UUID;

import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;

/**
 * @author alexeystulnikov 12/29/16.
 */

public class BluetoothConnector {
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private String mMacAddress;
    private BluetoothAdapter mBluetoothAdapter;

    public BluetoothConnector(String macAddress) {
        mMacAddress = macAddress;
    }

    public Observable<BluetoothSocket> getConnectorObserver() {
        return Observable.create(new Observable.OnSubscribe<BluetoothSocket>() {
            @Override
            public void call(Subscriber<? super BluetoothSocket> subscriber) {
                if (!initBtAdapter(subscriber)) {
                    Timber.d("Bluetooth init failed. Return.");
                    return;
                }

                Timber.i("***Connect to device with address : %s***", mMacAddress);
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mMacAddress);
                Timber.i("***Got remote  Device***" + device.getName());
                BluetoothSocket socket = null;

                try {
                    socket = device.createRfcommSocketToServiceRecord(SPP_UUID);
                    Timber.i("***Create socket***");
                } catch (IOException e) {
                    subscriber.onError(e);
                }

                Timber.i("***Cancel device discovery***");
                mBluetoothAdapter.cancelDiscovery();

                if (socket != null) {
                    try {
                        Timber.i("***Connecting...***");
                        socket.connect();
                    } catch (IOException e) {
                        try {
                            socket.close();
                        } catch (IOException e2) {
                            subscriber.onError(e2);
                        }
                        subscriber.onError(e);
                        return;
                    }

                    if (subscriber.isUnsubscribed()) {
                        try {
                            socket.close();
                        } catch (Exception e) {
                            subscriber.onError(e);
                        }
                    }
                }
                Timber.i("***Connection successful***");
                subscriber.onNext(socket);
                subscriber.onCompleted();
            }
        });
    }


    private boolean initBtAdapter(Subscriber<? super BluetoothSocket> subscriber) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isEnabled()) {
                Timber.d("Bluetooth enabled. Fine.");
                return true;
            } else {
                Timber.d("Bluetooth disabled. Need to enable.");
                subscriber.onError(new BluetoothDisabledException());
                return false;
            }
        } else {
            Timber.d("Bluetooth missing.");
            subscriber.onError(new BluetoothMissingException());
            return false;
        }
    }
}
