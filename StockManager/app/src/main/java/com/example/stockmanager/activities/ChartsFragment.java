package com.example.stockmanager.activities;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.stockmanager.R;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Access the data in some database and retrieve for the charts in the dashboard
 */
public class ChartsFragment extends Fragment {

    private final static String TAG = ChartsFragment.class.getSimpleName();

    private Context CONTEXT;

    private View rootView;

    private Animation rootAnimation = new AlphaAnimation(0f, 1f);
    private static final long ANIMATION_TIME = 1_500;

    private LineChart lineChart;
    private PieChart pieChart;
    private BarChart barChart;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.dashboard_fragment, container, false);
        return rootView;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {

            lineChart = rootView.findViewById(R.id.lineChart);
            pieChart  = rootView.findViewById(R.id.pieChart);
            barChart  = rootView.findViewById(R.id.barChart);

            // Configure animations and transitions
            rootAnimation.setDuration(ANIMATION_TIME);
            rootView.setAnimation(rootAnimation);

//            FrameLayout fl = new FrameLayout(getContext());
//            fl.setId(View.generateViewId());

            drawLineChart();
            drawPieChart();
            drawBarChart();


        } catch (Exception e){
            Log.e(TAG, "Cannot create the view.", e);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }


    /**
     * Draw a Line Chart example
     */
    private void drawLineChart(){

        {   // // Chart Style // //
            // background color
            lineChart.setBackgroundColor(Color.WHITE);

            // disable description text
            lineChart.getDescription().setEnabled(false);

            // enable touch gestures
            lineChart.setTouchEnabled(true);

            // set listeners
            lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    Log.i("Entry selected", e.toString());
                }

                @Override
                public void onNothingSelected() {
                }
            });
            lineChart.setDrawGridBackground(false);

            // enable scaling and dragging
            lineChart.setDragEnabled(true);
            lineChart.setScaleEnabled(true);

            // force pinch zoom along both axis
            lineChart.setPinchZoom(true);
        }

        XAxis xAxis;
        {   // // X-Axis Style // //
            xAxis = lineChart.getXAxis();

            // vertical grid lines
            xAxis.enableGridDashedLine(10f, 10f, 0f);
        }

        YAxis yAxis;
        {   // // Y-Axis Style // //
            yAxis = lineChart.getAxisLeft();

            // disable dual axis (only use LEFT axis)
            lineChart.getAxisRight().setEnabled(false);

            // horizontal grid lines
            yAxis.enableGridDashedLine(10f, 10f, 0f);

            // axis range
            yAxis.setAxisMaximum(200f);
            yAxis.setAxisMinimum(-50f);
        }


        {   // // Create Limit Lines // //
            LimitLine llXAxis = new LimitLine(9f, "Index 10");
            llXAxis.setLineWidth(4f);
            llXAxis.enableDashedLine(10f, 10f, 0f);
            llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
            llXAxis.setTextSize(10f);
//                llXAxis.setTypeface(tfRegular);

            LimitLine ll1 = new LimitLine(150f, "Upper Limit");
            ll1.setLineWidth(4f);
            ll1.enableDashedLine(10f, 10f, 0f);
            ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
            ll1.setTextSize(10f);
//                ll1.setTypeface(tfRegular);

            LimitLine ll2 = new LimitLine(-30f, "Lower Limit");
            ll2.setLineWidth(4f);
            ll2.enableDashedLine(10f, 10f, 0f);
            ll2.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
            ll2.setTextSize(10f);
//                ll2.setTypeface(tfRegular);

            // draw limit lines behind data instead of on top
            yAxis.setDrawLimitLinesBehindData(true);
            xAxis.setDrawLimitLinesBehindData(true);

            // add limit lines
            yAxis.addLimitLine(ll1);
            yAxis.addLimitLine(ll2);
        }

        // add data
        setLineChartData(45, 180);

        // draw points over time
        lineChart.animateX((int)ANIMATION_TIME);

        // get the legend (only possible after setting data)
        Legend l = lineChart.getLegend();

        // draw legend entries as lines
        l.setForm(Legend.LegendForm.LINE);
    }

    /**
     * Create a random data set for line chart
     * @param count number of points to be added
     * @param range range of the y value
     */
    private void setLineChartData(int count, float range) {

        ArrayList<Entry> values = new ArrayList<>();

        for (int i = 0; i < count; i++) {

            float val = (float) (Math.random() * range) - 30;
            values.add(new Entry(i, val/*, getResources().getDrawable(R.drawable.star)*/));
        }

        LineDataSet set1;

        if (lineChart.getData() != null &&
                lineChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) lineChart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            set1.notifyDataSetChanged();
            lineChart.getData().notifyDataChanged();
            lineChart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(values, "DataSet 1");

            set1.setDrawIcons(false);

            // draw dashed line
            set1.enableDashedLine(10f, 5f, 0f);

            // black lines and points
            set1.setColor(Color.BLACK);
            set1.setCircleColor(Color.BLACK);

            // line thickness and point size
            set1.setLineWidth(1f);
            set1.setCircleRadius(3f);

            // draw points as solid circles
            set1.setDrawCircleHole(false);

            // customize legend entry
            set1.setFormLineWidth(1f);
            set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set1.setFormSize(15.f);

            // text size of values
            set1.setValueTextSize(9f);

            // draw selection line as dashed
            set1.enableDashedHighlightLine(10f, 5f, 0f);

            // set the filled area
            set1.setDrawFilled(true);
            set1.setFillFormatter(new IFillFormatter() {
                @Override
                public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                    return lineChart.getAxisLeft().getAxisMinimum();
                }
            });

