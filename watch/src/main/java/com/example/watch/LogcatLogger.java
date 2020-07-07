package com.example.watch;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import java.io.File;
import java.io.IOException;

public class LogcatLogger implements LifecycleObserver {
    public static final String TAG = "watch:LogcatLogger";
    private File logFile;

    public LogcatLogger(File logFile) {
        this.logFile = logFile;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void run() {
        // clear the previous logcat and then write the new one to the file
        try {
            Process process = Runtime.getRuntime().exec("logcat -c");
            process = Runtime.getRuntime().exec("logcat -f " + logFile);
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }
}
