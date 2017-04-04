package com.udacity.stockhawk.ui;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.sync.WidgetService;

import timber.log.Timber;

/**
 * Created by harrison on 3/20/17.
 */

public class WidgetProvider extends AppWidgetProvider{

    static void widgetUpdate(Context context, int id, AppWidgetManager appWidgetManager) {
//        this is setting the base layout for the widget
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        Timber.d("Inside widget update");
//        We then take that views base layout to an adapter to fit the R.ID.WIDGET_LIST (which
//        is some sort of view compabitible with adapters (this case a list view) and then you
//        populate that listview with data retrieved from the INTENT created in the second arg
//        this intent is source from the context that is fed into the onUpdate method and then the
//        destination is the WidgetService class created by us which is a service that creates
//        a widgetDataProvider object which actually retrieves the data to be shown in our widget
        views.setRemoteAdapter(R.id.widget_list, new Intent(context, WidgetService.class));
//        then tell the widget manager to update the widget for the new data retrieved / what
//        now populated in the new widget_list layout
        appWidgetManager.updateAppWidget(id, views);
    }

//    method called when the widget is first created / whenever the widget receives updated data
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Timber.d("Inside widget");
        for (int id : appWidgetIds) {
            widgetUpdate(context, id, appWidgetManager);
            Timber.d("Inside loop");

        }

    }

    //    need to override the onReceive() method. It is called when an intent is sent (one of the
//    intent filters listed on the WidgetProvider section of the Manifest) Then in this method
//    we have logic to take certain actions depending what intent was received. Then have the
//    appWidgetManager call the below given method and then this will make the widget that received
//    this call its onDataSetChanged method and correctly update the widget
//    appWidgetManager.notifyAppWidgetViewDataChanged. Calling this method then well call the
//    WidgetDatProvider class' onDataSetChanged() method which in my case will update the whole
//    widget list view for the latest in the shared preferences so any changes in the DB of tickers
//    or whether display mode is % or $ will be updated once you view the widget again. Does the
//    actual onDataSetChange() method call only once I exit the application and can see the widget in view
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Timber.d("Inside onReceive: " + intent.getAction());
        if (intent.getAction().equals(context.getString(R.string.custom_action))) {
            Timber.d("inside more");
            int[] ids = intent.getIntArrayExtra(context.getString(R.string.widget_ids));
            Timber.d(ids.toString());
            AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(ids
                    , R.id.widget_list);
        }
    }


}
