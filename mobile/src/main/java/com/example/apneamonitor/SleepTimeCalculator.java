package com.example.apneamonitor;

import java.util.List;

public class SleepTimeCalculator {
    private int step;
    private int windowSize;

    public SleepTimeCalculator(int step, int windowSize) {
        this.step = step;
        this.windowSize = windowSize;
    }

    public double calculateSleepTime(List<Double> data) {
        int totalSleepSegments = 0;
        Segmenter segments = new Segmenter(data, step, windowSize);
        while (segments.hasNext()) {
            List<Double> segment = segments.next();
            if (isSleep(segment)) {
                totalSleepSegments += 1;
            }
        }
        return totalSleepSegments;
    }

    static public boolean isSleep(List<Double> segment) {
        // TODO: implement this function
        return true;
    }

}
