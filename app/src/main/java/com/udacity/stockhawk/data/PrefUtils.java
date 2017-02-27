package com.udacity.stockhawk.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.udacity.stockhawk.R;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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

}
