package com.robot.cmpass.compass;

import android.app.Activity;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;


public class MainActivity extends Activity implements SensorEventListener, View.OnClickListener {

    private SensorManager mSensorManager;
    private Sensor mCompass;
    private TextView distanceText;
    private TextView orientText;
    private TextView angleText;
    private String direct = "l";
    private TextView logText;
    private Button btn_stop = null;
    private Button btn_cal = null;
    private EditText valueEdit = null;
    private int v = 90;
    private NumberPicker picker = null;
    private boolean stoped = true;
    Robot robot;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mCompass = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        logText = (TextView) findViewById(R.id.log);
        distanceText = (TextView) findViewById(R.id.distance);
        angleText = (TextView) findViewById(R.id.angle);
        orientText = (TextView) findViewById(R.id.orientation);
        btn_stop  = (Button) findViewById(R.id.btn_stop);
        btn_cal  = (Button) findViewById(R.id.cal_btn);
        picker = (NumberPicker) findViewById(R.id.numberPicker);

        picker.setValue(110);
        picker.setMaxValue(359);
        picker.setMinValue(0);

        btn_stop.setOnClickListener(this);
        btn_cal.setOnClickListener(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        robot = new Robot();
    }

    // The following method is required by the SensorEventListener interface;
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private float orientation;
    // The following method is required by the SensorEventListener interface;
// Hook this event to process updates;
    public void onSensorChanged(SensorEvent event) {

        orientation = Math.round(event.values[0]);

        // The other values provided are:
        //  float pitch = event.values[1];
        //  float roll = event.values[2];
        orientText.setText(orientation + " ");
        distanceText.setText(robot.getDistance() + "");
        angleText.setText(robot.getAngle() + " ");
        if (!robot.getDirection().equals(direct)) {
            logText.setText(robot.getDirection() + " | " +
                    robot.getDistance() + " | " +
                    robot.getAngle() + " | " +
                    orientation + "\n" + logText.getText());
            direct = robot.getDirection();
        }

        if (stoped) {
            return;
        }
        if(robot.connect()) {
            if (Math.abs(orientation - v) < 10 ) {
                robot.forward();
            } else if (Math.abs(orientation - v) < 180 && orientation > v) {
                robot.left();
            } else {
                robot.right();
            }
        }
    }

    @Override
    protected void onPause() {
        // Unregister the listener on the onPause() event to preserve battery life;
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mCompass, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onClick(View view) {
        if (view == btn_stop) {
            if (robot.connect())
                robot.stop();
            v = picker.getValue();
            stoped = !stoped;
            if (stoped) {
                btn_stop.setText("Start");
            } else
                btn_stop.setText("Stop");
        } else if (view == btn_cal) {
            picker.setValue((int)orientation);
        }
    }
}
