package com.example.watch;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SystemInformationLogger implements LifecycleObserver {
    public static final String TAG = "watch:SystemInformationLogger";
    static final public SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault());

    private Context context;
    private File outputFile;
    FileWriter writer;

    public SystemInformationLogger(Context context, File systemInformationFile) {
        this.context = context;
        this.outputFile = systemInformationFile;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void run() {
        try {
            writer = new FileWriter(outputFile);

            // TODO: add more information
            SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
            writer.write(sensorList.toString());

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
