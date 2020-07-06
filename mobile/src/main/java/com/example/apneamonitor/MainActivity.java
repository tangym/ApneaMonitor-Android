package com.example.apneamonitor;

import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void showAHI(View view) {
        TextView sleepTimeTextView = (TextView) findViewById(R.id.sleepTimeTextView);
        TextView apneaCountTextView = (TextView) findViewById(R.id.apneaCountTextView);
        TextView ahiTextView = (TextView) findViewById(R.id.ahiTextView);

        List<Double> data = receiveTrace();
//        data = Filter.filter(data);

        // FIXME: change these parameters
        SleepTimeCalculator stc = new SleepTimeCalculator(10, 100);
        double totalSleepTime = stc.calculateSleepTime(data);

        ApneaCalculator ac = new ApneaCalculator(10, 100);
        int apneaCount = ac.calculateApneaEvents(data);

        sleepTimeTextView.setText(String.format("Total sleep time: %f hours", totalSleepTime));
        apneaCountTextView.setText(String.format("Total Apnea Events: %d", apneaCount));
        ahiTextView.setText(String.format("AHI: %f", apneaCount / totalSleepTime));

        ArrayList<Float> sleepTimePerHour = new ArrayList<>();
        for (float time: new float[] {55, 59, 47, 60, 60, 50, 55, 23}) {
            sleepTimePerHour.add(time);
        }
        renderBarChart(sleepTimePerHour);
        renderLineChart(sleepTimePerHour);
    }

    public void renderBarChart(List<Float> sleepTimePerHour) {
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < sleepTimePerHour.size(); i++) {
//            entries.add(new BarEntry(i + 1, new float[] {sleepTimePerHour.get(i), 60 - sleepTimePerHour.get(i)}));
            entries.add(new BarEntry(i + 1, sleepTimePerHour.get(i)));
        }

        BarDataSet set = new BarDataSet(entries, "Sleep time");
        BarData data = new BarData(set);
        data.setBarWidth(0.9f); // set custom bar width
//        set.setColors(ColorTemplate.JOYFUL_COLORS);
//        set.setValueTextColor(Color.BLACK);
//        set.setValueTextSize(18f);

        BarChart barChart = findViewById(R.id.barchart);
        barChart.animateY(2000);

        YAxis leftAxis = barChart.getAxisLeft();
        YAxis rightAxis = barChart.getAxisRight();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(60f);
//        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawGridLinesBehindData(false);
        leftAxis.enableGridDashedLine(10f, 10f, 10f);
        leftAxis.setLabelCount(5, true);
        rightAxis.setAxisMinimum(0f);
        rightAxis.setAxisMaximum(60f);
        rightAxis.setDrawGridLines(false);
        rightAxis.setLabelCount(5, true);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setLabelCount(sleepTimePerHour.size());
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        barChart.setData(data);
        barChart.setFitBars(true); // make the x-axis fit exactly all bars
        barChart.setDrawBarShadow(true);
        barChart.setDrawGridBackground(false);
        barChart.setHighlightFullBarEnabled(true);
        barChart.invalidate(); // refresh
    }

    public void renderLineChart(List<Float> sleepTimePerHour) {
        LineChart chart = (LineChart) findViewById(R.id.linechart);

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < sleepTimePerHour.size(); i++) {
//            entries.add(new BarEntry(i + 1, new float[] {sleepTimePerHour.get(i), 60 - sleepTimePerHour.get(i)}));
            entries.add(new Entry(i + 1, sleepTimePerHour.get(i)));
        }
        LineDataSet dataSet = new LineDataSet(entries, "Label");
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate(); // refresh
    }

    public List<Double> receiveTrace() {
        // TODO: retrieve data from Data Layer API from smartwatch
        ArrayList<Double> trace = new ArrayList<Double>(Collections.nCopies(500, 0.0));
        return trace;
    }


}