package com.example.watch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;



public class MainActivity extends WearableActivity {
    public static final String TAG = "watch:MainActivity";
    public static final File appDirectory = new File( "sdcard/sleepacc" );
    public static final File dataDirectory = new File(appDirectory +"/data");
    public static final File logDirectory = new File( appDirectory + "/log" );
    public static final File batteryStatusFile = new File( logDirectory, "battery.csv" );
    public static final File logFile = new File( logDirectory, "logcat" + System.currentTimeMillis() + ".txt" );
    private PowerManager.WakeLock wakeLock;

    Button buttonStart;
    Button buttonStop;
    boolean isRunning;

    static final public SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault());

//    BufferedWriter batteryStatusWriter;
    FileOutputStream batteryStatusWriter;
    private Intent sensorListenerIntent = null;

//    long bootTime = ((new Date()).getTime() - SystemClock.elapsedRealtime()) * 1000000;
    private BroadcastReceiver mBatteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isLow = intent.getBooleanExtra(BatteryManager.EXTRA_BATTERY_LOW, false);
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
            int powerSource = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            String technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
            int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
            int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);

            // TODO: save data in memory and write to disk on destroy etc.
            String timestamp = dateFormat.format(new Date());
            String template = "%s,%b,%d,%d,%d,%d,%d,%s,%d,%d\n";
            try {
                batteryStatusWriter.write(String.format(template,
                        timestamp, isLow, level, scale, health, powerSource, status, technology, temperature, voltage).getBytes());
//                batteryStatusWriter.flush();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // clear the previous logcat and then write the new one to the file
        try {
            Process process = Runtime.getRuntime().exec("logcat -c");
            process = Runtime.getRuntime().exec("logcat -f " + logFile);
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        Log.d(TAG, "MainActivity.onCreate() is called.");
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
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "watch:SensorLoggerWakelockTag");
        wakeLock.acquire();

        // Initialize battery status log writer
        try {
//            batteryStatusWriter = new BufferedWriter(new FileWriter(batteryStatusFile), 200);
            batteryStatusWriter = new FileOutputStream(batteryStatusFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.registerReceiver(this.mBatteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));


        // Initialize other variables
        isRunning = false;
        buttonStart = (Button)findViewById(R.id.buttonStart);
        buttonStop = (Button)findViewById(R.id.buttonStop);
        buttonStop.setEnabled(false);

        buttonStart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                buttonStart.setEnabled(false);
                buttonStop.setEnabled(true);
                Log.d(TAG, "Start button clicked.");
                sensorListenerIntent = new Intent(getApplicationContext(), SensorListenerService.class);
//                startForegroundService(sensorListenerIntent);
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
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "MainActivity.onRestart() is called.");
    }
}

