package com.example.watch;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.SystemClock;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SystemInformationLogger implements LifecycleObserver {
    private Context context;
    private File outputFile;
    FileWriter writer;
    static final public SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault());


    public SystemInformationLogger(Context context, File systemInformationFile) {
        this.context = context;
        this.outputFile = systemInformationFile;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void run() {
        try {
            writer = new FileWriter(outputFile);

            SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
            writer.write(sensorList.toString());

            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
