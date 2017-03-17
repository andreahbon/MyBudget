package com.example.android.mybudget;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.mybudget.data.BudgetContract;
import com.example.android.mybudget.data.BudgetContract.TransEntry;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;

import static android.R.attr.data;
import static android.R.attr.format;
import static android.R.attr.id;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static com.example.android.mybudget.R.id.amount;

/**
 * Created by andre on 7/02/2017.
 */

public class TransCursorAdapter extends CursorAdapter {
    ArrayList<Integer> catIDs = new ArrayList<>();
    ArrayList<String> catNames = new ArrayList<>();
    Currency thisCurrency = Currency.getInstance(Locale.getDefault());
    String currSymbol = thisCurrency.getSymbol();


    public TransCursorAdapter(Context context, Cursor c, ArrayList<Integer> arrCatIDs, ArrayList<String> arrCatNames){
        super(context, c, 0);
        catIDs = arrCatIDs;
        catNames = arrCatNames;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        final CheckBox tvReconciled = (CheckBox) view.findViewById(R.id.reconciled);
        TextView tvCategory = (TextView) view.findViewById(R.id.category); // changed from Description
        TextView tvEstablishment = (TextView) view.findViewById(R.id.establishment);
        TextView tvDate = (TextView) view.findViewById(R.id.date);
        TextView tvAmount = (TextView) view.findViewById(amount);

        final int transID = cursor.getInt(cursor.getColumnIndexOrThrow(TransEntry._ID));
        int reconciledFlag = cursor.getInt(cursor.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_RECONCILEDFLAG));
        int thisCatID = cursor.getInt(cursor.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_CAT));
        String category = "";
        
        for (int j = 0; j < catIDs.size(); j++){
            if (thisCatID == catIDs.get(j)){
                category = catNames.get(j);
            }
        }

        String establishment = cursor.getString(cursor.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_EST));
        Date date = new Date(cursor.getLong(cursor.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_DATE)));
        float amount = cursor.getFloat(cursor.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_AMOUNT));

        tvReconciled.setOnCheckedChangeListener(null);
        if (reconciledFlag != 0){
            tvReconciled.setChecked(true);
        } else {
            tvReconciled.setChecked(false);
        }

        tvCategory.setText(category);
        tvEstablishment.setText(establishment);
        tvDate.setText(df.format(date));
        DecimalFormat dcFormat = new DecimalFormat(currSymbol + "#,##0.00;-" + currSymbol + "#,##0.00");
        tvAmount.setText(dcFormat.format(amount));

        if (amount < 0){
            tvAmount.setTextColor(Color.RED);
        } else {
            tvAmount.setTextColor(ContextCompat.getColor(context,R.color.primaryText));
        }

        tvReconciled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                int newReconcFlag = 0;
                if(tvReconciled.isChecked()){
                    newReconcFlag = 1;
                }
                Date c = new Date(System.currentTimeMillis());
                long todayMilli = c.getTime();

                Uri newUri = ContentUris.withAppendedId(TransEntry.CONTENT_URI, transID);
                ContentValues values = new ContentValues();
                values.put(TransEntry.COLUMN_TRANS_RECONCILEDFLAG, newReconcFlag);
                values.put(TransEntry.COLUMN_TRANS_DATEUPD, todayMilli);

                int updatedRows = context.getContentResolver().update(newUri, values, null, null);
                if (updatedRows > 0) {
                    Toast.makeText(context, context.getString(R.string.toast_updated), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, context.getString(R.string.toast_update_failed), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
