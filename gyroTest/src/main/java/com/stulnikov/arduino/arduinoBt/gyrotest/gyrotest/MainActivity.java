package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity implements SensorEventListener, BlueToothManager.BlueToothManagerListener {

    public static final String TAG = "GyroTest";
    private static final int AXIS_X = 0;
    private static final int AXIS_Y = 1;
    private static final int AXIS_Z = 2;

    private BlueToothSyncManager mBlueToothManager;

    private SensorManager mSensorManager;
    private Sensor mAccelerometerSensor;

    private int mAverageAngle;

    private TextView mXValueText;
    private TextView mYValueText;
    private TextView mZValueText;
    private TextView mAngleValueText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initSensors();
        mBlueToothManager = new BlueToothSyncManager(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mBlueToothManager.start();
    }

    @Override
    protected void onResume() {

        super.onResume();
        //регистрируем сенсоры в объекты сенсора
        mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {

        //говорим что данные будем получать из этого окласса
        mSensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBlueToothManager.stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.i(TAG, "Sensor Type: " + event.sensor.getName());
        Log.i(TAG, "Sensor Accuracy: " + event.accuracy);
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                mXValueText.setText(String.format("%1.2f", event.values[AXIS_X]));
                mYValueText.setText(String.format("%1.2f", event.values[AXIS_Y]));
                mZValueText.setText(String.format("%1.2f", event.values[AXIS_Z]));
                setAngle((int) event.values[AXIS_Y] * 10);
//                mXValueText.setText(String.format("%1.3f", event.values[AXIS_X]));
//                mYValueText.setText(String.format("%1.3f", event.values[AXIS_Y]));
//                mZValueText.setText(String.format("%1.3f", event.values[AXIS_Z]));
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BlueToothManager.REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                mBlueToothManager = new BlueToothSyncManager(this);
                mBlueToothManager.start();
            } else {
                onDeviceDisconnected();
            }
        }
    }

    private void initViews() {
        mXValueText = (TextView) findViewById(R.id.value_x);
        mYValueText = (TextView) findViewById(R.id.value_y);
        mZValueText = (TextView) findViewById(R.id.value_z);
        mAngleValueText = (TextView) findViewById(R.id.angle_value);
    }


    private void initSensors() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    private void setAngle(int angle) {
        mAverageAngle = (mAverageAngle + angle) / 2;
        mAngleValueText.setText(mAverageAngle + " deg.");
        int angleToSend = 90 + mAverageAngle;
        mBlueToothManager.safeSendData(String.valueOf(angleToSend));
    }

    @Override
    public void onBlueToothReady() {
        Log.d(TAG, "BlueTooth Ready");
    }

    @Override
    public void onDeviceConnected() {
        Log.d(TAG, "Device Connected");
    }

    @Override
    public void onDeviceDisconnected() {
        Log.d(TAG, "Device Disconnected");
    }

    @Override
    public void onDataReceived(String data) {
        Log.d(TAG, "Data Received: " + data);
    }
}
