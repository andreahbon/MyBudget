package com.example.android.mybudget;

import android.Manifest;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.mybudget.data.BudgetContract.TransEntry;
import com.example.android.mybudget.data.BudgetContract.CatEntry;
import com.example.android.mybudget.data.BudgetContract.FilterAccEntry;
import com.example.android.mybudget.data.BudgetContract.FilterCatEntry;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int REQUEST_WRITE_STORAGE = 112;
    public static final String PREFS_NAME = "DatePrefs";
    Currency thisCurrency = Currency.getInstance(Locale.getDefault());
    String currSymbol = thisCurrency.getSymbol();

    public int REQUEST_FILE_GET = 1;
    public int CHANGE_FILTER = 2;
    public int CHANGE_DATE_FILTER = 3;
    private static final int BUDGET_LOADER = 0;
    private static final int CAT_LOADER = 1;
    private static final int FILTER_ACC_LOADER = 2;
    private static final int FILTER_CAT_LOADER = 3;
    private TransCursorAdapter budAdapter;

    private ArrayList<Integer> catIDs = new ArrayList<>();
    private ArrayList<String> catNames = new ArrayList<>();
    ArrayList<Integer> accIDs = new ArrayList<>();

    private String filterAccSelection = "";
    private String filterCatSelection = "";

    String stDateFrom = "";
    String stDateTo = "";
    String stFilterType = "";
    private long dateFrom = 0;
    private long dateTo = 0;

    TextView tvDateFilter;
    ImageView prevButton, nextButton;
    ListView transListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvDateFilter = (TextView) findViewById(R.id.date_filter);
        prevButton = (ImageView) findViewById(R.id.prev_button);
        nextButton = (ImageView) findViewById(R.id.next_button);
        readPrefs();

        boolean hasPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_trans);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ActivityTransaction.class);
                startActivity(intent);
            }
        });

        transListView = (ListView) findViewById(R.id.trans_listview);
        budAdapter = new TransCursorAdapter(this, null, catIDs, catNames);
        transListView.setAdapter(budAdapter);

        // set alarm for monthly notification to back up data
        FunctionHelper.triggerAlarm(this);

        getLoaderManager().initLoader(FILTER_ACC_LOADER, null, this);

        tvDateFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent4 = new Intent(v.getContext(), ActivityDateFilter.class);
                startActivityForResult(intent4, CHANGE_DATE_FILTER);
            }
        });

        transListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Uri prodUri = new ContentUris().withAppendedId(TransEntry.CONTENT_URI, id);
                Intent i = new Intent(MainActivity.this, ActivityTransaction.class);
                i.setData(prodUri);
                startActivity(i);
            }
        });

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String prevPeriod = FunctionHelper.calculatePrevOrNextMonth("prev", dateFrom);
                goToPrevNextMonth(prevPeriod);
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String nextPeriod = FunctionHelper.calculatePrevOrNextMonth("next", dateFrom);
                goToPrevNextMonth(nextPeriod);
            }
        });
    }

    public void goToPrevNextMonth(String prevOrNextPeriod){
        String[] dates = prevOrNextPeriod.split("-");
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        long startDate = 0;
        long endDate = 0;
        try {
            startDate = df.parse(dates[0]).getTime();
            endDate = df.parse(dates[1]).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SharedPreferences datefilter = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = datefilter.edit();
        editor.putString("stDateFrom", dates[0]);
        editor.putString("stDateTo", dates[1]);
        editor.putLong("dateFrom", startDate);
        editor.putLong("dateTo", endDate);
        editor.commit();
        readPrefs();
        refresh_activity();
    }

    public void refresh_activity(){
        getLoaderManager().restartLoader(BUDGET_LOADER, null, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted, I guess?", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission denied.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_importcsv:
                importCSVfile();
                return true;
            case R.id.action_exportcsv:
                FunctionHelper.exportToCSV(this);
                return true;
            case R.id.action_mngcat:
                Intent intent = new Intent(this, ActivityCategory.class);
                startActivity(intent);
                return true;
            case R.id.action_mngacc:
                Intent intent2 = new Intent(this, ActivityAccounts.class);
                startActivity(intent2);
                return true;
            case R.id.action_recurring:
                Intent intent4 = new Intent(this, ActivityRecurring.class);
                startActivity(intent4);
                return true;
            case R.id.action_filter:
                Intent intent3 = new Intent(this, ActivityFilter.class);
                startActivityForResult(intent3, CHANGE_FILTER);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void importCSVfile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_FILE_GET);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if intent is to get CSV file and a file is found
        if (requestCode == REQUEST_FILE_GET && resultCode == RESULT_OK) {
            try {
                InputStream iStream = getContentResolver().openInputStream(data.getData());
                BufferedReader buffer = new BufferedReader(new InputStreamReader(iStream));
                saveCSVToDatabase(buffer);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e("MainActivity", "Couldn't find the file");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("MainActivity", "Couldn't read (?) the file");
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (requestCode == CHANGE_FILTER && resultCode == RESULT_OK) {
            Intent refresh = new Intent(this, MainActivity.class);
            startActivity(refresh);
            this.finish();
        }
        if (requestCode == CHANGE_DATE_FILTER && resultCode == RESULT_OK) {
            readPrefs();
            refresh_activity();
        }
    }

    public void readPrefs(){
        SharedPreferences dateFilter = getSharedPreferences(PREFS_NAME, 0);
        stDateFrom = dateFilter.getString("stDateFrom", "");
        stDateTo = dateFilter.getString("stDateTo", "");
        stFilterType = dateFilter.getString("stFilterType", "All transactions");
        dateFrom = dateFilter.getLong("dateFrom", 0);
        dateTo = dateFilter.getLong("dateTo", 0);
        String dateText;
        prevButton.setVisibility(View.INVISIBLE);
        nextButton.setVisibility(View.INVISIBLE);
        if (stFilterType.equals("All transactions")){
            dateText = getString(R.string.all_transactions);
        } else {
            if (stFilterType.equals("Month")){
                dateText = getString(R.string.month);
                prevButton.setVisibility(View.VISIBLE);
                nextButton.setVisibility(View.VISIBLE);
            } else {
                dateText = getString(R.string.custom_period);
            }
            dateText += " - " + stDateFrom + " to " + stDateTo;
        }
        tvDateFilter.setText(dateText);
    }

    public void saveCSVToDatabase(BufferedReader buffer) throws IOException, ParseException {
        String line;
        String fullText = "";
        Date c = new Date(System.currentTimeMillis());
        long todayMilli = c.getTime();

        int id, reconciledFlag, recurringFlag, category, subcategory, taxFlag, accountID;
        long dateTrans, dateUpdated;
        String description, establishment;
        float amount;

        while ((line = buffer.readLine()) != null) {
            String[] RowData = line.split(",");
            if (!RowData[0].equals("_id")) { // skipping the label row here
                //RowData[0] is the id, so we won't import it
                reconciledFlag = Integer.parseInt(RowData[1]);
                dateTrans = Long.parseLong(RowData[2]);

                description = RowData[3];
                establishment = RowData[4];

                accountID = Integer.parseInt(RowData[5]);
                amount = Float.valueOf(RowData[6]);
                category = Integer.parseInt(RowData[7]);
                subcategory = Integer.parseInt(RowData[8]);
                taxFlag = Integer.parseInt(RowData[9]);
                recurringFlag = Integer.parseInt(RowData[11]);

                ContentValues values = new ContentValues();
                values.put(TransEntry.COLUMN_TRANS_RECONCILEDFLAG, reconciledFlag);
                values.put(TransEntry.COLUMN_TRANS_DATE, dateTrans);
                values.put(TransEntry.COLUMN_TRANS_DESC, description);
                values.put(TransEntry.COLUMN_TRANS_EST, establishment);
                values.put(TransEntry.COLUMN_TRANS_ACCOUNT, accountID);
                values.put(TransEntry.COLUMN_TRANS_AMOUNT, amount);
                values.put(TransEntry.COLUMN_TRANS_CAT, category);
                values.put(TransEntry.COLUMN_TRANS_SUBCAT, subcategory);
                values.put(TransEntry.COLUMN_TRANS_RECURRINGID, recurringFlag);
                values.put(TransEntry.COLUMN_TRANS_TAXFLAG, taxFlag);
                values.put(TransEntry.COLUMN_TRANS_DATEUPD, todayMilli);

                Uri newUri = getContentResolver().insert(TransEntry.CONTENT_URI, values);
                if (newUri == null) {
                    Toast.makeText(this, "Error saving transaction", Toast.LENGTH_SHORT).show();
                }
                int transID = (int) ContentUris.parseId(newUri); // then, update the account balance for the transaction added
                FunctionHelper.updateBalance(this, transID, dateTrans, dateTrans, accountID);
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        switch (i) {
            case FILTER_ACC_LOADER:
                String[] projection3 = {
                        FilterAccEntry._ID,
                        FilterAccEntry.COLUMN_ACC_ID};
                String sortBy3 = FilterAccEntry.COLUMN_ACC_ID + " ASC";
                return new CursorLoader(this, FilterAccEntry.CONTENT_URI, projection3, null, null, sortBy3);
            case FILTER_CAT_LOADER:
                String[] projection4 = {
                        FilterCatEntry._ID,
                        FilterCatEntry.COLUMN_CAT_ID};
                String sortBy4 = FilterCatEntry.COLUMN_CAT_ID + " ASC";
                return new CursorLoader(this, FilterCatEntry.CONTENT_URI, projection4, null, null, sortBy4);
            case BUDGET_LOADER:
                String[] projection = {
                        TransEntry._ID,
                        TransEntry.COLUMN_TRANS_RECONCILEDFLAG,
                        TransEntry.COLUMN_TRANS_CAT, // changed from DESC
                        TransEntry.COLUMN_TRANS_EST,
                        TransEntry.COLUMN_TRANS_DATE,
                        TransEntry.COLUMN_TRANS_AMOUNT};
                String sortBy = TransEntry.COLUMN_TRANS_DATE + " ASC";
                String selectionSt = "";

                if (!filterAccSelection.isEmpty()) {
                    selectionSt = filterAccSelection;
                    if (!filterCatSelection.isEmpty()) {
                        selectionSt += " AND " + filterCatSelection;
                    }
                } else {
                    if (!filterCatSelection.isEmpty()) {
                        selectionSt = filterCatSelection;
                    }
                }
                if (!selectionSt.isEmpty() && dateFrom > 0){
                    selectionSt += " AND ";
                }
                if(dateFrom > 0) {
                    selectionSt += "(" + TransEntry.COLUMN_TRANS_DATE + " >= " + dateFrom + " AND " + TransEntry.COLUMN_TRANS_DATE + " <= " + dateTo + ")";
                }
                return new CursorLoader(this, TransEntry.CONTENT_URI, projection, selectionSt, null, sortBy);
            case CAT_LOADER:
                String[] projection2 = {
                        CatEntry._ID,
                        CatEntry.COLUMN_CATNAME};
                String sortBy2 = CatEntry._ID + " ASC";
                return new CursorLoader(this, CatEntry.CONTENT_URI, projection2, null, null, sortBy2);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case FILTER_ACC_LOADER:
                filterAccSelection = "";
                while (cursor.moveToNext()) {
                    accIDs.add(cursor.getInt(cursor.getColumnIndexOrThrow(FilterAccEntry.COLUMN_ACC_ID)));
                    if (filterAccSelection.equals("")) {
                        filterAccSelection = "(" + TransEntry.COLUMN_TRANS_ACCOUNT + "=" + cursor.getInt(cursor.getColumnIndexOrThrow(FilterAccEntry.COLUMN_ACC_ID));
                    } else {
                        filterAccSelection = filterAccSelection + " OR " + TransEntry.COLUMN_TRANS_ACCOUNT + "=" + cursor.getInt(cursor.getColumnIndexOrThrow(FilterAccEntry.COLUMN_ACC_ID));
                    }
                }
                if (!filterAccSelection.equals("")) {
                    filterAccSelection += ")";
                }
                getLoaderManager().initLoader(FILTER_CAT_LOADER, null, this);
                break;
            case FILTER_CAT_LOADER:
                filterCatSelection = "";
                while (cursor.moveToNext()) {
                    if (filterCatSelection.equals("")) {
                        filterCatSelection = "(" + TransEntry.COLUMN_TRANS_CAT + "=" + cursor.getInt(cursor.getColumnIndexOrThrow(FilterCatEntry.COLUMN_CAT_ID));
                    } else {
                        filterCatSelection = filterCatSelection + " OR " + TransEntry.COLUMN_TRANS_CAT + "=" + cursor.getInt(cursor.getColumnIndexOrThrow(FilterCatEntry.COLUMN_CAT_ID));
                    }
                }
                if (!filterCatSelection.equals("")) {
                    filterCatSelection += ")";
                }
                getLoaderManager().initLoader(CAT_LOADER, null, this);
            case CAT_LOADER:
                while (cursor.moveToNext()) {
                    catIDs.add(cursor.getInt(cursor.getColumnIndexOrThrow(CatEntry._ID)));
                    catNames.add(cursor.getString(cursor.getColumnIndexOrThrow(CatEntry.COLUMN_CATNAME)));
                }
                getLoaderManager().initLoader(BUDGET_LOADER, null, this);
                break;
            case BUDGET_LOADER:
                // calculating the category balance, if any categories are selected
                DecimalFormat dcFormat = new DecimalFormat(currSymbol+"#,##0.00;-" + currSymbol + "#,##0.00");
                TextView catBalanceTV = (TextView) findViewById(R.id.catbalance_textview);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) transListView.getLayoutParams();
                if(!filterCatSelection.isEmpty()){
                    float catBalance = 0;
                    cursor.moveToPosition(-1);
                    while(cursor.moveToNext()){
                        catBalance += cursor.getFloat(cursor.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_AMOUNT));
                    }
                    catBalanceTV.setText(getString(R.string.cat_balance) + dcFormat.format(catBalance));
                    catBalanceTV.setVisibility(View.VISIBLE);
                    params.addRule(RelativeLayout.ABOVE, R.id.catbalance_textview);
                } else {   // if no categories are selected, hide the textview and make the listview extend to the balance listview
                    catBalanceTV.setVisibility(View.GONE);
                    params.addRule(RelativeLayout.ABOVE, R.id.balance_textview);
                }
                budAdapter.swapCursor(cursor);
                // call the function to display the account balance
                long dateBalance;
                if(dateTo==0){
                    dateBalance = 32535090000000L;
                } else {
                    dateBalance = dateTo;
                }
                float totalBalance = FunctionHelper.displayBalance(this, accIDs, dateBalance);
                TextView balanceTV = (TextView) findViewById(R.id.balance_textview);
                String strBalance;
                strBalance = getString(R.string.acc_balance) + dcFormat.format(totalBalance);
                if(!stDateTo.isEmpty()){
                    strBalance += getString(R.string.as_of) + stDateTo;
                }
                balanceTV.setText(strBalance);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case FILTER_ACC_LOADER:
                break;
            case BUDGET_LOADER:
                budAdapter.swapCursor(null);
                break;
            case CAT_LOADER:
                break;
            default:
                break;
        }
    }
}