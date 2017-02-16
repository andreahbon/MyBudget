package com.example.android.mybudget;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.mybudget.data.BudgetContract;

/**
 * Created by andre on 13/02/2017.
 */

public class SpCatCursorAdapter extends CursorAdapter {

    public SpCatCursorAdapter(Context context, Cursor c){super(context, c, 0);}

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.cat_spinner_list, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView catNameTV = (TextView) view.findViewById(R.id.sp_cat_name);
        final int catID = cursor.getInt(cursor.getColumnIndexOrThrow(BudgetContract.CatEntry._ID));
        String catName = cursor.getString(cursor.getColumnIndexOrThrow(BudgetContract.CatEntry.COLUMN_CATNAME));
        catNameTV.setText(catName);
    }
}
