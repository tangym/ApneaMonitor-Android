package com.example.watch;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import com.google.android.gms.dynamic.LifecycleDelegate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.POWER_SERVICE;

public class SensorLogger implements SensorEventListener2, LifecycleObserver {
    public final String TAG = "SensorLogger";
    private Context context = null;
    private SensorManager sensorManager = null;

    int dataBufferSize = 1;
    int dataBufferIndex = 0;
    float[][] dataBuffer = new float[dataBufferSize][6];
    String[] dataSensorTypeBuffer = new String[dataBufferSize];
    String[] dataTimestampBuffer = new String[dataBufferSize];

    SensorConfiguration[] sensors = {
            new SensorConfiguration(android.hardware.Sensor.TYPE_ACCELEROMETER, "ACC", SensorManager.SENSOR_DELAY_UI),
    };

    private FileWriter writer = null;

    public SensorLogger(Context context) {
        Log.d(TAG, "SensorLogger created.");
        for (SensorConfiguration sensor: sensors) {
            Log.d(TAG, sensor.toString());
        }
        this.context = context;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void start() {
        Log.d(TAG, "SensorLogger.initialize() is called on create.");

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        for (SensorConfiguration sensorConfig : sensors) {
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(sensorConfig.type),
                    sensorConfig.delay);
        }

        Log.d(TAG, "Writing to " + MainActivity.dataDirectory);
        String startTime = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "s" + startTime + ".csv";
        try {
            writer = new FileWriter(new File(MainActivity.dataDirectory, fileName));
            for (SensorConfiguration sensor: sensors) {
                writer.write(sensor + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void stop() {
        // TODO: unregister sensors, flush sensor events, flush data to sdcard, free file writer
        Log.d(TAG, "SensorLogger.free() is called on destroy.");
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
    public void onAccuracyChanged(Sensor sensor, int i) {}

    @Override
    public void onFlushCompleted(Sensor sensor) {}


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

    class SensorConfiguration {
        public int type;
        public String name;
        public int delay;
        public SensorConfiguration(int type, String name, int delay) {
            this.type = type;
            this.name = name;
            this.delay = delay;
        }

        @Override
        public String toString() {
            String template = "{%s: %s, %s: %s, %s: %s}";
            return String.format(template,
                    "type", type,
                    "name", name,
                    "delay", delay);
        }
    }
}
