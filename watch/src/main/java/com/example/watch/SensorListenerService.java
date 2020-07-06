package com.example.watch;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class SensorListenerService extends Service implements SensorEventListener2 {
    private static final String TAG = MainActivity.TAG; //"watch:SensorListenerService";
    private SensorManager sensorManager = null;
    private FileWriter writer = null;

    int dataBufferSize = 2;
    int dataBufferIndex = 0;
    float[][] dataBuffer = new float[dataBufferSize][6];
    String[] dataSensorTypeBuffer = new String[dataBufferSize];
    String[] dataTimestampBuffer = new String[dataBufferSize];

    SensorConfiguration[] sensors = {
            new SensorConfiguration(android.hardware.Sensor.TYPE_ACCELEROMETER, "ACC", SensorManager.SENSOR_DELAY_UI),
//            new SensorConfiguration(android.hardware.Sensor.TYPE_GYROSCOPE, "GYRO", SensorManager.SENSOR_DELAY_UI),
//            new SensorName(android.hardware.Sensor.TYPE_GYROSCOPE_UNCALIBRATED, "GYRO_UN", SensorManager.SENSOR_DELAY_UI),
//            new SensorName(android.hardware.Sensor.TYPE_MAGNETIC_FIELD, "MAG", SensorManager.SENSOR_DELAY_UI),
//            new SensorName(android.hardware.Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED, "MAG_UN", SensorManager.SENSOR_DELAY_UI),
//            new SensorName(android.hardware.Sensor.TYPE_ROTATION_VECTOR, "ROT", SensorManager.SENSOR_DELAY_UI),
//            new SensorName(android.hardware.Sensor.TYPE_GAME_ROTATION_VECTOR, "GAME_ROT", SensorManager.SENSOR_DELAY_UI)
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "SensorListenerService.onStartCommand() is called.");
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        for (SensorConfiguration sensorConfig : sensors) {
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(sensorConfig.type),
                    sensorConfig.delay);
        }

        Log.d(TAG, "Writing to " + MainActivity.dataDirectory);
        String startTime = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "s" + startTime + ".csv";
        try {
            writer = new FileWriter(new File(MainActivity.dataDirectory, fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

//        return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "SensorListenerService.onCreate() is called.");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "SensorListenerService.onDestroy() is called.");
        sensorManager.flush(this);
        sensorManager.unregisterListener(this);
        flushDataBuffer();
        try {
            writer.flush();
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "SensorListenerService.onBind() is called.");
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent evt) {
//        long timestamp = bootTime + evt.timestamp;
//        long timestamp = (new Date()).getTime();

        String timestamp = MainActivity.dateFormat.format(new Date());
        dataTimestampBuffer[dataBufferIndex] = timestamp;
        int sensorIndex = 0;
        for (sensorIndex = 0; sensorIndex < sensors.length; sensorIndex++) {
            if (evt.sensor.getType() == sensors[sensorIndex].type) {
                dataSensorTypeBuffer[dataBufferIndex] = sensors[sensorIndex].name;
                break;
            }
        }
        for (int i = 0; i < 6; i++) {
            if (i < evt.values.length) {
                dataBuffer[dataBufferIndex][i] = evt.values[i];
            } else {
                dataBuffer[dataBufferIndex][i] = 0.f;
            }
        }

        dataBufferIndex += 1;
        if (dataBufferIndex >= dataBufferSize) {
            flushDataBuffer();
            dataBufferIndex = 0;
        }
    }

    protected void flushDataBuffer() {
        String template = "%s,%s,%f,%f,%f,%f,%f,%f\n";
        try {
            for (int i = 0; i < dataBufferIndex; i++) {
                String timestamp = dataTimestampBuffer[i];
                String sensorType = dataSensorTypeBuffer[i];
                float[] eventValues = dataBuffer[i];
                writer.write(String.format(template, timestamp, sensorType,
                        eventValues[0], eventValues[1], eventValues[2], eventValues[3], eventValues[4], eventValues[5]));
            }
            writer.flush();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    @Override
    public void onFlushCompleted(Sensor sensor) {}


    class SensorConfiguration {
        public int type;
        public String name;
        public int delay;
        public SensorConfiguration(int type, String name, int delay) {
            this.type = type;
            this.name = name;
            this.delay = delay;
        }
    }

}
