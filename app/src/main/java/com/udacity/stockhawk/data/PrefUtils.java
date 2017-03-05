package com.udacity.stockhawk.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.udacity.stockhawk.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import timber.log.Timber;

public final class PrefUtils {

    private PrefUtils() {
    }

    /**
     * returns the stock ticker symbols in our shared preferences
     * @param context - used to access our shared preferences
     * @return a set of strings
     */
    public static Set<String> getStocks(Context context) {
        String stocksKey = context.getString(R.string.pref_stocks_key);
        String initializedKey = context.getString(R.string.pref_stocks_initialized_key);
        //line gets the array of strings to be used by default to populate the homepage stock ticker
        String[] defaultStocksList = context.getResources().getStringArray(R.array.default_stocks);

        HashSet<String> defaultStocks = new HashSet<>(Arrays.asList(defaultStocksList));
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        //gets boolean from shared preferences that denotes whether or note the preferences has been
        //intialized and populated before by the app
        boolean initialized = prefs.getBoolean(initializedKey, false);

        //if never intialized, then it takes a set of Strings (the default stocks set of strings)
        //and puts them all into the shared preferences. This whole set of strings is associated
        //with the key STOCKSKEY
        if (!initialized) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(initializedKey, true);
            editor.putStringSet(stocksKey, defaultStocks);
            editor.apply();
            return defaultStocks;
        }
        //if already intialized just returns the set of strings in the shared preferences
        return prefs.getStringSet(stocksKey, new HashSet<String>());

    }

    /**
     * Changes the stock ticker symbol set (add or remove)
     * @param context - used to access the shared preferences
     * @param symbol - the stock ticker symbol string
     * @param add - boolean denoting whether this action should add the ticker or not
     */
    private static void editStockPref(Context context, String symbol, Boolean add) {
        String key = context.getString(R.string.pref_stocks_key);
        Set<String> stocks = getStocks(context);

        //have a string set that is current shared preferences string set..then the below lines
        //mutate the set. and then we just put this new string set into the shared preferences with
        //the same key which just overrides the old string preferences set
        if (add) {
            stocks.add(symbol);
        } else {
            stocks.remove(symbol);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(key, stocks);
        editor.apply();
    }

    public static void addStock(Context context, String symbol) {
        editStockPref(context, symbol, true);
    }

    public static void removeStock(Context context, String symbol) {
        editStockPref(context, symbol, false);
    }

//    based on the shared preferences gets what the menu item should show either '$' or '%'
    public static String getDisplayMode(Context context) {
        String key = context.getString(R.string.pref_display_mode_key);
        String defaultValue = context.getString(R.string.pref_display_mode_default);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(key, defaultValue);
    }

    /**
     * method that changes the display mode to the only other display mode available.
     * first get a sharedPreferences instance object, then get an editor object of our specific
     * sharedPref instance. And then use this editor to reassign KEY (the display mode preference)
     * to be the other option
     * @param context - the context in which we call this method
     */
    public static void toggleDisplayMode(Context context) {
        String key = context.getString(R.string.pref_display_mode_key);
        String absoluteKey = context.getString(R.string.pref_display_mode_absolute_key);
        String percentageKey = context.getString(R.string.pref_display_mode_percentage_key);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String displayMode = getDisplayMode(context);

        SharedPreferences.Editor editor = prefs.edit();

        if (displayMode.equals(absoluteKey)) {
            editor.putString(key, percentageKey);
        } else {
            editor.putString(key, absoluteKey);
        }

        editor.apply();
    }

    /**
     * converts string to LINEDATA (object used in MPANDROIDCHART library - which is what we're
     * using to graph historical data
     * @param context - the CONTEXT that is calling this so we can use XML resources etc..
     * @param data - the data points to be transformed into a DATASET. this is just the String
     *             that is stored in the DB table (ie has date in milliseconds and the associated
     *             closing price)
     * @return a hashmap with 2 values (1) LINEDATA object, which is an object in the MPandroidChart
     *          library which holds data found in DATA (2) an array of labels associated with DATA
     *          to be used for x axis labels
     */
    public static HashMap<String, Object> str_to_line_data(Context context, String data) {
//        holds the data points to be graphed
        ArrayList<Entry> entries = new ArrayList<>();
//        holds the labels that the ENTRIES correspond to
        ArrayList<String> labels = new ArrayList<>();
        HashMap<String, Object> chart_data = new HashMap<>();
        int position = 0;
        int comma = 0;
        int new_line = 0;
        Long time;
        Date date;
        String str_date, price;
        SimpleDateFormat sdf = new SimpleDateFormat(context.getString(R.string.date_format));
        while (position < data.length() - 1) {
            comma = data.indexOf(context.getString(R.string.comma), position);
            time = Long.parseLong(data.substring(position, comma));
            date = new Date(time);
            str_date = sdf.format(date);
            labels.add(str_date);
            new_line = data.indexOf(context.getString(R.string.new_line), position);
            price = data.substring(comma+1, new_line);
            entries.add(new Entry(time.floatValue(), Float.parseFloat(price)));
            Timber.d("Time: %f \n Price: %f", time.floatValue(), Float.parseFloat(price));
            //move my starting point POSITION by the difference between the newLine and the old
            //POSITION (which gets me to the newline char and then add 1 to get to the first letter)
            position = position + (new_line-position) + 1;
        }
        Timber.d(entries.get(0).toString());
        LineDataSet lineDataSet = new LineDataSet(entries, context.getString(R.string.hist_prices));
//        lineDataSet.setColor(R.color.colorPrimary);
        lineDataSet.setColors(new int[] {R.color.material_blue_500}, context);
        LineData lineData = new LineData(lineDataSet);
        chart_data.put(context.getString(R.string.line_data), lineData);
        chart_data.put(context.getString(R.string.line_labels), labels.toArray());
        return chart_data;
    }

    public static String[] convert_array(Object[] arr) {
        String[] str_arr = new String[arr.length];
        int count = 0;
        for (Object i : arr) {
            String str = (String) i;
            str_arr[count] = str;
            count++;
        }
        return str_arr;
    }

}
