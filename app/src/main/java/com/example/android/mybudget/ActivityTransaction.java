package com.example.android.mybudget;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.ContentUris;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.mybudget.data.BudgetContract;
import com.example.android.mybudget.data.BudgetContract.TransEntry;
import com.example.android.mybudget.data.BudgetContract.CatEntry;
import com.example.android.mybudget.data.BudgetContract.AccEntry;
import com.example.android.mybudget.data.BudgetContract.RecurrEntry;

import com.example.android.mybudget.FunctionHelper;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ActivityTransaction extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    //TODO: Add "Discard changes" dialog when back button is pressed

    static final int BUD_LOADER = 0;
    static final int CAT_LOADER = 1;
    static final int ACC_LOADER = 2;
    Uri mCurrentTransUri;

    SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
    SimpleDateFormat dfYear = new SimpleDateFormat("yyyy");
    SimpleDateFormat dfMonth = new SimpleDateFormat("MM");
    SimpleDateFormat dfDay = new SimpleDateFormat("dd");

    CheckBox mReconcET, mIncTaxET, mRecurrET;
    TextView mDateET, amountSign, mLabelAcc;
    EditText mDescET, mEstET, mAmountET;
    Spinner mAccSpinner, mAccToSpinner, mCatSpinner, mPeriodSpinner;

    RadioGroup mTypeRadGroup;
    RadioButton expButton, incButton, transButton;
    LinearLayout mRecurLayout, mAccToLayout, mIsRecurring;

    CursorAdapter catAdapter, accAdapter;

    int year, month, day, transID, mSelectedPeriod, accTrans;
    int recurrID = 0;
    long oldDate, newDate;
    float accBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        Intent i = getIntent();
        mCurrentTransUri = i.getData();

        if (mCurrentTransUri != null) {
            setTitle("Edit transaction");
        } else {
            setTitle("Add a transaction");
        }

        oldDate =  32535090000000L;
        getLoaderManager().initLoader(CAT_LOADER, null, this);

        mReconcET = (CheckBox) findViewById(R.id.edit_reconc);
        mDateET = (TextView) findViewById(R.id.tv_trans_date);
        mDescET = (EditText) findViewById(R.id.edit_trans_desc);
        mEstET = (EditText) findViewById(R.id.edit_trans_est);
        mLabelAcc = (TextView) findViewById(R.id.label_acc);
        mAccSpinner = (Spinner) findViewById(R.id.sp_trans_acc);
        mAccToSpinner = (Spinner)findViewById(R.id.sp_trans_acc_to);
        mAmountET = (EditText) findViewById(R.id.edit_trans_amount);
        mCatSpinner = (Spinner) findViewById(R.id.sp_trans_cat);
        mIncTaxET = (CheckBox) findViewById(R.id.edit_flag_income);
        mIsRecurring = (LinearLayout) findViewById(R.id.layout_isrecurring);
        mRecurrET = (CheckBox) findViewById(R.id.edit_flag_recurring);

        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        showDate(year, month + 1, day);

        catAdapter = new SpCatCursorAdapter(this, null);
        accAdapter = new SpAccCursorAdapter(this, null);

        mTypeRadGroup = (RadioGroup) findViewById(R.id.trans_type_radgroup);
        expButton = (RadioButton) findViewById(mTypeRadGroup.getChildAt(0).getId());
        incButton = (RadioButton) findViewById(mTypeRadGroup.getChildAt(1).getId());
        transButton = (RadioButton) findViewById(mTypeRadGroup.getChildAt(2).getId());
        expButton.setChecked(true); // setting default transaction type as expense
        amountSign = (TextView) findViewById(R.id.amount_sign);
        amountSign.setText("-$"); // setting the sign to "-"
        mAccToLayout = (LinearLayout) findViewById(R.id.layout_accto);
        mAccToLayout.setVisibility(View.GONE); // hiding the "Account To" field as the default type of transaction is expense, not transfer

        mRecurLayout = (LinearLayout) findViewById(R.id.layout_recurring);
        mPeriodSpinner = (Spinner) findViewById(R.id.sp_rec_period);
        ArrayAdapter<CharSequence> periodAdapter = new ArrayAdapter<>(this, R.layout.period_spinner_list, R.id.sp_period_option);
        periodAdapter.addAll(getResources().getTextArray(R.array.period_array));
        mPeriodSpinner.setAdapter(periodAdapter);

        mTypeRadGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) { //changes transactions to negative or positive depending on transaction type selected
                if(incButton.isChecked()){
                    amountSign.setText("$");
                    mLabelAcc.setText(getString(R.string.trans_acc));
                    mAccToLayout.setVisibility(View.GONE);
                    mIsRecurring.setVisibility(View.VISIBLE);
                }
                if(expButton.isChecked()){
                    amountSign.setText("-$");
                    mLabelAcc.setText(getString(R.string.trans_acc));
                    mAccToLayout.setVisibility(View.GONE);
                    mIsRecurring.setVisibility(View.VISIBLE);
                }
                if(transButton.isChecked()){ // if Transfer is selected, change the "Account" label to "Account from" and show the "Account to" field
                    mLabelAcc.setText(getString(R.string.trans_acc_from));
                    mAccToLayout.setVisibility(View.VISIBLE);
                    mIsRecurring.setVisibility(View.GONE);
                }
            }
        });

        mRecurrET.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mRecurrET.isChecked()){
                    mRecurLayout.setVisibility(View.VISIBLE);
                } else {
                    mRecurLayout.setVisibility(View.GONE);
                }
            }
        });
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
                        TransEntry.COLUMN_TRANS_RECURRINGID,
                        TransEntry.COLUMN_TRANS_AMOUNT,
                        TransEntry.COLUMN_ACCOUNT_BALANCE};
                return new CursorLoader(this, mCurrentTransUri, projection, null, null, null);
            case CAT_LOADER:
                String[] projection2 = {
                        CatEntry._ID,
                        CatEntry.COLUMN_CATNAME};
                String sortBy = CatEntry.COLUMN_CATNAME + " ASC";
                return new CursorLoader(this, CatEntry.CONTENT_URI, projection2, null, null, sortBy);
            case ACC_LOADER:
                String[] projection3 = {
                        AccEntry._ID,
                        AccEntry.COLUMN_ACCNAME};
                String sortBy3 = AccEntry._ID + " ASC";
                return new CursorLoader(this, AccEntry.CONTENT_URI, projection3, null, null, sortBy3);
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
                DecimalFormat dcFormat = new DecimalFormat("#,##0.00;#,##0.00");

                if (data.moveToFirst()) {
                    accBalance = data.getFloat(data.getColumnIndexOrThrow(TransEntry.COLUMN_ACCOUNT_BALANCE));
                    transID = data.getInt(data.getColumnIndexOrThrow(TransEntry._ID));
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
                    oldDate = data.getLong(data.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_DATE));

                    mDateET.setText(df.format(date));
                    mDescET.setText(data.getString(data.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_DESC)));
                    mEstET.setText(data.getString(data.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_EST)));
                    int selectedAccount = data.getInt(data.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_ACCOUNT));
                    accTrans = selectedAccount;
                    int selectedCategory = data.getInt(data.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_CAT));

                    int incTaxFlag = data.getInt(data.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_TAXFLAG));
                    if (incTaxFlag == 0) {
                        mIncTaxET.setChecked(false);
                    } else {
                        mIncTaxET.setChecked(true);
                    }
                    recurrID = data.getInt(data.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_RECURRINGID));
                    if (recurrID == 0) {
                        mRecurrET.setChecked(false);
                        mRecurLayout.setVisibility(View.GONE);
                    } else {
                        mRecurrET.setChecked(true);
                        mRecurLayout.setVisibility(View.VISIBLE);
                        String[] projection = {RecurrEntry._ID, RecurrEntry.COLUMN_PERIOD};
                        String selection = RecurrEntry._ID + "=?";
                        String[] selectionArgs = {String.valueOf(recurrID)};
                        Cursor cursor = getContentResolver().query(ContentUris.withAppendedId(RecurrEntry.CONTENT_URI, recurrID), projection, selection, selectionArgs, null);
                        cursor.moveToFirst();
                        mSelectedPeriod = cursor.getInt(cursor.getColumnIndexOrThrow(RecurrEntry.COLUMN_PERIOD));
                        int selectPeriodPos = mSelectedPeriod - 1;
                        mPeriodSpinner.setSelection(selectPeriodPos);
                    }
                    float thisAmount = data.getFloat(data.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_AMOUNT));
                    if (thisAmount < 0){
                        thisAmount = Math.abs(thisAmount);
                        amountSign.setText("-$");
                        expButton.setChecked(true);
                    } else {
                        amountSign.setText("$");
                        incButton.setChecked(true);
                    }
                    mAmountET.setText(dcFormat.format(thisAmount));

                    getCategoryPosition(selectedCategory);
                    getAccountPosition(selectedAccount);
                }
                break;
            case CAT_LOADER:
                mCatSpinner.setAdapter(catAdapter);
                catAdapter.swapCursor(data);
                mCatSpinner.setSelection(0);
                getLoaderManager().initLoader(ACC_LOADER, null, this);
                break;
            case ACC_LOADER:
                mAccSpinner.setAdapter(accAdapter);
                mAccToSpinner.setAdapter(accAdapter);
                accAdapter.swapCursor(data);
                mAccSpinner.setSelection(0);
                mAccToSpinner.setSelection(0);
                if (mCurrentTransUri != null) {
                    getLoaderManager().initLoader(BUD_LOADER, null, this);
                }
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
                mAccSpinner.setSelection(0);
                mCatSpinner.setSelection(0);
                mIncTaxET.setChecked(false);
                mRecurrET.setChecked(false);
                mAmountET.setText(null);
                break;
            case CAT_LOADER:
                loader.reset();
                catAdapter.swapCursor(null);
                break;
            case ACC_LOADER:
                loader.reset();
                accAdapter.swapCursor(null);
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
        newDate = dateTrans;

        Date c = new Date(System.currentTimeMillis());
        long todayMilli = c.getTime();

        String descTrans = mDescET.getText().toString().trim();
        String estTrans = mEstET.getText().toString().trim();
        accTrans = (int) mAccSpinner.getSelectedItemId();
        int catTrans = (int) mCatSpinner.getSelectedItemId();

        int taxFlag = 0;
        if (mIncTaxET.isChecked()) {
            taxFlag = 1;
        }
        String strAmount = mAmountET.getText().toString().trim();
        if (strAmount.contains("$")) {
            strAmount = strAmount.replace("$", "");
        }
        if (strAmount.contains(",")) {
            strAmount = strAmount.replace(",", "");
        }
        float amountTrans = Float.valueOf(strAmount);
        if(expButton.isChecked() || transButton.isChecked()){
            amountTrans *= -1;
        }

        ContentValues values = new ContentValues();
        values.put(TransEntry.COLUMN_TRANS_RECONCILEDFLAG, reconFlag);
        values.put(TransEntry.COLUMN_TRANS_DATE, dateTrans);
        values.put(TransEntry.COLUMN_TRANS_DATEUPD, todayMilli);
        values.put(TransEntry.COLUMN_TRANS_DESC, descTrans);
        values.put(TransEntry.COLUMN_TRANS_EST, estTrans);
        values.put(TransEntry.COLUMN_TRANS_ACCOUNT, accTrans);
        values.put(TransEntry.COLUMN_TRANS_CAT, catTrans);
        values.put(TransEntry.COLUMN_TRANS_TAXFLAG, taxFlag);
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
                transID = (int) ContentUris.parseId(newUri);
            } else {
                Toast.makeText(this, getString(R.string.toast_save_failed), Toast.LENGTH_SHORT).show();
            }
        }
        updateRecurring(transID, values);
        FunctionHelper.updateBalance(this, transID, oldDate, newDate, accTrans);
        if (transButton.isChecked()){
            values.remove(TransEntry.COLUMN_TRANS_ACCOUNT);
            values.remove(TransEntry.COLUMN_TRANS_AMOUNT);
            accTrans = (int) mAccToSpinner.getSelectedItemId();
            amountTrans *= -1;
            values.put(TransEntry.COLUMN_TRANS_ACCOUNT, accTrans);
            values.put(TransEntry.COLUMN_TRANS_AMOUNT, amountTrans);
            Uri newUri = getContentResolver().insert(TransEntry.CONTENT_URI, values);
            if (newUri != null) {
                Toast.makeText(this, getString(R.string.toast_saved), Toast.LENGTH_SHORT).show();
                transID = (int) ContentUris.parseId(newUri);
            } else {
                Toast.makeText(this, getString(R.string.toast_save_failed), Toast.LENGTH_SHORT).show();
            }
            updateRecurring(transID, values);
            FunctionHelper.updateBalance(this, transID, oldDate, newDate, accTrans);
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
            // deleting any recurring transaction first
            if(recurrID!=0) {
                String[] projection = {TransEntry._ID, TransEntry.COLUMN_TRANS_RECURRINGID,
                        TransEntry.COLUMN_TRANS_DATE};
                String selection = TransEntry.COLUMN_TRANS_RECURRINGID + "=?";
                String[] selectionArgs = {String.valueOf(recurrID)};
                String sortBy = TransEntry.COLUMN_TRANS_DATE + " DESC";
                // first, check how many transactions there are with this recurring ID
                Cursor cursor = getContentResolver().query(TransEntry.CONTENT_URI, projection, selection, selectionArgs, sortBy);
                cursor.moveToFirst();
                // if there is only one, delete the recurring transaction
                if (cursor.getCount() == 1) {
                    String selectionRec = RecurrEntry._ID + "=?";
                    int deletedRecRows = getContentResolver().delete(ContentUris.withAppendedId(RecurrEntry.CONTENT_URI, recurrID), selectionRec, selectionArgs);
                } else {
                    // if there are more than one transactions with this rec ID, but this one is the latest one, update the next date in the
                    // recurring table with a new next date = date of second-to-last transaction + period
                    if (cursor.getCount() > 1 && cursor.getInt(cursor.getColumnIndexOrThrow(TransEntry._ID)) == transID) {
                        cursor.moveToNext();
                        long longCurrDate = cursor.getLong(cursor.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_DATE));
                        Date nextDate = FunctionHelper.calculateNextDate(mSelectedPeriod, longCurrDate);
                        Log.i("longCurrDate", "longCurrDate = " + longCurrDate);
                        long longNextDate = nextDate.getTime();

                        ContentValues valuesRec = new ContentValues();
                        valuesRec.put(RecurrEntry.COLUMN_NEXT_DATE, longNextDate);
                        String selectionRec = RecurrEntry._ID + "=?";
                        String[] selectionArgsRec = {String.valueOf(recurrID)};
                        int updatedRec = getContentResolver().update(RecurrEntry.CONTENT_URI, valuesRec, selectionRec, selectionArgsRec);
                    }
                }
            }

            int deletedRows = getContentResolver().delete(mCurrentTransUri, null, null);
            if (deletedRows > 0) {
                Toast.makeText(this, getString(R.string.toast_trans_deleted), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.toast_trans_delete_failed), Toast.LENGTH_SHORT).show();
            }
            FunctionHelper.updateBalance(this, transID, oldDate, oldDate, accTrans);
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
        for (int position = 0; position < catAdapter.getCount(); position++) {
            if (catAdapter.getItemId(position) == catID) {
                mCatSpinner.setSelection(position);
            }
        }
    }
    private void getAccountPosition(int accID) {
        for (int position = 0; position < accAdapter.getCount(); position++) {
            if (accAdapter.getItemId(position) == accID) {
                mAccSpinner.setSelection(position);
            }
        }
    }

    private void updateRecurring(int thisTransID, ContentValues transValues){
        if(mRecurrET.isChecked()){
            int selectedPeriod = 1;
            if(mPeriodSpinner.getSelectedItemPosition()!= AdapterView.INVALID_POSITION){
                selectedPeriod = mPeriodSpinner.getSelectedItemPosition() + 1;
            }
            Date nextDate = FunctionHelper.calculateNextDate(selectedPeriod, transValues.getAsLong(TransEntry.COLUMN_TRANS_DATE));
            long longDate = nextDate.getTime();

            ContentValues values = new ContentValues();
            values.put(RecurrEntry.COLUMN_INIT_TRANS_ID, thisTransID);
            values.put(RecurrEntry.COLUMN_CURRENT_DATE, transValues.getAsLong(TransEntry.COLUMN_TRANS_DATE));
            values.put(RecurrEntry.COLUMN_DESCRIPTION, transValues.getAsString(TransEntry.COLUMN_TRANS_DESC));
            values.put(RecurrEntry.COLUMN_ESTABLISHMENT, transValues.getAsString(TransEntry.COLUMN_TRANS_EST));
            values.put(RecurrEntry.COLUMN_ACCOUNT_ID, transValues.getAsInteger(TransEntry.COLUMN_TRANS_ACCOUNT));
            values.put(RecurrEntry.COLUMN_AMOUNT, transValues.getAsFloat(TransEntry.COLUMN_TRANS_AMOUNT));
            values.put(RecurrEntry.COLUMN_CAT_ID, transValues.getAsInteger(TransEntry.COLUMN_TRANS_CAT));
            values.put(RecurrEntry.COLUMN_INCOME_TAX, transValues.getAsInteger(TransEntry.COLUMN_TRANS_TAXFLAG));
            values.put(RecurrEntry.COLUMN_PERIOD, selectedPeriod);
            values.put(RecurrEntry.COLUMN_NEXT_DATE, longDate);
            if(recurrID==0) {
                Uri insertUri = getContentResolver().insert(RecurrEntry.CONTENT_URI, values);
                if(insertUri!=null){
                    Toast.makeText(this, getString(R.string.toast_rec_added), Toast.LENGTH_SHORT).show();
                    values.clear();
                    values.put(TransEntry.COLUMN_TRANS_RECURRINGID, ContentUris.parseId(insertUri));
                    String selection = TransEntry._ID + "=?";
                    String[] selectionArgs = {String.valueOf(thisTransID)};
                    int updateTrans = getContentResolver().update(ContentUris.withAppendedId(TransEntry.CONTENT_URI, thisTransID), values,
                        selection, selectionArgs);
                } else {
                    Toast.makeText(this, getString(R.string.toast_rec_add_failed), Toast.LENGTH_SHORT).show();
                }
            } else {
                String selection = RecurrEntry._ID + "=?";
                String[] selectionArgs = {String.valueOf(recurrID)};
                int updateRec = getContentResolver().update(ContentUris.withAppendedId(RecurrEntry.CONTENT_URI, recurrID), values, selection, selectionArgs);
            }
        } else {
            String selection = RecurrEntry._ID + "=?";
            String[] selectionArgs = {String.valueOf(recurrID)};
            int deletedRows = getContentResolver().delete(ContentUris.withAppendedId(RecurrEntry.CONTENT_URI, recurrID), selection, selectionArgs);
            ContentValues values2 = new ContentValues();
            values2.put(TransEntry.COLUMN_TRANS_RECURRINGID, 0);
            String selection2 = TransEntry._ID + "=?";
            String[] selectionArgs2 = {String.valueOf(thisTransID)};
            int updateTrans2 = getContentResolver().update(ContentUris.withAppendedId(TransEntry.CONTENT_URI, thisTransID), values2,
                    selection2, selectionArgs2);
        }
    }
}
