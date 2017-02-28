package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

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
        Object[] obj_arr = (Object[]) chart_data.get(getString(R.string.line_labels));
        final String[] labels = PrefUtils.convert_array(obj_arr);
        LineChart chart = (LineChart) findViewById(R.id.chart);
        chart.setData(lineData);
        //axis formatter that refers to the LABELS array to put down nice x axis labels
        IAxisValueFormatter axisValueFormatter = new IAxisValueFormatter() {
            //trying to put in logic to only put labels in certain intervals
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                int index = (int) value;
                if (index % 4 == 0) {
                    return labels[index];
                }
                else {
                    return "";
                }
            }
        };
        XAxis xaxis = chart.getXAxis();
        xaxis.setValueFormatter(axisValueFormatter);
        chart.invalidate();

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
