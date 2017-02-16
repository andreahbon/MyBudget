package com.example.android.mybudget;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.mybudget.data.BudgetContract.TransEntry;
import com.example.android.mybudget.data.BudgetContract.CatEntry;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ActivityTransaction extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int BUD_LOADER = 0;
    private static final int CAT_LOADER = 1;
    private Uri mCurrentTransUri;

    private SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
    private SimpleDateFormat dfYear = new SimpleDateFormat("yyyy");
    private SimpleDateFormat dfMonth = new SimpleDateFormat("MM");
    private SimpleDateFormat dfDay = new SimpleDateFormat("dd");

    private CheckBox mReconcET;
    private TextView mDateET;
    private EditText mDescET;
    private EditText mEstET;
    private EditText mAccET;
    private EditText mAmountET;
    private Spinner mCatSpinner;
    private CheckBox mIncTaxET;
    private CheckBox mRecurrET;

    private CursorAdapter adapter;
    private int selectedCategory;

    private boolean isCatLoaderfinished;
    private boolean isBudLoaderfinished;

    private Calendar calendar;
    private int year, month, day;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        Intent i = getIntent();
        mCurrentTransUri = i.getData();

        if (mCurrentTransUri != null) {
            setTitle("Edit transaction");
            getLoaderManager().initLoader(BUD_LOADER, null, this);
        } else {
            setTitle("Add a transaction");
        }

        getLoaderManager().initLoader(CAT_LOADER, null, this);

        mReconcET = (CheckBox) findViewById(R.id.edit_reconc);
        mDateET = (TextView) findViewById(R.id.tv_trans_date);
        mDescET = (EditText) findViewById(R.id.edit_trans_desc);
        mEstET = (EditText) findViewById(R.id.edit_trans_est);
        mAccET = (EditText) findViewById(R.id.edit_trans_acc);
        mAmountET = (EditText) findViewById(R.id.edit_trans_amount);
        mCatSpinner = (Spinner) findViewById(R.id.sp_trans_cat);
        mIncTaxET = (CheckBox) findViewById(R.id.edit_flag_income);
        mRecurrET = (CheckBox) findViewById(R.id.edit_flag_recurring);

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        showDate(year, month + 1, day);

        adapter = new SpCatCursorAdapter(this, null);
        mCatSpinner.setAdapter(adapter);

