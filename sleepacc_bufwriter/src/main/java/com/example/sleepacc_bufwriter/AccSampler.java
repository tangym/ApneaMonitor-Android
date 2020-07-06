package com.example.sleepacc_bufwriter;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ScheduledExecutorService;
import java.util.Date;

public class AccSampler extends Service implements SensorEventListener {
    private static final String TAG = "SleepAcc/AccSampler";
    private OutputStreamWriter dataWriter;

    int dataBufferSize = 30000;
    int dataBufferIndex = 0;
    float[][] dataBuffer = new float[dataBufferSize][3];
    String[] dataTimestampBuffer = new String[dataBufferSize];

    private final static int SENS_ACCELEROMETER = Sensor.TYPE_ACCELEROMETER;
    private final static int sensorDelay = SensorManager.SENSOR_DELAY_GAME;

    SensorManager mSensorManager;

    // schedule commands to run after a given delay or periodically
    private ScheduledExecutorService mScheduler;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "Buffer size: " + String.valueOf(dataBufferSize));
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
        }
        if (mScheduler != null && !mScheduler.isTerminated()) {
            mScheduler.shutdown();
        }
        if (dataWriter != null) {
            try {
                flushDataBuffer();
                dataWriter.flush();
                dataWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Get default sensors
        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        Sensor accelerometerSensor = mSensorManager.getDefaultSensor(SENS_ACCELEROMETER);

        // Register the listener
        if (mSensorManager != null) {
            if (accelerometerSensor != null) {
                Log.v(TAG, "Sensor delay option is: " + String.valueOf(sensorDelay));
                mSensorManager.registerListener(this, accelerometerSensor, sensorDelay);
            } else {
                Log.w(TAG, "No Accelerometer found");
            }
        }

        String startTime = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(new Date());
        String prefix = "sdcard/sleepacc/s" + startTime;
        try {
            dataWriter = new OutputStreamWriter(new FileOutputStream(prefix));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopMeasurement() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
        if (mScheduler != null && !mScheduler.isTerminated()) {
            mScheduler.shutdown();
        }
        if (dataWriter != null) {
            try {
                flushDataBuffer();
                dataWriter.flush();
                dataWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == SENS_ACCELEROMETER) {
            int sensorType = 0;
            String time = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault()).format(new Date());
            dataTimestampBuffer[dataBufferIndex] = time;
            for (int i = 0; i < 3; i++) {
                dataBuffer[dataBufferIndex][i] = event.values[i];
            }
            dataBufferIndex += 1;

            try {
                if (dataBufferIndex >= dataBufferSize) {
                    for (int i = 0; i < dataBufferSize; i++) {
                        dataWriter.append(String.valueOf(sensorType) + "," + String.valueOf(dataTimestampBuffer[i]) + ","
                                + String.valueOf(Arrays.toString(dataBuffer[i])) + "\n");
                    }
                    dataWriter.flush();
                    dataBufferIndex = 0;
                }
            } catch (IOException ie) {
                ie.printStackTrace();
            }
        }
    }

    protected void flushDataBuffer() {
        try {
            for (int i = 0; i < dataBufferIndex; i++) {
                dataWriter.append(String.valueOf(0) + "," + String.valueOf(dataTimestampBuffer[i]) + ","
                        + String.valueOf(Arrays.toString(dataBuffer[i])) + "\n");
            }
            dataWriter.flush();
            dataBufferIndex = 0;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}


