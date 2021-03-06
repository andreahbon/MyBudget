package com.example.android.mybudget;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.mybudget.data.BudgetContract;
import com.example.android.mybudget.data.BudgetContract.RecurrEntry;
import com.example.android.mybudget.data.BudgetContract.TransEntry;
import com.example.android.mybudget.FunctionHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.R.attr.data;

public class ActivityRecurring extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int RECURRING_LOADER = 0;
    RecurCursorAdapter recAdapter;
    ArrayList<Long> selectedRecTrans = new ArrayList<Long>();
    ArrayList<String> selRecNextDate = new ArrayList<String>();
    SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
    long todayMilli;
    boolean anythingSelected = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recurring);

        ListView recListView = (ListView) findViewById(R.id.recur_listview);
        recAdapter = new RecurCursorAdapter(this, null, selectedRecTrans);
        recListView.setAdapter(recAdapter);

        getLoaderManager().initLoader(RECURRING_LOADER, null, this);

        Date c = new Date(System.currentTimeMillis());
        todayMilli = c.getTime();

        recListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckBox cbSelected = (CheckBox) view.findViewById(R.id.recur_selected);
                TextView tvNextDate = (TextView) view.findViewById(R.id.recur_nextdate);
                if(cbSelected.isChecked()){
                    cbSelected.setChecked(false);
                    for(int i = 0; i < selectedRecTrans.size(); i++){
                        if(selectedRecTrans.get(i)== id){
                            selectedRecTrans.remove(i);
                            selRecNextDate.remove(i);
                        }
                        if(selectedRecTrans.size() < 1){
                            anythingSelected = false;
                        }
                    }
                } else {
                    anythingSelected = true;
                    cbSelected.setChecked(true);
                    selectedRecTrans.add(id);
                    selRecNextDate.add(tvNextDate.getText().toString());
                }
                Log.i("Recurring trans", selectedRecTrans.toString());
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(id == RECURRING_LOADER){
            String[] projection = {
                    RecurrEntry._ID,
                    RecurrEntry.COLUMN_PERIOD,
                    RecurrEntry.COLUMN_DESCRIPTION,
                    RecurrEntry.COLUMN_ESTABLISHMENT,
                    RecurrEntry.COLUMN_CURRENT_DATE,
                    RecurrEntry.COLUMN_AMOUNT,
                    RecurrEntry.COLUMN_NEXT_DATE};
            String sortBy = RecurrEntry.COLUMN_NEXT_DATE + " ASC";
            return new CursorLoader(this, RecurrEntry.CONTENT_URI, projection, null, null, sortBy);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        recAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { recAdapter.swapCursor(null);}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_filter, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_filter:
                addRecTransactions();
                finish();
                return true;
            case android.R.id.home:
                if (!anythingSelected) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        NavUtils.navigateUpFromSameTask(ActivityRecurring.this);
                    }
                };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.discard_dialog_msg));
        builder.setPositiveButton(getString(R.string.discard), discardButtonClickListener);
        builder.setNegativeButton(getString(R.string.keep_editing), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (!anythingSelected) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }


    public void addRecTransactions(){
        for(int i = 0; i < selectedRecTrans.size(); i++){
            String[] projection = {
                    RecurrEntry._ID,
                    RecurrEntry.COLUMN_INIT_TRANS_ID,
                    RecurrEntry.COLUMN_CURRENT_DATE,
                    RecurrEntry.COLUMN_DESCRIPTION,
                    RecurrEntry.COLUMN_ESTABLISHMENT,
                    RecurrEntry.COLUMN_ACCOUNT_ID,
                    RecurrEntry.COLUMN_AMOUNT,
                    RecurrEntry.COLUMN_CAT_ID,
                    RecurrEntry.COLUMN_INCOME_TAX,
                    RecurrEntry.COLUMN_PERIOD};
            String selection = RecurrEntry._ID + "=?";
            String[] selectionArgs = {selectedRecTrans.get(i).toString()};
            Cursor cursor = getContentResolver().query(RecurrEntry.CONTENT_URI, projection, selection, selectionArgs, null);
            cursor.moveToFirst();

            Date dateTransDT = null;
            try {
                dateTransDT = df.parse(selRecNextDate.get(i).toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            long dateTrans = dateTransDT.getTime();
            int accNumber = cursor.getInt(cursor.getColumnIndexOrThrow(RecurrEntry.COLUMN_ACCOUNT_ID));

            ContentValues values = new ContentValues();
            values.put(TransEntry.COLUMN_TRANS_RECONCILEDFLAG, 0);
            values.put(TransEntry.COLUMN_TRANS_DATE, dateTrans);
            values.put(TransEntry.COLUMN_TRANS_DATEUPD, todayMilli);
            values.put(TransEntry.COLUMN_TRANS_DESC, cursor.getString(cursor.getColumnIndexOrThrow(RecurrEntry.COLUMN_DESCRIPTION)));
            values.put(TransEntry.COLUMN_TRANS_EST, cursor.getString(cursor.getColumnIndexOrThrow(RecurrEntry.COLUMN_ESTABLISHMENT)));
            values.put(TransEntry.COLUMN_TRANS_ACCOUNT, accNumber);
            values.put(TransEntry.COLUMN_TRANS_CAT, cursor.getInt(cursor.getColumnIndexOrThrow(RecurrEntry.COLUMN_CAT_ID)));
            values.put(TransEntry.COLUMN_TRANS_TAXFLAG, cursor.getInt(cursor.getColumnIndexOrThrow(RecurrEntry.COLUMN_INCOME_TAX)));
            values.put(TransEntry.COLUMN_TRANS_AMOUNT, cursor.getFloat(cursor.getColumnIndexOrThrow(RecurrEntry.COLUMN_AMOUNT)));
            values.put(TransEntry.COLUMN_TRANS_RECURRINGID, selectedRecTrans.get(i));
            Uri insertedTrans = getContentResolver().insert(TransEntry.CONTENT_URI, values);

            values.clear();
            Date nextDate = FunctionHelper.calculateNextDate(cursor.getInt(cursor.getColumnIndexOrThrow(RecurrEntry.COLUMN_PERIOD)), dateTrans);
            long longNextDate = nextDate.getTime();
            values.put(RecurrEntry.COLUMN_CURRENT_DATE, dateTrans);
            values.put(RecurrEntry.COLUMN_NEXT_DATE, longNextDate);
            int updatedRec = getContentResolver().update(RecurrEntry.CONTENT_URI, values, selection, selectionArgs);
            // now, we need to update the account balance after adding the recurring transaction
            int transID = (int) ContentUris.parseId(insertedTrans);
            FunctionHelper.updateBalance(this, transID, dateTrans, dateTrans, accNumber);
        }
    }
}
