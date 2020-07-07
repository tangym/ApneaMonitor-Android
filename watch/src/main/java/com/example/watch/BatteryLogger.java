package com.example.watch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BatteryLogger implements LifecycleObserver {
    public static final String TAG = "watch:BatteryLogger";
    static final public SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault());

    private Context context;
    private File outputFile;
    //    BufferedWriter writer;
    FileOutputStream writer;

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
                writer.write(String.format(template,
                        timestamp, isLow, level, scale, health, powerSource, status, technology, temperature, voltage).getBytes());
//                writer.flush();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    };

    public BatteryLogger(Context context, File batteryStatusFile) {
        this.context = context;
        this.outputFile = batteryStatusFile;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void start() {
        // Initialize battery status log writer
        try {
//            writer = new BufferedWriter(new FileWriter(outputFile), 200);
            writer = new FileOutputStream(outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        context.registerReceiver(this.mBatteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

}
