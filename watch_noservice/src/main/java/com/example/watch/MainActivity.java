package com.example.watch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.BatteryManager;
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
import java.util.Date;
import java.util.Locale;



public class MainActivity extends WearableActivity implements LifecycleOwner {
    public static final String TAG = "watch:MainActivity";
    public static final File appDirectory = new File( "sdcard/sleepacc" );
    public static final File dataDirectory = new File(appDirectory +"/data");
    public static final File logDirectory = new File( appDirectory + "/log" );
    public static final File batteryStatusFile = new File( logDirectory, "battery.csv" );
    public static final File logFile = new File( logDirectory, "logcat" + System.currentTimeMillis() + ".txt" );
    public static final File systemInformationFile = new File(logDirectory, "systemInfo.txt");
    private LifecycleRegistry lifecycleRegistry;
    private PowerManager.WakeLock wakeLock;

    Button buttonStart;
    Button buttonStop;
    TextView textView;
    boolean isRunning;

    static final public SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault());

    private Intent sensorListenerIntent = null;

//    long bootTime = ((new Date()).getTime() - SystemClock.elapsedRealtime()) * 1000000;

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
        Log.i(TAG, "Battery optimization ignored.");
        boolean isIgnoringBatteryOptimizations = powerManager.isIgnoringBatteryOptimizations(getPackageName());
        if (!isIgnoringBatteryOptimizations) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }

        lifecycleRegistry = new LifecycleRegistry(this);
        lifecycleRegistry.markState(Lifecycle.State.CREATED);

        this.getLifecycle().addObserver(new LogcatLogger(logFile));
        this.getLifecycle().addObserver(new SystemInformationLogger(this, systemInformationFile));
        this.getLifecycle().addObserver(new BatteryLogger(this, batteryStatusFile));

        Log.d(TAG, "MainActivity.onCreate() is called.");
        // Initialize other variables
        isRunning = false;
        buttonStart = (Button) findViewById(R.id.buttonStart);
        buttonStop = (Button) findViewById(R.id.buttonStop);
        textView = (TextView) findViewById(R.id.textView);
        buttonStop.setEnabled(false);

        buttonStart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                buttonStart.setEnabled(false);
                buttonStop.setEnabled(true);
                Log.d(TAG, "Start button clicked.");
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
                buttonStart.setEnabled(true);
                buttonStop.setEnabled(false);
                isRunning = false;
                if (sensorListenerIntent != null) {
                    stopService(sensorListenerIntent);
                    sensorListenerIntent = null;
                }
                return true;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "MainActivity.onDestroy() is called.");
        wakeLock.release();
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
        // TODO: update lifecycleRegister.markState()
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
