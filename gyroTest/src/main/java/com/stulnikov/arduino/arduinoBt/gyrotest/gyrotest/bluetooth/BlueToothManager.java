package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.bluetooth;

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

public class BlueToothManager implements BlueToothController {

    public static final int REQUEST_ENABLE_BT = 1;
    private static final String TAG = "BlueToothManager";
    private static final int ARDUINO_DATA = 1;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String MAC_ADDRESS = "98:D3:31:B1:79:C0"; // BT module MAC-address

    private Activity mActivity;
    protected BlueToothManagerListener mListener;

    private Handler mMessageHandler;
    private ConnectedThread mConnectThread;

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;

    private boolean mConnected;

    public BlueToothManager(Activity activity) {
        mActivity = activity;

        mMessageHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case ARDUINO_DATA:
                        byte[] readBuf = (byte[]) msg.obj;
                        String strIn = new String(readBuf, 0, msg.arg1);
                        parseMessage(strIn);
                        break;
                }
            }
        };
    }

    @Override
    public void setListener(BlueToothManagerListener listener) {
        this.mListener = listener;
    }

    public boolean isConnected() {
        return mConnected;
    }

    @Override
    public void start() {
        if (btAdapter != null && btAdapter.isEnabled()) {
            DiscoveryThread discoveryThread = new DiscoveryThread();
            discoveryThread.start();
        } else {
            initBtAdapter();
        }
    }

    @Override
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
                showError("Fatal Error", "In onPause() Can't close socket" + e.getMessage() + ".");
            }
        }
    }

    @Override
    public void sendData(String data) {
        if (isConnected()) {
            mConnectThread.sendData(data);
        }
    }

    protected void parseMessage(String message) {
        Log.i(TAG, "Data from Arduino: " + message);
        mListener.onDataReceived(message);
    }

    private void connect() {
        DiscoveryThread discoveryThread = new DiscoveryThread();
        discoveryThread.start();
    }

    private void initBtAdapter() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter != null) {
            if (btAdapter.isEnabled()) {
                Log.i(TAG, "Bluetooth enabled. Fine.");
                mListener.onBlueToothReady();
                connect();
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                mActivity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        } else {
            showError("Fatal Error", "Bluetooth MISSING");
        }
    }

    private void showError(String title, String message) {
        Log.e(TAG, message);
        Toast.makeText(mActivity, title + " - " + message + ". Finishing.", Toast.LENGTH_LONG).show();
        mActivity.finish();
    }

    private class DiscoveryThread extends Thread {

        @Override
        public void run() {
            btAdapter.startDiscovery();
            BluetoothDevice device = btAdapter.getRemoteDevice(MAC_ADDRESS);
            Log.d(TAG, "***Got remote  Device***" + device.getName());
            try {
                btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                Log.d(TAG, "...Create socket...");
                if (btSocket != null) {
                    btAdapter.cancelDiscovery();
                    Log.d(TAG, "***Cancel device discovery***");
                    Log.d(TAG, "***Connecting...***");
                    mConnectThread = new ConnectedThread(btSocket);
                    btSocket.connect();
                    Log.d(TAG, "***Connection successful***");
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
                    if (inStream.available() > 1) {
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
            Log.d(TAG, "***Sending data: " + message + "***");

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
}
