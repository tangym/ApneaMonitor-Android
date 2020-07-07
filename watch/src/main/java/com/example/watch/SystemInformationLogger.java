package com.example.watch;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Environment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

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

        outputFile = new File(outputDirectory, "os.txt");
        logOSInfo(outputFile);
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

    public void logOSInfo(File outputFile) {
        String message = "";
        try {
            // TODO: add more information
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            message += "Package name: " + context.getPackageName();
            message += "\nVersion name: " + packageInfo.versionName;
            message += "\nVersion code: " + packageInfo.versionCode;
        } catch ( PackageManager.NameNotFoundException nnfe) {
            nnfe.printStackTrace();
        }

        message += "\nOS version: " + System.getProperty("os.version") + " (" + Build.VERSION.INCREMENTAL + ")";
        message += "\nAPI level: " + Build.VERSION.SDK;
        message += "\nDevice: " + Build.DEVICE;
        message += "\nModel: " + Build.MODEL;
        message += "\nProduct: " + Build.PRODUCT;
        message += "\nManufacturer: " + Build.MANUFACTURER;
        message += "\nTags: " + Build.TAGS;

        message += "\nScreen: " + ((Activity) context).getWindowManager().getDefaultDisplay().getWidth() + " x "
                + ((Activity) context).getWindowManager().getDefaultDisplay().getHeight();
        message += "\nSD card state: " + Environment.getExternalStorageState();

        Properties properties = System.getProperties();
        Enumeration keys = properties.keys();
        String key = "";
        while (keys.hasMoreElements()) {
            key = (String) keys.nextElement();
            message += "\n > " + key + " = " + (String) properties.get(key);
        }

        try {
            FileWriter writer = new FileWriter(outputFile);
            writer.write(message);
            writer.close();
        } catch ( IOException ioe ) {
            ioe.printStackTrace();
        }
    }
}
