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

import com.example.android.mybudget.data.BudgetContract.TransEntry;
import com.example.android.mybudget.data.BudgetContract.CatEntry;
import com.example.android.mybudget.data.BudgetContract.AccEntry;
import com.example.android.mybudget.data.BudgetContract.RecurrEntry;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.R.attr.id;

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
    TextView mDateET, amountSign, mRecExpDate;
    EditText mDescET, mEstET, mAmountET;
    Spinner mAccSpinner, mCatSpinner, mRecPeriodSpinner;

    RadioGroup mTypeRadGroup;
    RadioButton expButton, incButton, transButton;

    LinearLayout mRecurringOptions, mUseAsPayLayout;

    CursorAdapter catAdapter, accAdapter;

    int year, month, day, mDateDialog;
    int transID, recurrID = 0;
    boolean isRecurring;
    long dateTrans;

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

        getLoaderManager().initLoader(CAT_LOADER, null, this);

        mTypeRadGroup = (RadioGroup) findViewById(R.id.trans_type_radgroup);
        mReconcET = (CheckBox) findViewById(R.id.edit_reconc);
        mDateET = (TextView) findViewById(R.id.tv_trans_date);
        mDescET = (EditText) findViewById(R.id.edit_trans_desc);
        mEstET = (EditText) findViewById(R.id.edit_trans_est);
        mAccSpinner = (Spinner) findViewById(R.id.sp_trans_acc);
        mAmountET = (EditText) findViewById(R.id.edit_trans_amount);
        mCatSpinner = (Spinner) findViewById(R.id.sp_trans_cat);
        mIncTaxET = (CheckBox) findViewById(R.id.edit_flag_income);
        mRecurrET = (CheckBox) findViewById(R.id.edit_flag_recurring);

        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        showDate(year, month + 1, day);

        catAdapter = new SpCatCursorAdapter(this, null);
        accAdapter = new SpAccCursorAdapter(this, null);

        expButton = (RadioButton) findViewById(mTypeRadGroup.getChildAt(0).getId());
        incButton = (RadioButton) findViewById(mTypeRadGroup.getChildAt(1).getId());
        transButton = (RadioButton) findViewById(mTypeRadGroup.getChildAt(2).getId());
        expButton.setChecked(true); // setting default transaction type as expense
        amountSign = (TextView) findViewById(R.id.amount_sign);
        amountSign.setText("-$"); // setting the sign to "-"

        mRecurringOptions = (LinearLayout) findViewById(R.id.layout_recoptions);
        mUseAsPayLayout = (LinearLayout) findViewById(R.id.if_income);
        mRecPeriodSpinner = (Spinner) findViewById(R.id.sp_recperiod);
        mRecExpDate = (TextView) findViewById(R.id.tv_recexpdate);
        String[] periodArray = getResources().getStringArray(R.array.period_array);

        ArrayAdapter<CharSequence> periodAdapter = new ArrayAdapter<CharSequence>(this, R.layout.period_spinner_list, R.id.sp_period_option, periodArray);
        mRecPeriodSpinner.setAdapter(periodAdapter);

        mTypeRadGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) { //changes transactions to negative or positive depending on transaction type selected
                if(incButton.isChecked()){
                    amountSign.setText("$");
                    mUseAsPayLayout.setVisibility(View.VISIBLE);
                }
                if(expButton.isChecked()){
                    amountSign.setText("-$");
                    mUseAsPayLayout.setVisibility(View.GONE);
                }
                if(transButton.isChecked()){
                    mUseAsPayLayout.setVisibility(View.GONE);
                }
            }
        });

        mRecurrET.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //TODO: If an existing transaction has this option checked, then it's unchecked, need to flag it, so the related recurring transactions are deleted (both from the
                // TODO: recurring and the main transactions table
                if(mRecurrET.isChecked()){
                    mRecurringOptions.setVisibility(View.VISIBLE);
                    if(incButton.isChecked()){
                        mUseAsPayLayout.setVisibility(View.VISIBLE);
                    } else {
                        mUseAsPayLayout.setVisibility(View.GONE);
                    }
                } else {
                    mRecurringOptions.setVisibility(View.GONE);
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
                        TransEntry.COLUMN_TRANS_AMOUNT};
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

                    transID = data.getInt(data.getColumnIndexOrThrow(TransEntry._ID));
                    mDateET.setText(df.format(date));
                    mDescET.setText(data.getString(data.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_DESC)));
                    mEstET.setText(data.getString(data.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_EST)));
                    int selectedAccount = data.getInt(data.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_ACCOUNT));
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
                    } else {
                        mRecurrET.setChecked(true);
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
                    if(recurrID > 0) {
                        getRecurringInfo(recurrID);
                    }
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
                accAdapter.swapCursor(data);
                mAccSpinner.setSelection(0);
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
        dateTrans = dateTransDT.getTime();

        Date c = new Date(System.currentTimeMillis());
        long todayMilli = c.getTime();

        String descTrans = mDescET.getText().toString().trim();
        String estTrans = mEstET.getText().toString().trim();
        int accTrans = (int) mAccSpinner.getSelectedItemId();
        int catTrans = (int) mCatSpinner.getSelectedItemId();

        int taxFlag = 0;
        if (mIncTaxET.isChecked()) {
            taxFlag = 1;
        }
        isRecurring = false;
        int recurFlag = 0;

        String strAmount = mAmountET.getText().toString().trim();
        if (strAmount.contains("$")) {
            strAmount = strAmount.replace("$", "");
        }
        if (strAmount.contains(",")) {
            strAmount = strAmount.replace(",", "");
        }
        float amountTrans = Float.valueOf(strAmount);
        if(expButton.isChecked()){
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
        values.put(TransEntry.COLUMN_TRANS_RECURRINGID, recurFlag);
        values.put(TransEntry.COLUMN_TRANS_AMOUNT, amountTrans);
        if (mCurrentTransUri != null) {
            int updatedRows = getContentResolver().update(mCurrentTransUri, values, null, null);
            if (updatedRows > 0) {
                Toast.makeText(this, getString(R.string.toast_updated), Toast.LENGTH_SHORT).show();
                if (mRecurrET.isChecked()) {
                    updateRecurring(transID, recurrID);
                }
            } else {
                Toast.makeText(this, getString(R.string.toast_update_failed), Toast.LENGTH_SHORT).show();
            }
        } else {
            Uri newUri = getContentResolver().insert(TransEntry.CONTENT_URI, values);
            if (newUri != null) {
                Toast.makeText(this, getString(R.string.toast_saved), Toast.LENGTH_SHORT).show();
                if (mRecurrET.isChecked()) {
                    transID = (int) ContentUris.parseId(newUri);
                    updateRecurring(transID, recurrID);
                }
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
        // TODO: need to display a dialog and confirm whether to delete the associated recurring transactions (all or just future ones) as well
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
        if(getResources().getResourceName(view.getId()).contains("tv_trans_date")){
            mDateDialog = 999;
            showDialog(999);
        }
        if(getResources().getResourceName(view.getId()).contains("tv_recexpdate")){
            mDateDialog = 900;
            showDialog(900);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 999:
                return new DatePickerDialog(this, myDateListener, year, month, day);
            case 900:
                return new DatePickerDialog(this, myDateListener, year + 1, month, day);
            default:
                return null;
        }
    }

    private DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int thisYear, int thisMonth, int thisDay) {
            showDate(thisYear, thisMonth + 1, thisDay);
        }
    };

    private void showDate(int thisYear, int thisMonth, int thisDay) {
        switch (mDateDialog){
            case 999:
                mDateET.setText(new StringBuilder().append(thisDay).append("/").append(thisMonth).append("/").append(thisYear));
                year = thisYear;
                month = thisMonth -1;
                day = thisDay;
                break;
            case 900:
                mRecExpDate.setText(new StringBuilder().append(thisDay).append("/").append(thisMonth).append("/").append(thisYear));
                break;
        }
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

    private void getRecurringInfo(int recurrID){
        String[] projection11 = {
                RecurrEntry._ID,
                RecurrEntry.COLUMN_INIT_TRANS_ID,
                RecurrEntry.COLUMN_PERIOD,
                RecurrEntry.COLUMN_START_DATE,
                RecurrEntry.COLUMN_EXP_DATE};
        String selection11 = RecurrEntry._ID + "=?";
        String[] selectionArgs = {String.valueOf(recurrID)};
        Cursor recCursor = getContentResolver().query(RecurrEntry.CONTENT_URI, projection11, selection11, selectionArgs, null);

        if(recCursor.getCount()>0){
        //    Toast.makeText(this, "There is a recurring transaction.", Toast.LENGTH_SHORT).show();
        // TODO: retrieve recurring transaction info and populate on screen
        } else {
        //    Toast.makeText(this, "There is no recurring transaction for recurring transaction ID " + recurrID, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateRecurring(int thisTransID, int thisRecurrID){
        String[] projection = {
                RecurrEntry.COLUMN_INIT_TRANS_ID,
                RecurrEntry.COLUMN_PERIOD,
                RecurrEntry.COLUMN_START_DATE,
                RecurrEntry.COLUMN_EXP_DATE};
        Cursor recCursor;
        int selectedPeriod = 1;
        if(mRecPeriodSpinner.getSelectedItemPosition()!= AdapterView.INVALID_POSITION) {
            selectedPeriod = mRecPeriodSpinner.getSelectedItemPosition() + 1;
        }
        ContentValues values = new ContentValues();
        values.put(RecurrEntry.COLUMN_INIT_TRANS_ID, thisTransID);
        values.put(RecurrEntry.COLUMN_PERIOD, selectedPeriod);
        values.put(RecurrEntry.COLUMN_START_DATE, dateTrans);
        values.put(RecurrEntry.COLUMN_EXP_DATE, mRecExpDate.getText().toString());
        if(thisRecurrID!=0){
            String selection = RecurrEntry._ID + "=?";
            String[] selectionArgs = {String.valueOf(thisRecurrID)};
            recCursor = getContentResolver().query(RecurrEntry.CONTENT_URI, projection, selection, selectionArgs, null);
            if(recCursor.getCount()>0){
                Log.i("UpdatingRecurring", String.valueOf(values));
                Uri mRecurrUri = new ContentUris().withAppendedId(RecurrEntry.CONTENT_URI, thisRecurrID);
                int updatedRows = getContentResolver().update(mRecurrUri, values, null, null);
                if(updatedRows<1){
                    Toast.makeText(this, getString(R.string.toast_rec_update_failed), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Log.i("InsertingRecurring", String.valueOf(values));
            Uri insertedRowUri = getContentResolver().insert(RecurrEntry.CONTENT_URI, values);
            if (insertedRowUri==null){
                Toast.makeText(this, getString(R.string.toast_rec_add_failed), Toast.LENGTH_SHORT).show();
            } else {
                addRecurringTransactions(true, thisTransID, thisRecurrID);
            }
        }
    }

    private void addRecurringTransactions(boolean isNewRec, int thisTransID, int thisRecurrID){
        if(isNewRec){

        }
    }
}
