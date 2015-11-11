package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener, BlueToothManager.BlueToothManagerListener {

    public static final String TAG = "GyroTest";
    private static final int AXIS_X = 0;
    private static final int AXIS_Y = 1;
    private static final int AXIS_Z = 2;

    private static final int START_ANGLE = 90;

    public static final String STEERING_SYMBOL = "s";
    public static final String ROBOT_SYMBOL = "r";
    public static final String DRIVE_SYMBOL = "d";
    public static final String DRIVE_FAST_FORWARD = "3";
    public static final String DRIVE_FORWARD = "1";
    public static final String DRIVE_BACK = "2";
    public static final String DRIVE_FALSE = "0";

    private BlueToothSyncManager mBlueToothManager;

    private SensorManager mSensorManager;
    private Sensor mAccelerometerSensor;

    private TextView mXValueText;
    private TextView mYValueText;
    private TextView mZValueText;
    private TextView mAngleValueText;
    private TextView mArduinoDataTextView;
    private View mContentLayout;
    private ProgressBar mProgressBar;
    private Button mRetryButton;

    private Button mRunButton;
    private Button mRunFastButton;
    private Button mRunBackButton;
    private Button mRobotButton;

    private int mAverageAngle;
    private boolean mRobotMode;

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
        mContentLayout.setVisibility(View.GONE);
        mRetryButton.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
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
    public void onBlueToothReady() {
        Log.d(TAG, "BlueTooth Ready");
        mArduinoDataTextView.setText("BlueTooth Ready");
    }

    @Override
    public void onDeviceConnected() {
        Log.d(TAG, "Device Connected");
        mArduinoDataTextView.setText("Device Connected");
        mProgressBar.setVisibility(View.GONE);
        mRetryButton.setVisibility(View.GONE);
        mContentLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDeviceDisconnected() {
        Log.d(TAG, "Device Disconnected");
        mArduinoDataTextView.setText("Device Disconnected");
        mProgressBar.setVisibility(View.GONE);
        mContentLayout.setVisibility(View.GONE);
        mRetryButton.setVisibility(View.VISIBLE);

    }

    @Override
    public void onDataReceived(String data) {
        Log.d(TAG, "Data Received: " + data);
        mArduinoDataTextView.setText("Data Received: " + data);
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
        mContentLayout = findViewById(R.id.content_layout);
        mXValueText = (TextView) findViewById(R.id.value_x);
        mYValueText = (TextView) findViewById(R.id.value_y);
        mZValueText = (TextView) findViewById(R.id.value_z);
        mAngleValueText = (TextView) findViewById(R.id.angle_value);
        mArduinoDataTextView = (TextView) findViewById(R.id.arduino_data);

        mRunButton = (Button) findViewById(R.id.run_button);
        mRunButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        setDrive(true);
                        break;
                    case MotionEvent.ACTION_UP:
                        setDrive(false);
                        break;
                }
                return false;
            }
        });

        mRunFastButton = (Button) findViewById(R.id.run_fast_button);
        mRunFastButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        setDriveFast();
                        break;
                    case MotionEvent.ACTION_UP:
                        setDrive(false);
                        break;
                }
                return false;
            }
        });

        mRunBackButton = (Button) findViewById(R.id.run_back_button);
        mRunBackButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        setDriveBack();
                        break;
                    case MotionEvent.ACTION_UP:
                        setDrive(false);
                        break;
                }
                return false;
            }
        });

        mRobotButton = (Button) findViewById(R.id.robot_button);
        mRobotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleRobotMode();
            }
        });

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mRetryButton = (Button) findViewById(R.id.retry);
        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Retry");
                mBlueToothManager.start();
                mRetryButton.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
            }
        });
    }


    private void initSensors() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    /**
     * Takes angle of device rotation adn sends angle that should be applied to servo
     * e.g. average last 2 angles plus start servo point (90 deg.)
     * also applying limit to rotation (/3.4) to avoid wide rotation
     *
     * @param angle angle of device
     */
    private void setAngle(int angle) {
        if (mAverageAngle != angle) {
            mAverageAngle = (mAverageAngle + angle) / 2;
            mAngleValueText.setText(mAverageAngle + " deg.");
            int angleToSend = START_ANGLE - (int) (mAverageAngle / 3.4f);
            mBlueToothManager.sendData(STEERING_SYMBOL + String.valueOf(angleToSend));
        }
    }

    private void setDrive(boolean isDrive) {
        String message = DRIVE_SYMBOL + (isDrive ? DRIVE_FORWARD : DRIVE_FALSE);
        mBlueToothManager.sendData(message);
    }

    private void setDriveFast() {
        mBlueToothManager.sendData(DRIVE_SYMBOL + DRIVE_FAST_FORWARD);
    }

    private void setDriveBack() {
        mBlueToothManager.sendData(DRIVE_SYMBOL + DRIVE_BACK);
    }

    private void toggleRobotMode() {
        mRobotMode = !mRobotMode;
        if (mRobotMode) {
            mSensorManager.unregisterListener(this);
        } else {
            mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        mRunButton.setEnabled(!mRobotMode);
        mRunFastButton.setEnabled(!mRobotMode);
        mRunBackButton.setEnabled(!mRobotMode);
        mBlueToothManager.sendData(ROBOT_SYMBOL);
    }
}
