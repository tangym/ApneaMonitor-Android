package com.example.apneamonitor;

import java.util.Iterator;
import java.util.List;

public class Segmenter {

    private List<Double> arrayList;
    private int step;
    private int windowSize;
    private int currentIndex;

    public Segmenter(List<Double> data, int step, int windowSize) {
        this.arrayList = data;
        this.step = step;
        this.windowSize = windowSize;
        this.currentIndex = 0;
    }

    public boolean hasNext() {
        if (currentIndex + windowSize < arrayList.size()) {
            return true;
        } else {
            return false;
        }
    }

    public List<Double> next() {
        List<Double> segment = arrayList.subList(currentIndex, currentIndex + windowSize);
        currentIndex += step;
        return segment;
    }

}