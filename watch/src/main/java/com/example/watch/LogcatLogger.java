package com.example.watch;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import java.io.File;
import java.io.IOException;

public class LogcatLogger implements LifecycleObserver {
    public static final String TAG = "watch:LogcatLogger";
    private File logFile;
    private Process process = null;

    public LogcatLogger(File logFile) {
        this.logFile = logFile;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void start() {
        // clear the previous logcat and then write the new one to the file
        try {
            process = Runtime.getRuntime().exec("logcat -c");
            process = Runtime.getRuntime().exec("logcat -f " + logFile);
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void stop() {
        // Dump the logcat to screen
        try {
            process = Runtime.getRuntime().exec("logcat -d");
        } catch ( IOException ioe ) {
            ioe.printStackTrace();
        }
    }
}
