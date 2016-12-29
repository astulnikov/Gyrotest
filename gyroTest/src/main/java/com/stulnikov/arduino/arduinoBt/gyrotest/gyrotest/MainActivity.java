package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest;

import android.content.Intent;
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

import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.bluetooth.BlueToothManager;
import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.bluetooth.BlueToothSyncManager;
import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.sensor.SensorController;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements MainView {

    public static final String TAG = "GyroTest";

    private MainPresenter mMainPresenter;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        if (mMainPresenter == null) {
            mMainPresenter = new MainPresenter();
        }
        mMainPresenter.bindView(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mContentLayout.setVisibility(View.GONE);
        mRetryButton.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        mMainPresenter.setBluetoothManager(new BlueToothSyncManager(this));
        mMainPresenter.setSensorController(new SensorController(this));
        mMainPresenter.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMainPresenter.stop();
    }

    @Override
    protected void onDestroy() {
        mMainPresenter.unbindView();
        super.onDestroy();
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
    public void showReceivedData(String data) {
        Log.d(TAG, "Data Received: " + data);
        mArduinoDataTextView.setText("Data Received: " + data);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BlueToothManager.REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                mMainPresenter.start();
            } else {
                onDeviceDisconnected();
            }
        }
    }

    @Override
    public void toggleRobotMode(boolean robotMode) {
        mRunButton.setEnabled(!robotMode);
        mRunFastButton.setEnabled(!robotMode);
        mRunBackButton.setEnabled(!robotMode);
    }

    @Override
    public void showAngle(float angle) {
        mAngleValueText.setText(angle + " deg.");
    }

    @Override
    public void showX(float x) {
        mXValueText.setText(String.format(Locale.getDefault(), "%1.2f", x));
    }

    @Override
    public void showY(float y) {
        mYValueText.setText(String.format(Locale.getDefault(), "%1.2f", y));
    }

    @Override
    public void showZ(float z) {
        mZValueText.setText(String.format(Locale.getDefault(), "%1.2f", z));
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
                        mMainPresenter.setDrive(true);
                        break;
                    case MotionEvent.ACTION_UP:
                        mMainPresenter.setDrive(false);
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
                        mMainPresenter.setDriveFast();
                        break;
                    case MotionEvent.ACTION_UP:
                        mMainPresenter.setDrive(false);
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
                        mMainPresenter.setDriveBack();
                        break;
                    case MotionEvent.ACTION_UP:
                        mMainPresenter.setDrive(false);
                        break;
                }
                return false;
            }
        });

        mRobotButton = (Button) findViewById(R.id.robot_button);
        mRobotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainPresenter.toggleRobotMode();
            }
        });

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mRetryButton = (Button) findViewById(R.id.retry);
        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Retry");
                mMainPresenter.start();
                mRetryButton.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
            }
        });
    }
}