//            // set color of filled area
//            if (Utils.getSDKInt() >= 18) {
//                // drawables only supported on api level 18 and above
//                Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_red);
//                set1.setFillDrawable(drawable);
//            } else {
                set1.setFillColor(Color.BLACK);
//            }

            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1); // add the data sets

            // create a data object with the data sets
            LineData data = new LineData(dataSets);

            // set data
            lineChart.setData(data);
        }
    }


    /**
     * Draw a example of a bar chart
     */
    private void drawBarChart(){
        try {
            // Plot layout
            barChart.setDrawMarkers(false);
            barChart.setDescription(null);
            barChart.getLegend().setEnabled(true);

            // Convert heart rate data to bar entries
//            List<BarEntry> entryList = generateBarData();

            // Setup the data
            List<IBarDataSet> bdt = generateBarData();

            // add a lot of colors

            BarData bd = new BarData(bdt);

            barChart.setData(bd);

            Legend l = pieChart.getLegend();
//            l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
//            l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
//            l.setOrientation(Legend.LegendOrientation.VERTICAL);
            l.setDrawInside(false);
            l.setXEntrySpace(7f);
            l.setYEntrySpace(0f);
            l.setYOffset(0f);

            barChart.animateY((int)ANIMATION_TIME, Easing.Linear);

        } catch (Exception e){
            Log.i(TAG, "Cannot plot the HRM data.");
            e.printStackTrace();
        }
    }

    /**
     * Draw a pie chart example
     */
    private void drawPieChart(){
        try {
            // Plot layout
            pieChart.setDrawMarkers(false);
            pieChart.setDescription(null);
            pieChart.getLegend().setEnabled(true);

            // Convert heart rate data to bar entries
            List<PieEntry> entryList = generatePieData();

            // Setup the data
            PieDataSet pdt = new PieDataSet(entryList, "");
            pdt.setSliceSpace(5f); // Spacing between slices
            pdt.setValueTextColor(Color.WHITE);
            pdt.setValueTextSize(24f);


            // add a lot of colors

            ArrayList<Integer> colors = new ArrayList<>();

            for (int c : ColorTemplate.VORDIPLOM_COLORS) colors.add(c);
            for (int c : ColorTemplate.JOYFUL_COLORS) colors.add(c);
            for (int c : ColorTemplate.COLORFUL_COLORS) colors.add(c);
            for (int c : ColorTemplate.LIBERTY_COLORS) colors.add(c);
            for (int c : ColorTemplate.PASTEL_COLORS) colors.add(c);
            colors.add(ColorTemplate.getHoloBlue());
            pdt.setColors(colors);

            PieData pd = new PieData(pdt);

            pieChart.setUsePercentValues(false);
//            pieChart.setDrawHoleEnabled(true);
            pieChart.setHoleColor(Color.WHITE);
            pieChart.setData(pd);

            Legend l = pieChart.getLegend();
//            l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
//            l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
//            l.setOrientation(Legend.LegendOrientation.VERTICAL);
            l.setDrawInside(false);
            l.setXEntrySpace(7f);
            l.setYEntrySpace(0f);
            l.setYOffset(0f);

            pieChart.animateY((int)ANIMATION_TIME, Easing.Linear);

        } catch (Exception e){
            Log.i(TAG, "Cannot plot the HRM data.");
            e.printStackTrace();
        }
    }

    /**
     * Generate some data for the pie chart
     * @return
     */
    private List<IBarDataSet> generateBarData() {

        ArrayList<Integer> colors = new ArrayList<>();
        for (int c : ColorTemplate.VORDIPLOM_COLORS) colors.add(c);
        for (int c : ColorTemplate.JOYFUL_COLORS) colors.add(c);
        for (int c : ColorTemplate.COLORFUL_COLORS) colors.add(c);
        for (int c : ColorTemplate.LIBERTY_COLORS) colors.add(c);
        for (int c : ColorTemplate.PASTEL_COLORS) colors.add(c);
        colors.add(ColorTemplate.getHoloBlue());

        List<IBarDataSet> dataList = new ArrayList<>();
        Map<String, Integer> data = sales();
        Iterator keys = data.keySet().iterator();
        int i = 0;
        while(keys.hasNext()){
            String key = (String) keys.next();
            Integer value = data.get(key);
            List<BarEntry> bel = new ArrayList<>();
            bel.add(new BarEntry(i++, (float) value));
            BarDataSet bdt = new BarDataSet(bel,key);
            bdt.setValueTextColor(Color.WHITE);
            bdt.setValueTextSize(24f);
            bdt.setColor(colors.get(i));
            dataList.add(bdt);
        }

        return dataList;
    }

    /**
     * Generate some data for the pie chart
     * @return
     */
    private List<PieEntry> generatePieData() {
        List<PieEntry> dataList = new ArrayList<>();
        Map<String, Integer> data = sales();
        Iterator keys = data.keySet().iterator();
        while(keys.hasNext()){
            String key = (String) keys.next();
            Integer value = data.get(key);
            dataList.add(new PieEntry((float) value, key));
        }

        return dataList;
    }


    /**
     * Create a generic data set with values of 'sales' per brazil states
     * @return
     */
    public Map<String, Integer> sales(){
        Map<String, Integer> sales = new HashMap<>();
        sales.put("São Paulo", 100);
        sales.put("Rio de Janeiro", 20);
        sales.put("Paraná", 80);
        sales.put("Bahia", 60);
        sales.put("Goiás", 10);
        sales.put("Minas Gerais", 75);
        return sales;
    }

}
