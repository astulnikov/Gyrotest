package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.dagger.DaggerPresenterComponent
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class MainActivity : AppCompatActivity(), MainView {

    @Inject
    lateinit var mainPresenter: MainPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        DaggerPresenterComponent
                .builder()
                .applicationComponent((application as GyroTestApp).applicationComponent)
                .build()
                .inject(this)
        mainPresenter.bindView(this)
    }

    override fun onStart() {
        super.onStart()
        content_layout.visibility = View.GONE
        retry.visibility = View.GONE
        progress_bar.visibility = View.VISIBLE
        mainPresenter.start()
    }

    override fun onStop() {
        super.onStop()
        mainPresenter.stop()
    }

    override fun onDestroy() {
        mainPresenter.unbindView()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        return id == R.id.action_settings || super.onOptionsItemSelected(item)
    }

    override fun onDeviceConnected() {
        Timber.d("Device Connected")
        arduino_data.setText(R.string.device_connected)
        progress_bar.visibility = View.GONE
        retry.visibility = View.GONE
        content_layout.visibility = View.VISIBLE
        robot_button.visibility = View.VISIBLE
    }

    override fun onDeviceDisconnected() {
        Timber.d("Device Disconnected")
        arduino_data.setText(R.string.device_disconnected)
        progress_bar.visibility = View.GONE
        content_layout.visibility = View.GONE
        robot_button.visibility = View.GONE
        retry.visibility = View.VISIBLE
    }

    override fun showReceivedData(data: String) {
        if (!TextUtils.isEmpty(data)) {
            arduino_data.text = getString(R.string.data_received, data)
        }
    }

    override fun showEnableBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                mainPresenter.start()
            } else {
                onDeviceDisconnected()
            }
        }
    }

    override fun toggleRobotMode(robotMode: Boolean) {
        run_button.isEnabled = !robotMode
        run_progress.isEnabled = !robotMode
        run_back_button.isEnabled = !robotMode
    }

    override fun showAngle(angle: Float) {
        angle_value.text = getString(R.string.angle, angle)
    }

    override fun showX(x: Float) {
        value_x.text = String.format(Locale.getDefault(), "%1.2f", x)
    }

    override fun showY(y: Float) {
        value_y.text = String.format(Locale.getDefault(), "%1.2f", y)
    }

    override fun showZ(z: Float) {
        value_z.text = String.format(Locale.getDefault(), "%1.2f", z)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initViews() {
        run_button.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> mainPresenter.setDrive(true)
                MotionEvent.ACTION_UP -> mainPresenter.setDrive(false)
            }
            false
        }
        run_progress.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                }
                MotionEvent.ACTION_MOVE -> mainPresenter.setDrivePercent(run_progress.progress)
                MotionEvent.ACTION_UP -> mainPresenter.setDrive(false)
            }
            false
        }
        run_back_button.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> mainPresenter.setDriveBack()
                MotionEvent.ACTION_UP -> mainPresenter.setDrive(false)
            }
            false
        }
        robot_button.setOnClickListener { mainPresenter.toggleRobotMode() }
        retry.setOnClickListener {
            Timber.d("Retry")
            mainPresenter.start()
            retry.visibility = View.GONE
            progress_bar.visibility = View.VISIBLE
        }
    }

    override fun showFrontDistance(level: Int) {
        front_distance.setImageLevel(level)
    }

    override fun showRearDistance(level: Int) {
        rear_distance.setImageLevel(level)
    }

    companion object {
        const val REQUEST_ENABLE_BT = 1
    }
}