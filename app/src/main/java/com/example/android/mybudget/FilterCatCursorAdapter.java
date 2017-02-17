package com.example.android.mybudget;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.mybudget.data.BudgetContract.CatEntry;
import com.example.android.mybudget.data.BudgetContract.FilterCatEntry;

/**
 * Created by andre on 17/02/2017.
 */

public class FilterCatCursorAdapter extends CursorAdapter {

    public FilterCatCursorAdapter(Context context, Cursor c){super(context, c, 0);}

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.filter_cat_list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        final CheckBox catCheck = (CheckBox) view.findViewById(R.id.filter_cat_checkbox);

        TextView catNameTV = (TextView) view.findViewById(R.id.filter_cat_name);
        final int catID = cursor.getInt(cursor.getColumnIndexOrThrow(CatEntry._ID));
        String catName = cursor.getString(cursor.getColumnIndexOrThrow(CatEntry.COLUMN_CATNAME));
        catNameTV.setText(catName);

        String [] filterProj = {
                FilterCatEntry._ID,
                FilterCatEntry.COLUMN_CAT_ID};
        String filterSelection = FilterCatEntry.COLUMN_CAT_ID + "=" + catID;
        Cursor filterCursor = context.getContentResolver().query(FilterCatEntry.CONTENT_URI, filterProj, filterSelection, null, null);

        catCheck.setOnCheckedChangeListener(null);
        catCheck.setChecked(false);
        if(filterCursor.getCount()>0){
            catCheck.setChecked(true);
        }
        filterCursor.close();

        catCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(catCheck.isChecked()){
                    ContentValues values = new ContentValues();
                    values.put(FilterCatEntry.COLUMN_CAT_ID, catID);
                    Uri newUri = context.getContentResolver().insert(FilterCatEntry.CONTENT_URI, values);
                    if(newUri == null){
                        Toast.makeText(context, "There was some error.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String selection = FilterCatEntry.COLUMN_CAT_ID + "=" + catID;
                    int deletedRows = context.getContentResolver().delete(FilterCatEntry.CONTENT_URI, selection, null);
                    if(deletedRows <= 0){
                        Toast.makeText(context, "No rows deleted.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
