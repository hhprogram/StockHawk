package com.udacity.stockhawk.sync;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import timber.log.Timber;

/**
 * Created by harrison on 3/24/17.
 */

public class WidgetDataProvider implements RemoteViewsService.RemoteViewsFactory{

    Context mContext;
    Cursor mCursor;
    String display_mode;

    public WidgetDataProvider(Context context, Intent intent) {
        mContext = context;
        Timber.d("Widget dataprovider initialized widget");
    }

    @Override
    public void onCreate() {
//        get a cursor for all quotes in the DB currently - not sure if this is best way but using
//        this cursor to update the widget (can I leverage the adapter somehow?
//        mCursor = mContext.getContentResolver().query(Contract.Quote.URI, null, null, null, null);
        Timber.d("Widget dataprovider in widget provider");
    }

    @Override
    public void onDataSetChanged() {
        if (mCursor != null) {
            mCursor.close();
        }
        Timber.d("Widget Dataprovider dataset changed");
        mCursor = mContext.getContentResolver().query(Contract.Quote.URI, null, null, null, null);
        display_mode = PrefUtils.getDisplayMode(mContext);
    }

    @Override
    public void onDestroy() {
//        disconnect the cursor as no longer needed
        mCursor = null;
    }

//    note this has to return the actual value that corresponds to the number of 'views' (ie number
//    of items in the collection (list) of items to be shown in this widget in order for anything to
//    show up. Defaults to zero and if have zero it will never call getViewAt
    @Override
    public int getCount() {
//        gets the number of rows attached to this cursor (ie the number of stock symbols in our
//        DB)
        return mCursor.getCount();
    }

//    similar to the StockAdapter's onBindViewHolder method. Basically, is called to populate each
//    different item in our collection widget
    @Override
    public RemoteViews getViewAt(int i) {
        RemoteViews view = new RemoteViews(mContext.getPackageName(), R.layout.list_item_quote);
        if (!mCursor.moveToFirst()) {
            Timber.d("Nothing in cursor");
        }
        Timber.d("Trying to get views");
        if (mCursor.moveToPosition(i)) {
            Timber.d(mCursor.getString(Contract.Quote.POSITION_SYMBOL));
            view.setTextViewText(R.id.symbol, mCursor.getString(Contract.Quote.POSITION_SYMBOL));
            view.setTextViewText(R.id.price, mCursor.getString(Contract.Quote.POSITION_PRICE));
            float change = Float.valueOf(mCursor.getString(Contract.Quote.POSITION_ABSOLUTE_CHANGE));
            if (display_mode.equals(mContext.getString(R.string.pref_display_mode_absolute_key))) {
                view.setTextViewText(R.id.change, "$" + mCursor
                        .getString(Contract.Quote.POSITION_ABSOLUTE_CHANGE));
            } else if (display_mode.equals(mContext.getString(R.string.pref_display_mode_percentage_key))) {
                view.setTextViewText(R.id.change, "%"+mCursor.getString(Contract
                        .Quote.POSITION_PERCENTAGE_CHANGE));
            }
//            this calls the method given by the 2nd argument (ie setBackgroundColor) on the view
//            given by the first argument and sets it to the value in the 3rd argument
            if (change < 0) {
                view.setInt(R.id.change, "setBackgroundColor", Color.RED);
            }
        }
//        do {
//            view.setTextViewText(symbol, mCursor.getString(Contract.Quote.POSITION_SYMBOL));
//
//        } while (mCursor.moveToNext())
        return view;
    }

//    custom placeholder that shows a custom loading view in the interim when the actual view
//    returned by getViewAt
    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

//note when this just returns 0 then all i get is a 'loading..." in each view but when set to 1
//    a proper view shows up
    @Override
    public int getViewTypeCount() {
        return 1    ;
    }

//    should return the unique ID of the row of the item at position i in my cursor
    @Override
    public long getItemId(int i) {
        return 0;
    }

//  telling me if the IDs returned in getItemID are unique and no 2 IDs refer to same thing.
//    ie changes to underlying data won't impact ID mapping
    @Override
    public boolean hasStableIds() {
        return false;
    }


}
