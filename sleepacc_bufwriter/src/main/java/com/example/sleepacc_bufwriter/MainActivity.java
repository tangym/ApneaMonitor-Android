package com.example.sleepacc_bufwriter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends WearableActivity {
    private TextView mTextView1, mTextView2;
    private Button startButton, stopButton;
    private boolean isWorking = false;
    private Handler mHandler = new Handler();
    private int interval = 0;
    private static final String TAG = "SleepAcc/MainActivity";
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault());
    final File batteryStatusFile = new File( "sdcard/sleepacc", "battery.csv" );
    BufferedWriter batteryStatusWriter;

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

            String timestamp = dateFormat.format(new Date());
            String template = "%s,%b,%d,%d,%d,%d,%d,%s,%d,%d\n";
            try {
                batteryStatusWriter.write(String.format(template,
                        timestamp, isLow, level, scale, health, powerSource, status, technology, temperature, voltage));
//                batteryStatusWriter.flush();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    };

    protected void recordBatteryInfo() {
        Intent intent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        boolean isLow = intent.getBooleanExtra(BatteryManager.EXTRA_BATTERY_LOW, false);
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
        int powerSource = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        String technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
        int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
        int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);

        String timestamp = dateFormat.format(new Date());
        String template = "%s,%b,%d,%d,%d,%d,%d,%s,%d,%d\n";
        try {
            batteryStatusWriter.write(String.format(template,
                    timestamp, isLow, level, scale, health, powerSource, status, technology, temperature, voltage));
            batteryStatusWriter.flush();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // For debugging data missing problem
//        File appDirectory = new File( "sdcard/sleepacc" );
//        File logDirectory = new File( appDirectory + "/log" );
//        File logFile = new File( logDirectory, "logcat" + System.currentTimeMillis() + ".txt" );
//        if ( !appDirectory.exists() ) {
//            appDirectory.mkdir();
//        }
//        if ( !logDirectory.exists() ) {
//            logDirectory.mkdir();
//        }
//        // clear the previous logcat and then write the new one to the file
//        try {
//            Process process = Runtime.getRuntime().exec("logcat -c");
//            process = Runtime.getRuntime().exec("logcat -f " + logFile);
//        } catch ( IOException e ) {
//            e.printStackTrace();
//        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        final PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
        try {
            batteryStatusWriter = new BufferedWriter(new FileWriter(batteryStatusFile), 4096);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.registerReceiver(this.mBatteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView1 = (TextView) stub.findViewById(R.id.text1);
                mTextView2 = (TextView) stub.findViewById(R.id.text2);
                startButton = (Button) findViewById(R.id.bt_start);
                stopButton = (Button) findViewById(R.id.bt_stop);
                mTextView1.setGravity(Gravity.CENTER);
                mTextView2.setGravity(Gravity.CENTER);
                startButton.setEnabled(true);
                stopButton.setEnabled(false);

                class ClickEvent implements View.OnClickListener {
                    public void onClick(View v) {
                        if (v == startButton) {
                            isWorking = true;
                            wakeLock.acquire();
                            mTextView2.setText("It is working!");
                            startButton.setEnabled(false);
                            recordBatteryInfo();
                            startRepeatedSampling();
                            stopButton.setEnabled(true);
                        } else if (v == stopButton) {
                            isWorking = false;
                            wakeLock.release();
                            MainActivity.this.stopService(new Intent(MainActivity.this, AccSampler.class));
                            mTextView2.setText("It is NOT working!");
                            stopButton.setEnabled(false);
                            stopRepeatedSampling();
                            recordBatteryInfo();
                            startButton.setEnabled(true);
                        }
                    }
                }

                startButton.setOnClickListener(new ClickEvent());
                stopButton.setOnClickListener(new ClickEvent());
            }
        });
    }

    Runnable sampler = new Runnable() {
        @Override
        public void run() {
            try {
                if (isWorking) {
                    isWorking = false;
                    MainActivity.this.startService(new Intent(MainActivity.this, AccSampler.class));
                    interval = 3600000;   //sample 1-hour data
                }
                else {
                    isWorking = true;
                    MainActivity.this.stopService(new Intent(MainActivity.this, AccSampler.class));
                    interval = 1000;    //rest for 1 second, avoiding a large data file
                }
            }
            finally {
                mHandler.postDelayed(sampler, interval);
            }
        }
    };

    void startRepeatedSampling() {
        sampler.run();
    }

    void stopRepeatedSampling() {
        mHandler.removeCallbacks(sampler);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume() called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause() called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop() called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy() called");
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
//        mTextView1.setTextColor(Color.WHITE);
//        mTextView1.getPaint().setAntiAlias(false);
//        mTextView2.setTextColor(Color.WHITE);
//        mTextView2.getPaint().setAntiAlias(false);
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
//        mTextView1.setTextColor(Color.GREEN);
//        mTextView1.getPaint().setAntiAlias(true);
//        mTextView2.setTextColor(Color.GREEN);
//        mTextView2.getPaint().setAntiAlias(true);
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
    }
}

