package com.robot.gesture.gesture;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;


public class MainActivity extends Activity implements SensorEventListener, View.OnClickListener {

    private SensorManager mSensorManager;
    private Sensor mCompass;
    private TextView pitchText;
    private TextView orientText;
    private TextView rollText;
    private String direct = "l";
    private TextView logText;
    private Button btn_stop = null;
    private Button btn_cal = null;
    private boolean stoped = true;
    Robot robot;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mCompass = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        logText = (TextView) findViewById(R.id.log);
        pitchText = (TextView) findViewById(R.id.pitch);
        rollText = (TextView) findViewById(R.id.roll);
        orientText = (TextView) findViewById(R.id.orientation);
        btn_stop  = (Button) findViewById(R.id.btn_stop);
        btn_cal  = (Button) findViewById(R.id.cal_btn);

        btn_stop.setOnClickListener(this);
        btn_cal.setOnClickListener(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        robot = new Robot();
    }

    // The following method is required by the SensorEventListener interface;
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private float _orient;
    private float _pitch;
    private float _roll;

    private boolean calib = false;

    // The following method is required by the SensorEventListener interface;
// Hook this event to process updates;
    public void onSensorChanged(SensorEvent event) {

        float orientation = Math.round(event.values[0]);
        float pitch = event.values[1];
        float roll = event.values[2];

        if (calib) {
            _orient = orientation;
            _pitch = pitch;
            _roll = roll;
            calib = false;
        }

        orientText.setText(orientation + " ");
        pitchText.setText(pitch + "");
        rollText.setText(roll + " ");
        if (!robot.getDirection().equals(direct)) {
            logText.setText(robot.getDirection() + " | " +
                    pitch + " | " +
                    roll + " | " +
                    orientation + "\n" + logText.getText());
            direct = robot.getDirection();
        }

        if (Math.abs(pitch - _pitch) < 40 && Math.abs(roll - _roll) < 40) {
            logText.setText("Stop");
        } else {
            if ((roll - _roll) > 40) {
                logText.setText("Left");
            } else if ((roll - _roll) < -40) {
                logText.setText("Right");
            } else if ((pitch - _pitch) > 40)  {
                logText.setText("Forward");
            } else if ((pitch - _pitch) < -40) {
                logText.setText("Backward");
            }
        }

        if (stoped) {
            return;
        }
        if(robot.connect()) {
            if (Math.abs(pitch - _pitch) < 40 && Math.abs(roll - _roll) < 40) {
                robot.stop();
            } else {
                if ((roll - _roll) > 40) {
                    robot.left();
                } else if ((roll - _roll) < -40) {
                    robot.right();
                } else if ((pitch - _pitch) > 40) {
                    robot.forward();
                } else if ((pitch - _pitch) < -40) {
                    robot.backward();
                }
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
            stoped = !stoped;
            if (stoped) {
                btn_stop.setText("Start");
            } else
                btn_stop.setText("Stop");
        } else if (view == btn_cal) {
            calib = true;
        }
    }
}
