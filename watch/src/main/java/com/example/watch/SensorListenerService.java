package com.example.watch;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;


public class SensorListenerService extends Service implements LifecycleOwner {
    private static final String TAG = "watch:SensorListenerService";
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
