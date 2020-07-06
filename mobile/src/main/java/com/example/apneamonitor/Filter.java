package com.example.apneamonitor;

import java.util.ArrayList;
import java.util.List;

public class Filter {
    static public List<Double> filter(List<Double> data) {
        ArrayList<Double> output = new ArrayList<>();
        // Change to TV filter here
        int kernelSize = 5;
        output = (ArrayList<Double>) movingAverageFilter(data, kernelSize);
        return output;
    }

    static public List<Double> movingAverageFilter(List<Double> data, int kernelSize) {
        ArrayList<Double> kernel = new ArrayList<>();
        for (int i = 0; i < kernelSize; i++) {
            kernel.add(1.0 / kernelSize);
        }
        return convolve(data, kernel);
    }

    static public List<Double> convolve(List<Double> data, List<Double> kernel) {
        ArrayList<Double> output = new ArrayList<>(data.size());
        for (int i = 0; i < data.size() - kernel.size(); i++) {
            double singlePointOutput = 0.0;
            for (int j = 0; j < kernel.size(); j++) {
                singlePointOutput += data.get(i + j) * kernel.get(kernel.size() - j - 1);
            }
            output.set(i, singlePointOutput);
        }
        return output;
    }

    static public List<Double> convolve(List<Double> data, List<Double> kernel, String mode) {
        if (mode.equals("valid")) {
            return convolve(data, kernel);
        }
        else if (mode.equals("padding")) {
            // consider padding length
            for (int i = 0; i < kernel.size() - 1; i++) {
                data.add(0, 0.0);
                data.add(0.0);
            }
            return convolve(data, kernel);
        } else {
            return convolve(data, kernel);
        }
    }
}
