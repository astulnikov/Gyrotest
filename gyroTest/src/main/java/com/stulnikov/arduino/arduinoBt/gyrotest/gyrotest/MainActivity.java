package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest;

import android.content.Context;
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

public class MainActivity extends ActionBarActivity implements SensorEventListener {

    public static final String TAG = "GyroTest";
    private static final int AXIS_X = 0;
    private static final int AXIS_Y = 1;
    private static final int AXIS_Z = 2;

    SensorManager mSensorManager;
    Sensor mAccelerometerSensor;

    TextView mXValueText;
    TextView mYValueText;
    TextView mZValueText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initSensors();
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
                mXValueText.setText(String.format("%1f", event.values[AXIS_X] * 10));
                mYValueText.setText(String.format("%1f", event.values[AXIS_Y] * 10));
                mZValueText.setText(String.format("%1f", event.values[AXIS_Z] * 10));

//                mXValueText.setText(String.format("%1.3f", event.values[AXIS_X]));
//                mYValueText.setText(String.format("%1.3f", event.values[AXIS_Y]));
//                mZValueText.setText(String.format("%1.3f", event.values[AXIS_Z]));
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void initViews() {
        mXValueText = (TextView) findViewById(R.id.value_x);
        mYValueText = (TextView) findViewById(R.id.value_y);
        mZValueText = (TextView) findViewById(R.id.value_z);
    }


    private void initSensors() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }
}
