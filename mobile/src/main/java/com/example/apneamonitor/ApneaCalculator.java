package com.example.apneamonitor;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

public class ApneaCalculator {
    private int step;
    private int windowSize;

    public ApneaCalculator(int step, int windowSize) {
        this.step = step;
        this.windowSize = windowSize;
    }

    public int calculateApneaEvents(List<Double> data) {
        int totalApneaEvents = 0;
        Segmenter segments = new Segmenter(data, step, windowSize);

        while (segments.hasNext()) {
            List<Double> segment = segments.next();
            if (SleepTimeCalculator.isSleep(segment)) {
                List<Double> features = extractFeatures(segment);
                if (isApnea(features)) {
                    totalApneaEvents += 1;
                }
            }
        }
        return totalApneaEvents;
    }

    static public boolean isApnea(List<Double> features) {
        // import RF here
        return Math.random() < 0.5;
    }

    public List<Double> extractFeatures(List<Double> segment) {
        List<Double> features = new ArrayList<>();
        // std, mean, 20, 80, median, dis, peaks, amp_change
        features.add(sum(segment));
        features.add(mean(segment));
        features.add(std(segment));
        return features;
    }

    public double sum(List<Double> data) {
        double summary = 0.0;
        for (double d: data) {
            summary += d;
        }
        return summary;
    }

    public double mean(List<Double> data) {
        return sum(data) / data.size();
    }

    public double std(List<Double> data) {
        double mean = mean(data);
        double s = 0.0;
        for (double d: data) {
            double diff = d - mean;
            s += (diff * diff);
        }
        s /= data.size();
        return Math.sqrt(s);
    }
}
