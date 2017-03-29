package com.udacity.stockhawk.sync;

import android.content.Intent;
import android.widget.RemoteViewsService;

import timber.log.Timber;

/**
 * Created by harrison on 3/24/17.
 */

public class WidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Timber.d("inside service");
        return new WidgetDataProvider(this, intent);
    }
}
