package com.robot.gesture.gesture;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created by amine on 11/6/15.
 */
public class Robot {
    private static final String LEFT = "l";
    private static final String RIGHT = "r";
    private static final String BACK = "b";
    private static final String FRONT = "f";
    private static final String STOP = "s";

    private final UUID UUID_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private int distance = 300;
    private float angle = 300;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mDevice = null;
    private BluetoothSocket mSocket = null;
    private Handler mHandler = null;
    private String lastCmd = LEFT;
    private ConnectedDevice mConnected;
    private boolean _isConnect = false;
    private int distance2 = 100;

    public Robot()  {

    }

    public String getDirection() {
        return lastCmd;
    }
    public int getDistance() {
        return distance;
    }


    public int getDistance2() {
        return distance2;
    }
    public boolean connect () {
        if (_isConnect) {
            return true;
        };
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null)
            return false;

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                // mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                if (device.getAddress().equals("20:14:10:29:20:53")) {
                    BluetoothSocket tmp = null;
                    mDevice = device;
                    try {
                        tmp = device.createRfcommSocketToServiceRecord(UUID_SPP);
                    } catch (IOException e) {
                        return false;
                    }
                    mBluetoothAdapter.cancelDiscovery();
                    mSocket = tmp;

                    try {
                        mSocket.connect();
                    } catch (IOException connectException) {
                        try {
                            mSocket.close();
                        } catch (IOException closeException) { }
                        return false;
                    }
                    mConnected = new ConnectedDevice(mSocket);
                    mConnected.start();
                    mConnected.setPriority(Thread.MAX_PRIORITY);
                    createHandler();
                    Log.i("Amine", "Connected");
                    _isConnect = true;
                    return true;
                }
            }
        }
        return false;
    }

    private void createHandler() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                byte[] buffer= (byte[]) msg.obj;
                float v = 200;
                int ll = buffer[1] & 0xFF;

                if (buffer[2] == 3 && buffer[0] == 3) {
                    distance2 = ll;
                }

                if (buffer[2] == 1 && buffer[0] == 1) {
                    distance = ll;
                }
                if (buffer[2] == 2 && buffer[0] == 2) {
                    angle = ll;
                }
            }
        };
    }



    void speed (int v) {
        byte bytes[] = new byte[1];
        bytes[0] = (byte)v;
        mConnected.write("S".getBytes());
        mConnected.write(bytes);

    }

    void servo_left () {
        mConnected.write("m".getBytes());

    }


    void servo_center () {
        mConnected.write("c".getBytes());

    }

    void servo_right () {
        mConnected.write("o".getBytes());

    }
    void stop() {
        if (true) {
            mConnected.write(STOP.getBytes());
            lastCmd = STOP;
        }
    }

    void left() {
        if (true) {
            mConnected.write(LEFT.getBytes());
            lastCmd = LEFT;
        }
    }

    void updateDistance() {
        mConnected.write("d".getBytes());
    }

    void updateAngle() {
        mConnected.write("a".getBytes());
    }

    void right() {
        if (true) {
            mConnected.write(RIGHT.getBytes());
            lastCmd = RIGHT;
        }
    }

    void forward () {
        if (true) {
            mConnected.write(FRONT.getBytes());
            lastCmd = FRONT;
        }
    }

    void backward() {
        if (lastCmd != BACK) {
            mConnected.write(BACK.getBytes());
            lastCmd = BACK;
        }
    }

    public float getAngle() {
        return angle;
    }

    public class ConnectedDevice extends Thread{
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        public ConnectedDevice(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[10];
            int begin = 0;
            int bytes = 0;
            while (true) {
                try {
                    bytes = mmInStream.read(buffer, 0, 10);
                    for (int i = 1; i < 9; i++) {
                        if (buffer[i - 1] == 1 && buffer[i + 1] == 1)
                            distance = buffer[i];
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

}