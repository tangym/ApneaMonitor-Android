package com.example.sleepacc;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ScheduledExecutorService;
import java.util.Date;

public class AccSampler extends Service implements SensorEventListener {

    private static final String TAG = "SleepAcc/AccSampler";
    private BufferedWriter outputSensorData;

    private int cntAcc = 0, cntGyro = 0;
    private float timestamp = System.nanoTime();
    private float NANO = 1000000000.0f;
    // private float[] rMatrix = new float[9];

    private final static int SENS_ACCELEROMETER = Sensor.TYPE_ACCELEROMETER;
    private final static int SENS_GYROSCOPE = Sensor.TYPE_GYROSCOPE;

    SensorManager mSensorManager;

    // schedule commands to run after a given delay or periodically
    private ScheduledExecutorService mScheduler;

    @Override
    public void onCreate() {
        super.onCreate();
        startMeasurement();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopMeasurement();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected void startMeasurement()   {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
            float ts = ((System.nanoTime() - timestamp) / NANO);
            Log.w(TAG, "Sampling rate of ACCELEROMETER is: " + Float.toString(cntAcc / ts));
            Log.w(TAG, "Sampling rate of GYROSCOPE is: " + Float.toString(cntGyro / ts));
        }
        if (mScheduler != null && !mScheduler.isTerminated()) {
            mScheduler.shutdown();
        }
        if (outputSensorData != null) {
            try {
                outputSensorData.flush();
                outputSensorData.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Get default sensors
        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        Sensor accelerometerSensor = mSensorManager.getDefaultSensor(SENS_ACCELEROMETER);
        Sensor gyroscopeSensor = mSensorManager.getDefaultSensor(SENS_GYROSCOPE);

        // Register the listener
        if (mSensorManager != null) {
            if (accelerometerSensor != null) {
                mSensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
            } else {
                Log.w(TAG, "No Accelerometer found");
            }
            if (gyroscopeSensor != null) {
//                mSensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
                mSensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_UI);
            } else {
                Log.w(TAG, "No Gyroscope Sensor found");
            }
        }

        String startTime = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(new Date());
        String prefix = "sdcard/sleepacc/s" + startTime;
        try {
            outputSensorData = new BufferedWriter(new FileWriter(prefix));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopMeasurement() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
            float ts = ((System.nanoTime() - timestamp) / NANO);
            Log.w(TAG, "Sampling rate of ACCELEROMETER is: " + Float.toString(cntAcc / ts));
            Log.w(TAG, "Sampling rate of GYROSCOPE is: " + Float.toString(cntGyro / ts));
        }
        if (mScheduler != null && !mScheduler.isTerminated()) {
            mScheduler.shutdown();
        }
        if (outputSensorData != null) {
            try {
                outputSensorData.flush();
                outputSensorData.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.w(TAG, "All sensors stopped!");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == SENS_ACCELEROMETER) {
            cntAcc ++;
            int sensorType = 0;
            try {
                String time = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault()).format(new Date());
                Log.d(TAG, "Received sensor data " + event.sensor.getName() + " at " + time + " = " + Arrays.toString(event.values));
                outputSensorData.append(String.valueOf(sensorType) + "," + String.valueOf(time) + "," + String.valueOf(Arrays.toString(event.values)) + "\n");
                outputSensorData.flush();
            } catch (IOException ie) {
                ie.printStackTrace();
            }
        }

        if (event.sensor.getType() == SENS_GYROSCOPE) {
            cntGyro ++;
            int sensorType = 1;
            try {
                String time = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault()).format(new Date());
                Log.d(TAG, "Received sensor data " + event.sensor.getName() + " at " + time + " = " + Arrays.toString(event.values));
                outputSensorData.append(String.valueOf(sensorType) + "," + String.valueOf(time) + "," + String.valueOf(Arrays.toString(event.values)) + "\n");
                outputSensorData.flush();
            } catch (IOException ie) {
                ie.printStackTrace();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}


