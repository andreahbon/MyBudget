package com.example.android.mybudget;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.mybudget.data.BudgetContract.AccEntry;

/**
 * Created by andre on 17/02/2017.
 */

public class SpAccCursorAdapter extends CursorAdapter {

    public SpAccCursorAdapter(Context context, Cursor c){super(context, c, 0);}

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.acc_spinner_list, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView accNameTV = (TextView) view.findViewById(R.id.sp_acc_name);
        final int accID = cursor.getInt(cursor.getColumnIndexOrThrow(AccEntry._ID));
        String accName = cursor.getString(cursor.getColumnIndexOrThrow(AccEntry.COLUMN_ACCNAME));
        accNameTV.setText(accName);
    }
}
