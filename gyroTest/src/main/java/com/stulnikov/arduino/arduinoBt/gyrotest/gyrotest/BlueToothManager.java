package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BlueToothManager {

    public static final int REQUEST_ENABLE_BT = 1;
    private static final String TAG = "BlueToothManager";
    private static final int ARDUINO_DATA = 1;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String MAC_ADDRESS = "98:D3:31:B1:79:C0"; // MAC-адрес БТ модуля

    protected Activity mActivity;
    protected BlueToothManagerListener mListener;

    protected Handler mMessageHandler;
    private ConnectedThread mConnectThread;

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;

    private boolean mConnected;

    public BlueToothManager(Activity activity) {
        mActivity = activity;
        if (activity instanceof BlueToothManagerListener) {
            mListener = (BlueToothManagerListener) activity;
        }

        mMessageHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case ARDUINO_DATA:
                        byte[] readBuf = (byte[]) msg.obj;
                        String strIn = new String(readBuf, 0, msg.arg1);
                        Log.i(TAG, "Данные от Arduino: " + strIn);
                        mListener.onDataReceived(strIn);
                        break;
                }
            }
        };
    }

    public boolean isConnected() {
        return mConnected;
    }

    public void start() {
        if (btAdapter != null && btAdapter.isEnabled()) {
            DiscoveryThread discoveryThread = new DiscoveryThread();
            discoveryThread.start();
        } else {
            initBtAdapter();
        }
    }

    private void connect() {
        DiscoveryThread discoveryThread = new DiscoveryThread();
        discoveryThread.start();
    }

    public void stop() {
        if (mConnectThread != null && mConnectThread.status_OutStream() != null) {
            mConnectThread.cancel();
        }
        if (btSocket != null) {
            try {
                btSocket.close();
                mConnected = false;
                mListener.onDeviceDisconnected();
            } catch (IOException e) {
                showError("Fatal Error", "В onPause() Не могу закрыть сокет" + e.getMessage() + ".");
            }
        }
    }

    protected void sendData(String data) {
        if(isConnected()) {
            mConnectThread.sendData(data);
        }
    }

    private void initBtAdapter() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter != null) {
            if (btAdapter.isEnabled()) {
                Log.i(TAG, "Bluetooth включен. Все отлично.");
                mListener.onBlueToothReady();
                connect();
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                mActivity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        } else {
            showError("Fatal Error", "Bluetooth ОТСУТСТВУЕТ");
        }
    }

    private void showError(String title, String message) {
        Log.e(TAG, message);
        Toast.makeText(mActivity, title + " - " + message + ". Завершение.", Toast.LENGTH_LONG).show();
        mActivity.finish();
    }

    private class DiscoveryThread extends Thread {

        @Override
        public void run() {
            btAdapter.startDiscovery();
            BluetoothDevice device = btAdapter.getRemoteDevice(MAC_ADDRESS);
            Log.d(TAG, "***Получили удаленный Device***" + device.getName());
            try {
                btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                Log.d(TAG, "...Создали сокет...");
                if (btSocket != null) {
                    btAdapter.cancelDiscovery();
                    Log.d(TAG, "***Отменили поиск других устройств***");
                    Log.d(TAG, "***Соединяемся...***");
                    mConnectThread = new ConnectedThread(btSocket);
                    btSocket.connect();
                    Log.d(TAG, "***Соединение успешно установлено***");
                    mConnectThread.start();
                    mConnected = true;
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mListener.onDeviceConnected();
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
                mConnected = false;
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onDeviceDisconnected();
                    }
                });
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket copyBtSocket;
        private final OutputStream outStrem;
        private final InputStream inStream;

        public ConnectedThread(BluetoothSocket socket) {
            copyBtSocket = socket;
            OutputStream tmpOut = null;
            InputStream tmpIn = null;
            try {
                tmpOut = socket.getOutputStream();
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            outStrem = tmpOut;
            inStream = tmpIn;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    if(inStream.available() > 1) {
                        bytes = inStream.read(buffer);
                        mMessageHandler.obtainMessage(ARDUINO_DATA, bytes, -1, buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    mConnected = false;
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mListener.onDeviceDisconnected();
                        }
                    });
                    break;
                }
            }
        }

        public void sendData(String message) {
            byte[] msgBuffer = message.getBytes();
            Log.d(TAG, "***Отправляем данные: " + message + "***");

            try {
                outStrem.write(msgBuffer);
            } catch (IOException e) {
                e.printStackTrace();
                mConnected = false;
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onDeviceDisconnected();
                    }
                });
            }
        }

        public void cancel() {
            try {
                copyBtSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public Object status_OutStream() {
            return outStrem;
        }
    }

    interface BlueToothManagerListener {
        public void onBlueToothReady();

        public void onDeviceConnected();

        public void onDeviceDisconnected();

        public void onDataReceived(String data);
    }
}
