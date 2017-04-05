package com.udacity.stockhawk.sync;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;

public final class QuoteSyncJob {

    private static final int ONE_OFF_ID = 2;
    private static final String ACTION_DATA_UPDATED = "com.udacity.stockhawk.ACTION_DATA_UPDATED";
    private static final int PERIOD = 300000;
    private static final int INITIAL_BACKOFF = 10000;
    private static final int PERIODIC_ID = 1;
    private static final int YEARS_OF_HISTORY = 2;

    private QuoteSyncJob() {
    }

    //called to actually get the stock info from the YahooFinance API
    static void getQuotes(final Context context) {

        Timber.d("Running sync job");

        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.YEAR, -YEARS_OF_HISTORY);

        try {
            //gets the stock symbols that are in the sharedPreferences (ie the ones the use is
            //interested in seeing
            Set<String> stockPref = PrefUtils.getStocks(context);
            Set<String> stockCopy = new HashSet<>();
            stockCopy.addAll(stockPref);
            String[] stockArray = stockPref.toArray(new String[stockPref.size()]);

            Timber.d(stockCopy.toString());

            if (stockArray.length == 0) {
                return;
            }
            //gets the info from YahooFinance API for all stocks in STOCKARRAY
            Map<String, Stock> quotes = YahooFinance.get(stockArray);
            //makes an iterator through the HashSet of stock symbols in our sharedPref so we can
            //loop through them once to insert the corresponding stock data for each symbol and
            //put each one into a content values
            Iterator<String> iterator = stockCopy.iterator();

            Timber.d(quotes.toString());

            ArrayList<ContentValues> quoteCVs = new ArrayList<>();

            while (iterator.hasNext()) {
                final String symbol = iterator.next();
//                boolean variable used to determine if the ticker symbol entered is a valid one
                Boolean valid = true;
                Stock stock = quotes.get(symbol);
                if (stock == null) {
                    Timber.d(symbol + " not valid");
                    valid = false;
                } else {
                    Timber.d(stock.toString() + "stock info via Yahoo Finance API");
                    final StockQuote quote = stock.getQuote();
                    //note: in order to show a Toast from a background service thread - need to use
                    //a handler:
                    //https://discussions.udacity.com/t/toast-from-a-service-from-background-thread/221010/2
                    if (quote.getPreviousClose() == null) {
                        //remove the invalid ticker from the sharedPref - or else everytime we refresh
                        //the main page this will be activated because the invalid string is still in
                        //the SharedPref
                        Timber.d(quote.getSymbol() + " says ask is null");
                        valid = false;
                    } else {
                        float price = quote.getPrice().floatValue();
                        float change = quote.getChange().floatValue();
                        float percentChange = quote.getChangeInPercent().floatValue();


                        // WARNING! Don't request historical data for a stock that doesn't exist!
                        // The request will hang forever X_x
                        //each element in this list is a HISTORICAL QUOTE object which has an attribute
                        //called DATE (which is retrieved below by GETDATE(), and a close price attribute
                        //which is retrieved via getClose() method
                        // see the commented out Timber code in the StockAdapter's onBindViewHolder method
                        List<HistoricalQuote> history = stock.getHistory(from, to, Interval.WEEKLY);

                        StringBuilder historyBuilder = new StringBuilder();

                        for (HistoricalQuote it : history) {
                            historyBuilder.append(it.getDate().getTimeInMillis());
                            historyBuilder.append(", ");
                            historyBuilder.append(it.getClose());
                            historyBuilder.append("\n");
                        }

                        ContentValues quoteCV = new ContentValues();
                        quoteCV.put(Contract.Quote.COLUMN_SYMBOL, symbol);
                        quoteCV.put(Contract.Quote.COLUMN_PRICE, price);
                        quoteCV.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, percentChange);
                        quoteCV.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, change);


                        quoteCV.put(Contract.Quote.COLUMN_HISTORY, historyBuilder.toString());
                        //arrayList of content values that is later converted to an array for bulk insert
                        //into our DB
                        quoteCVs.add(quoteCV);
                    }
                }
                if (!valid) {
                    PrefUtils.removeStock(context, symbol);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, symbol +" invalid ticker"
                                    , Toast.LENGTH_SHORT).show();
                        }
                    });
                    continue;
                }

            }

            context.getContentResolver()
                    .bulkInsert(
                            Contract.Quote.URI,
                            quoteCVs.toArray(new ContentValues[quoteCVs.size()]));

            Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
            context.sendBroadcast(dataUpdatedIntent);

        } catch (IOException exception) {
            Timber.e(exception, "Error fetching stock quotes");
        }
    }

    private static void schedulePeriodic(Context context) {
        Timber.d("Scheduling a periodic task");


        JobInfo.Builder builder = new JobInfo.Builder(PERIODIC_ID, new ComponentName(context, QuoteJobService.class));


        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(PERIOD)
                .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);


        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        scheduler.schedule(builder.build());
    }


    public static synchronized void initialize(final Context context) {

        schedulePeriodic(context);
        syncImmediately(context);

    }

    public static synchronized void syncImmediately(final Context context) {

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        //checks if have network access they launches the QuoteIntent Service
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            Intent nowIntent = new Intent(context, QuoteIntentService.class);
            context.startService(nowIntent);
        } else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "No network connection - Stock info may be out of date"
                            , Toast.LENGTH_SHORT).show();
                }
            });
            JobInfo.Builder builder = new JobInfo.Builder(ONE_OFF_ID, new ComponentName(context, QuoteJobService.class));


            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);


            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

            scheduler.schedule(builder.build());


        }
    }


}
