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
    private File outputDirectory;

    public SystemInformationLogger(Context context, File sysInfoDirectory) {
        this.context = context;
        this.outputDirectory = sysInfoDirectory;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void run() {
        File outputFile = new File(outputDirectory, "sensors.csv");
        logSensorInfo(outputFile);
    }

    public void logSensorInfo(File outputFile) {
        try {
            FileWriter writer = new FileWriter(outputFile);
            SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
            // writer.write(sensorList.toString());
            writer.write("id,name,typeStr,type,resolution,power,reportingMode,maxDelay,minDelay,maxRange," +
                    "fifoMaxEventCount,fifoReservedEventCount,highestDirectReportRateLevel,vendor,version," +
                    "isAdditionalInfoSupported,isDirectChannelTypeSupported,isDynamicSensor,isWakeUpSensor\n");
            for (Sensor sensor: sensorList) {
                String message = String.format("%d,\"%s\",\"%s\",%d,%f,%f,%d,%d,%d,%f,%d,%d,%d,\"%s\",%d,%b,%b,%b,%b\n",
                        sensor.getId(), sensor.getName(), sensor.getStringType(),
                        sensor.getType(), sensor.getResolution(), sensor.getPower(), sensor.getReportingMode(),
                        sensor.getMaxDelay(), sensor.getMinDelay(), sensor.getMaximumRange(),
                        sensor.getFifoMaxEventCount(), sensor.getFifoReservedEventCount(), sensor.getHighestDirectReportRateLevel(),
                        sensor.getVendor(), sensor.getVersion(), sensor.isAdditionalInfoSupported(),
                        sensor.isDirectChannelTypeSupported(0), sensor.isDynamicSensor(), sensor.isWakeUpSensor());
                writer.write(message);
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
