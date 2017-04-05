package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import timber.log.Timber;

/**
 * Created by harrison on 2/27/17.
 * Detail activity to show a graph of the historical stock prices.
 */

public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private final int STOCK_LOADER = 0;
    private final String selection = Contract.Quote.COLUMN_SYMBOL + "= ?";
    private final String[] projection = {Contract.Quote.COLUMN_HISTORY};
    Bundle detailBundle;
    String stockSymbol;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        Intent intent = getIntent();
        setContentView(R.layout.activity_detail);
        detailBundle = intent.getExtras();
        stockSymbol = detailBundle.getString(Contract.Quote.COLUMN_SYMBOL);
        getSupportActionBar().setTitle(stockSymbol);
        getSupportActionBar().setHomeActionContentDescription(R.string.to_homescreen);

        getSupportLoaderManager().initLoader(STOCK_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] selectionArgs = {stockSymbol};
        return new CursorLoader(this, Contract.Quote.URI, projection, selection, selectionArgs
                , null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            Timber.d("No data in this cursor for: %s", stockSymbol);
        }
        HashMap<String, Object> chart_data = PrefUtils.str_to_line_data(this, data.getString(0));
        LineData lineData = (LineData) chart_data.get(getString(R.string.line_data));
        //remember need to declare local variables that an inner class accesses as final, as it
        //actually makes a copy of this variable vs. actually acessing therefore, need to declare
        //final such that you don't change value of variable after an inner class variable has been
        //instantiated
        final HashMap<Integer, Date> labels = (HashMap<Integer, Date>) chart_data
                .get(getString(R.string.line_labels));
        final SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.date_format));
        LineChart finalchart = (LineChart) findViewById(R.id.chart);
        finalchart.setData(lineData);

        //axis formatter that refers to the LABELS array to put down nice x axis labels
        IAxisValueFormatter axisValueFormatter = new IAxisValueFormatter() {
            //trying to put in logic to only put labels in certain intervals
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                int date_num = (int) value;
                Date date = labels.get(date_num);
                return sdf.format(date);
            }
        };
        XAxis xaxis = finalchart.getXAxis();
        xaxis.setValueFormatter(axisValueFormatter);
//        gets the yaxis object (the left yaxis)
        YAxis yaxis = finalchart.getAxisLeft();
        yaxis.setDrawLabels(true);
        TextView start = (TextView) findViewById(R.id.start_date);
        TextView end = (TextView) findViewById(R.id.end_date);
        TextView max = (TextView) findViewById(R.id.min_price);
        TextView min = (TextView) findViewById(R.id. max_price);
        int start_date = (int) chart_data.get(getString(R.string.start_date));
        int end_date = (int) chart_data.get(getString(R.string.end_date));
        int max_x_value = (int) chart_data.get(getString(R.string.max_date));
        int min_x_value = (int) chart_data.get(getString(R.string.min_date));
        float max_price = (float) chart_data.get(getString(R.string.max_price));
        float min_price = (float) chart_data.get(getString(R.string.min_price));
        start.setText(getString(R.string.start_detail) +" "+ sdf.format(labels.get(start_date)));
        end.setText(getString(R.string.end_detail) + " " + sdf.format(labels.get(end_date)));
        max.setText(getString(R.string.max_price)+ " $" + max_price + " " + "("
                + sdf.format(labels.get(max_x_value)) +")");
        min.setText(getString(R.string.min_price)+ " $"+ + min_price + " " + "("
                + sdf.format(labels.get(min_x_value)) +")");

        //get the legend associated with the chart object and then set the legend to false as no
        //need to show it with only one line
        finalchart.getLegend().setEnabled(false);
        //Description object is part of the MPChartAndroid Library
        Description chart_desc = new Description();
        chart_desc.setText("Stock Price in USD");
        finalchart.setDescription(chart_desc);
        finalchart.invalidate();


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
