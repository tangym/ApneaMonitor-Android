package com.example.watch;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Locale;


public class MainActivity extends WearableActivity implements LifecycleOwner {
    public static final String TAG = "watch:MainActivity";
    public static final File appDirectory = new File( "sdcard/sleepacc" );
    public static final File dataDirectory = new File(appDirectory +"/data");
    public static final File logDirectory = new File( appDirectory + "/log" );
    public static final File batteryStatusFile = new File( logDirectory, "battery.csv" );
    public static final File logFile = new File( logDirectory, "logcat.txt" );
    public static final File systemInformationFile = new File(logDirectory, "systemInfo.txt");

    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault());

    private LifecycleRegistry lifecycleRegistry;
    private PowerManager.WakeLock wakeLock;

    private Button buttonStart;
    private Button buttonStop;
    private TextView textView;
    private boolean isRunning;
    private Intent sensorListenerIntent = null;

//    private long bootTime = ((new Date()).getTime() - SystemClock.elapsedRealtime()) * 1000000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if ( !appDirectory.exists() ) {
            appDirectory.mkdir();
        }
        if ( !dataDirectory.exists() ) {
            dataDirectory.mkdir();
        }
        if ( !logDirectory.exists() ) {
            logDirectory.mkdir();
        }

        // Prevent CPU halt after screen lock
        setAmbientEnabled();
        Log.i(TAG, "Partial wake lock acquired.");
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "watch:SensorLoggerWakelockTag");
        wakeLock.acquire();
        // Disable doze mode for this app
        Log.i(TAG, "Battery optimization ignored.");
        boolean isIgnoringBatteryOptimizations = powerManager.isIgnoringBatteryOptimizations(getPackageName());
        if (!isIgnoringBatteryOptimizations) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }

        // Register lifecycle-aware components
        lifecycleRegistry = new LifecycleRegistry(this);
        lifecycleRegistry.markState(Lifecycle.State.CREATED);

        this.getLifecycle().addObserver(new LogcatLogger(logFile));
        this.getLifecycle().addObserver(new SystemInformationLogger(this, systemInformationFile));
        this.getLifecycle().addObserver(new BatteryLogger(this, batteryStatusFile));

        Log.d(TAG, "MainActivity.onCreate() is called.");

        // Initialize other variables
        isRunning = false;
        buttonStart = findViewById(R.id.buttonStart);
        buttonStop = findViewById(R.id.buttonStop);
        textView = findViewById(R.id.textView);
        buttonStop.setEnabled(false);

        buttonStart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                buttonStart.setEnabled(false);
                buttonStop.setEnabled(true);
                Log.d(TAG, "Start button clicked.");
                // Start a background service to collect sensor data
                sensorListenerIntent = new Intent(getApplicationContext(), SensorListenerService.class);
                startForegroundService(sensorListenerIntent);
                startService(sensorListenerIntent);
                isRunning = true;
                return true;
            }
        });
        buttonStop.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                buttonStop.setEnabled(false);
                buttonStart.setEnabled(true);
                if (sensorListenerIntent != null) {
                    stopService(sensorListenerIntent);
                    sensorListenerIntent = null;
                }
                isRunning = false;
                return true;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "MainActivity.onDestroy() is called.");
        wakeLock.release();
        lifecycleRegistry.markState(Lifecycle.State.DESTROYED);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "MainActivity.onStart() is called.");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "MainActivity.onResume() is called.");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "MainActivity.onPause() is called.");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "MainActivity.onStop() is called.");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "MainActivity.onRestart() is called.");
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return lifecycleRegistry;
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        textView.setText("Ambient");
        textView.setTextColor(Color.GREEN);
        textView.getPaint().setAntiAlias(false);
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
        textView.setText("Active");
        textView.setTextColor(Color.WHITE);
        textView.getPaint().setAntiAlias(true);
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
    }

}

