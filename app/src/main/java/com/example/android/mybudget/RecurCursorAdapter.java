package com.example.android.mybudget;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.mybudget.data.BudgetContract.RecurrEntry;

import com.example.android.mybudget.FunctionHelper;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;

/**
 * Created by andre on 7/02/2017.
 */

public class RecurCursorAdapter extends CursorAdapter {
    Currency thisCurrency = Currency.getInstance(Locale.getDefault());
    String currSymbol = thisCurrency.getSymbol();


    public RecurCursorAdapter(Context context, Cursor c){
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.recurring_list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        final CheckBox cbSelected = (CheckBox) view.findViewById(R.id.recur_selected);
        TextView tvDescription = (TextView) view.findViewById(R.id.recur_description);
        TextView tvEstablishment = (TextView) view.findViewById(R.id.recur_establishment);
        TextView tvNextDate = (TextView) view.findViewById(R.id.recur_nextdate);
        TextView tvAmount = (TextView) view.findViewById(R.id.recur_amount);

        final int recID = cursor.getInt(cursor.getColumnIndexOrThrow(RecurrEntry._ID));
        int periodID = cursor.getInt(cursor.getColumnIndexOrThrow(RecurrEntry.COLUMN_PERIOD));
        String description = cursor.getString(cursor.getColumnIndexOrThrow(RecurrEntry.COLUMN_DESCRIPTION));
        String establishment = cursor.getString(cursor.getColumnIndexOrThrow(RecurrEntry.COLUMN_ESTABLISHMENT));
        long currDate = cursor.getLong(cursor.getColumnIndexOrThrow(RecurrEntry.COLUMN_CURRENT_DATE));
        float amount = cursor.getFloat(cursor.getColumnIndexOrThrow(RecurrEntry.COLUMN_AMOUNT));
        long nextDate = cursor.getLong(cursor.getColumnIndexOrThrow(RecurrEntry.COLUMN_NEXT_DATE));

        cbSelected.setOnCheckedChangeListener(null);
        cbSelected.setChecked(false);

        tvDescription.setText(description);
        tvEstablishment.setText(establishment);
        tvNextDate.setText(df.format(nextDate));
        DecimalFormat dcFormat = new DecimalFormat(currSymbol + "#,##0.00;-" + currSymbol + "#,##0.00");
        tvAmount.setText(dcFormat.format(amount));

        if (amount < 0){
            tvAmount.setTextColor(Color.RED);
        } else {
            tvAmount.setTextColor(ContextCompat.getColor(context,R.color.primaryText));
        }
    }

}
