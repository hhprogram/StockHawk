package com.udacity.stockhawk.sync;

import android.app.IntentService;
import android.content.Intent;

import timber.log.Timber;


public class QuoteIntentService extends IntentService {

    public QuoteIntentService() {
        super(QuoteIntentService.class.getSimpleName());
    }

    //when this class receives an intent and it is handle - then it calls the getQuotes method
    //in QuoteSyncJob
    @Override
    protected void onHandleIntent(Intent intent) {
        Timber.d("Intent handled");
        QuoteSyncJob.getQuotes(getApplicationContext());
    }
}