/*
        mCatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedCategory = (int) (long) l;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectedCategory = 0;
            }
        });
*/
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        switch (i) {
            case BUD_LOADER:
                String[] projection = {
                        TransEntry._ID,
                        TransEntry.COLUMN_TRANS_RECONCILEDFLAG,
                        TransEntry.COLUMN_TRANS_DESC,
                        TransEntry.COLUMN_TRANS_EST,
                        TransEntry.COLUMN_TRANS_DATE,
                        TransEntry.COLUMN_TRANS_ACCOUNT,
                        TransEntry.COLUMN_TRANS_CAT,
                        TransEntry.COLUMN_TRANS_TAXFLAG,
                        TransEntry.COLUMN_TRANS_DATEUPD,
                        TransEntry.COLUMN_TRANS_RECURRINGFLAG,
                        TransEntry.COLUMN_TRANS_AMOUNT};
                return new CursorLoader(this, mCurrentTransUri, projection, null, null, null);
            case CAT_LOADER:
                String[] projection2 = {
                        CatEntry._ID,
                        CatEntry.COLUMN_CATNAME};
                String sortBy = CatEntry.COLUMN_CATNAME + " ASC";
                return new CursorLoader(this, CatEntry.CONTENT_URI, projection2, null, null, sortBy);
            default:
                return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_transactions, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentTransUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                try {
                    saveTrans();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return true;

            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
/*            case android.R.id.home:
                if (!mProdHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        NavUtils.navigateUpFromSameTask(DetailsActivity.this);
                    }
                };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
*/
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case BUD_LOADER:
                DecimalFormat dcFormat = new DecimalFormat("$#,##0.00;-$#,##0.00");

                if (data.moveToFirst()) {
                    int reconcFlag = data.getInt(data.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_RECONCILEDFLAG));
                    if (reconcFlag == 0) {
                        mReconcET.setChecked(false);
                    } else {
                        mReconcET.setChecked(true);
                    }
                    Date date = new Date(data.getLong(data.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_DATE)));
                    year = Integer.parseInt(dfYear.format(date));
                    month = Integer.parseInt(dfMonth.format(date)) - 1;
                    day = Integer.parseInt(dfDay.format(date));
                    ;

                    mDateET.setText(df.format(date));
                    mDescET.setText(data.getString(data.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_DESC)));
                    mEstET.setText(data.getString(data.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_EST)));
                    mAccET.setText(Integer.toString(data.getInt(data.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_ACCOUNT))));
                    selectedCategory = data.getInt(data.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_CAT));

                    int incTaxFlag = data.getInt(data.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_TAXFLAG));
                    if (incTaxFlag == 0) {
                        mIncTaxET.setChecked(false);
                    } else {
                        mIncTaxET.setChecked(true);
                    }
                    int recurrFlag = data.getInt(data.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_RECURRINGFLAG));
                    if (recurrFlag == 0) {
                        mRecurrET.setChecked(false);
                    } else {
                        mRecurrET.setChecked(true);
                    }
                    mAmountET.setText(dcFormat.format(data.getFloat(data.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_AMOUNT))));
                    if (isCatLoaderfinished){
                        getCategoryPosition(selectedCategory);
                    }
                    isBudLoaderfinished = true;
                }
                break;
            case CAT_LOADER:
                adapter.swapCursor(data);
                if (isBudLoaderfinished){
                    getCategoryPosition(selectedCategory);
                }
                isCatLoaderfinished = true;
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case BUD_LOADER:
                loader.reset();
                mReconcET.setChecked(false);
                mDateET.setText(null);
                mDescET.setText(null);
                mEstET.setText(null);
                mAccET.setText(null);
                mCatSpinner.setSelection(0);
                mIncTaxET.setChecked(false);
                mRecurrET.setChecked(false);
                mAmountET.setText(null);
                break;
            case CAT_LOADER:
                loader.reset();
                adapter.swapCursor(null);
                break;
        }
    }

    private void saveTrans() throws ParseException {
        int reconFlag = 0;
        if (mReconcET.isChecked()) {
            reconFlag = 1;
        }

        String dateTransSt = mDateET.getText().toString();
        Date dateTransDT = df.parse(dateTransSt);
        long dateTrans = dateTransDT.getTime();

        Date c = new Date(System.currentTimeMillis());
        long todayMilli = c.getTime();

        String descTrans = mDescET.getText().toString().trim();
        String estTrans = mEstET.getText().toString().trim();
        int accTrans = Integer.parseInt(mAccET.getText().toString().trim());
        int catTrans = (int) mCatSpinner.getSelectedItemId();

        int taxFlag = 0;
        if (mIncTaxET.isChecked()) {
            taxFlag = 1;
        }
        int recurFlag = 0;
        if (mRecurrET.isChecked()) {
            recurFlag = 1;
        }

        String strAmount = mAmountET.getText().toString().trim();
        if (strAmount.contains("$")) {
            strAmount = strAmount.replace("$", "");
        }
        if (strAmount.contains(",")) {
            strAmount = strAmount.replace(",", "");
        }
        float amountTrans = Float.valueOf(strAmount);

        ContentValues values = new ContentValues();
        values.put(TransEntry.COLUMN_TRANS_RECONCILEDFLAG, reconFlag);
        values.put(TransEntry.COLUMN_TRANS_DATE, dateTrans);
        values.put(TransEntry.COLUMN_TRANS_DATEUPD, todayMilli);
        values.put(TransEntry.COLUMN_TRANS_DESC, descTrans);
        values.put(TransEntry.COLUMN_TRANS_EST, estTrans);
        values.put(TransEntry.COLUMN_TRANS_ACCOUNT, accTrans);
        values.put(TransEntry.COLUMN_TRANS_CAT, catTrans);
        values.put(TransEntry.COLUMN_TRANS_TAXFLAG, taxFlag);
        values.put(TransEntry.COLUMN_TRANS_RECURRINGFLAG, recurFlag);
        values.put(TransEntry.COLUMN_TRANS_AMOUNT, amountTrans);
        if (mCurrentTransUri != null) {
            int updatedRows = getContentResolver().update(mCurrentTransUri, values, null, null);
            if (updatedRows > 0) {
                Toast.makeText(this, getString(R.string.toast_updated), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.toast_update_failed), Toast.LENGTH_SHORT).show();
            }
        } else {
            Uri newUri = getContentResolver().insert(TransEntry.CONTENT_URI, values);
            if (newUri != null) {
                Toast.makeText(this, getString(R.string.toast_saved), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.toast_save_failed), Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_trans_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteTrans();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteTrans() {
        if (mCurrentTransUri != null) {
            int deletedRows = getContentResolver().delete(mCurrentTransUri, null, null);
            if (deletedRows > 0) {
                Toast.makeText(this, getString(R.string.toast_trans_deleted), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.toast_trans_delete_failed), Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    @SuppressWarnings("deprecation")
    public void showDatePickerDialog(View view) {
        showDialog(999);
    }

    @Override
    @SuppressWarnings("deprecation")
    protected Dialog onCreateDialog(int id) {
        if (id == 999) {
            return new DatePickerDialog(this, myDateListener, year, month, day);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int thisYear, int thisMonth, int thisDay) {
            showDate(thisYear, thisMonth + 1, thisDay);
        }
    };

    private void showDate(int year, int month, int day) {
        mDateET.setText(new StringBuilder().append(day).append("/").append(month).append("/").append(year));
    }

    private void getCategoryPosition(int catID) {
        for (int position = 0; position < adapter.getCount(); position++) {
            if (adapter.getItemId(position) == catID) {
                mCatSpinner.setSelection(position);
            }
        }
    }
}
