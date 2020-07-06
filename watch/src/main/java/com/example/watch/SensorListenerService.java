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
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class SensorListenerService extends Service implements LifecycleOwner {
    private static final String TAG = MainActivity.TAG; //"watch:SensorListenerService";
    private LifecycleRegistry lifecycleRegistry;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "SensorListenerService.onStartCommand() is called.");
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "SensorListenerService.onCreate() is called.");

        lifecycleRegistry = new LifecycleRegistry(this);
        lifecycleRegistry.markState(Lifecycle.State.CREATED);

        this.getLifecycle().addObserver(new SensorLogger(this));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "SensorListenerService.onDestroy() is called.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "SensorListenerService.onBind() is called.");
        return null;
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return lifecycleRegistry;
    }

}