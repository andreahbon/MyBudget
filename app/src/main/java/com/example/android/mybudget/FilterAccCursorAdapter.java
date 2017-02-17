package com.example.android.mybudget;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.mybudget.data.BudgetContract.AccEntry;
import com.example.android.mybudget.data.BudgetContract.FilterAccEntry;

/**
 * Created by andre on 17/02/2017.
 */

public class FilterAccCursorAdapter extends CursorAdapter {

    public FilterAccCursorAdapter(Context context, Cursor c){super(context, c, 0);}

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.filter_acc_list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        final CheckBox accCheck = (CheckBox) view.findViewById(R.id.filter_acc_checkbox);

        TextView accNameTV = (TextView) view.findViewById(R.id.filter_acc_name);
        final int accID = cursor.getInt(cursor.getColumnIndexOrThrow(AccEntry._ID));
        String accName = cursor.getString(cursor.getColumnIndexOrThrow(AccEntry.COLUMN_ACCNAME));
        accNameTV.setText(accName);

        String [] filterProj = {
                FilterAccEntry._ID,
                FilterAccEntry.COLUMN_ACC_ID};
        String filterSelection = FilterAccEntry.COLUMN_ACC_ID + "=" + accID;
        Cursor filterCursor = context.getContentResolver().query(FilterAccEntry.CONTENT_URI, filterProj, filterSelection, null, null);

        accCheck.setOnCheckedChangeListener(null);
        accCheck.setChecked(false);
        if (filterCursor.getCount() > 0){
            accCheck.setChecked(true);
        }
        filterCursor.close();

        accCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (accCheck.isChecked()) {
                    ContentValues values = new ContentValues();
                    values.put(FilterAccEntry.COLUMN_ACC_ID, accID);
                    Uri newUri = context.getContentResolver().insert(FilterAccEntry.CONTENT_URI, values);
                    if (newUri == null){
                        Toast.makeText(context, "There was some error.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String selection = FilterAccEntry.COLUMN_ACC_ID + "=" + accID;
                    int deletedRows = context.getContentResolver().delete(FilterAccEntry.CONTENT_URI, selection, null);
                    if (deletedRows <= 0) {
                        Toast.makeText(context, "No rows deleted.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }
}
