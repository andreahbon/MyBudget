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
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.mybudget.data.BudgetContract.TransEntry;
import com.example.android.mybudget.data.BudgetContract.CatEntry;
import com.example.android.mybudget.data.BudgetContract.AccEntry;
import com.example.android.mybudget.data.BudgetContract.FilterAccEntry;
import com.example.android.mybudget.data.BudgetContract.FilterCatEntry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int REQUEST_WRITE_STORAGE = 112;
    public static final String PREFS_NAME = "DatePrefs";

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

    private String filterAccSelection = "";
    private String filterCatSelection = "";

    private String stDateFrom = "";
    private String stDateTo = "";
    private String stFilterType = "";
    private long dateFrom = 0;
    private long dateTo = 0;

    TextView tvDateFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvDateFilter = (TextView) findViewById(R.id.date_filter);
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

        ListView transListView = (ListView) findViewById(R.id.trans_listview);
        budAdapter = new TransCursorAdapter(this, null, catIDs, catNames);
        transListView.setAdapter(budAdapter);

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
                exportToCSV();
                return true;
            case R.id.action_mngcat:
                Intent intent = new Intent(this, ActivityCategory.class);
                startActivity(intent);
                return true;
            case R.id.action_mngacc:
                Intent intent2 = new Intent(this, ActivityAccounts.class);
                startActivity(intent2);
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
        //Toast.makeText(this, "stFilterType = " + stFilterType, Toast.LENGTH_SHORT).show();
        String dateText;
        if (stFilterType.equals("All transactions")){
            dateText = "All transactions";
        } else {
            if (stFilterType.equals("Month")){
                stFilterType = "This month";
            }
            dateText = stFilterType + " - " + stDateFrom + " to " + stDateTo;
        }
        tvDateFilter.setText(dateText);
    }

    public void saveCSVToDatabase(BufferedReader buffer) throws IOException, ParseException {
        // TODO: The dates on imported transactions display incorrectly (e.g. 14/01/1970), even though the long numbers seem correct - fix this at some point
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
                budAdapter.swapCursor(cursor);
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


    public void exportToCSV() {
        String[] projection10 = {
                TransEntry._ID,
                TransEntry.COLUMN_TRANS_RECONCILEDFLAG,
                TransEntry.COLUMN_TRANS_DATE,
                TransEntry.COLUMN_TRANS_DESC,
                TransEntry.COLUMN_TRANS_EST,
                TransEntry.COLUMN_TRANS_ACCOUNT,
                TransEntry.COLUMN_TRANS_AMOUNT,
                TransEntry.COLUMN_TRANS_CAT,
                TransEntry.COLUMN_TRANS_SUBCAT,
                TransEntry.COLUMN_TRANS_TAXFLAG,
                TransEntry.COLUMN_TRANS_DATEUPD,
                TransEntry.COLUMN_TRANS_RECURRINGID};
        String sortBy10 = TransEntry.COLUMN_TRANS_DATE + " ASC";
        Cursor transCursor = getContentResolver().query(TransEntry.CONTENT_URI, projection10, null, null, sortBy10);

        String columnString1 = TransEntry._ID + "," +
                TransEntry.COLUMN_TRANS_RECONCILEDFLAG + "," +
                TransEntry.COLUMN_TRANS_DATE + "," +
                TransEntry.COLUMN_TRANS_DESC + "," +
                TransEntry.COLUMN_TRANS_EST + "," +
                TransEntry.COLUMN_TRANS_ACCOUNT + "," +
                TransEntry.COLUMN_TRANS_AMOUNT + "," +
                TransEntry.COLUMN_TRANS_CAT + "," +
                TransEntry.COLUMN_TRANS_SUBCAT + "," +
                TransEntry.COLUMN_TRANS_TAXFLAG + "," +
                TransEntry.COLUMN_TRANS_DATEUPD + "," +
                TransEntry.COLUMN_TRANS_RECURRINGID;

        String dataString1 = "";
        while(transCursor.moveToNext()){
            dataString1 += transCursor.getInt(transCursor.getColumnIndexOrThrow(TransEntry._ID)) + ",";
            dataString1 += transCursor.getInt(transCursor.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_RECONCILEDFLAG)) + ",";
            dataString1 += transCursor.getInt(transCursor.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_DATE)) + ",";
            dataString1 += transCursor.getString(transCursor.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_DESC)) + ",";
            dataString1 += transCursor.getString(transCursor.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_EST)) + ",";
            dataString1 += transCursor.getInt(transCursor.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_ACCOUNT)) + ",";
            dataString1 += transCursor.getFloat(transCursor.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_AMOUNT)) + ",";
            dataString1 += transCursor.getInt(transCursor.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_CAT)) + ",";
            dataString1 += transCursor.getInt(transCursor.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_SUBCAT)) + ",";
            dataString1 += transCursor.getInt(transCursor.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_TAXFLAG)) + ",";
            dataString1 += transCursor.getInt(transCursor.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_DATEUPD)) + ",";
            dataString1 += transCursor.getInt(transCursor.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_RECURRINGID)) + "\n";
    }
        transCursor.close();
        String combinedString1 = columnString1 + "\n" + dataString1;

        String[] projection11 = {
                CatEntry._ID,
                CatEntry.COLUMN_CATNAME};
        String sortBy11 = CatEntry._ID + " ASC";
        Cursor catCursor = getContentResolver().query(CatEntry.CONTENT_URI, projection11, null, null, sortBy11);

        String columnString2 = CatEntry._ID + "," +
                CatEntry.COLUMN_CATNAME;

        String dataString2 = "";
        while(catCursor.moveToNext()){
            dataString2 += catCursor.getInt(catCursor.getColumnIndexOrThrow(CatEntry._ID)) + ",";
            dataString2 += catCursor.getString(catCursor.getColumnIndexOrThrow(CatEntry.COLUMN_CATNAME)) + "\n";
        }
        catCursor.close();
        String combinedString2 = columnString2 + "\n" + dataString2;

        String[] projection12 = {
                AccEntry._ID,
                AccEntry.COLUMN_ACCNAME};
        String sortBy12 = AccEntry._ID + " ASC";
        Cursor accCursor = getContentResolver().query(AccEntry.CONTENT_URI, projection12, null, null, sortBy12);

        String columnString3 = AccEntry._ID + "," + AccEntry.COLUMN_ACCNAME;

        String dataString3 = "";
        while(accCursor.moveToNext()){
            dataString3 += accCursor.getInt(accCursor.getColumnIndexOrThrow(AccEntry._ID)) + ",";
            dataString3 += accCursor.getString(accCursor.getColumnIndexOrThrow(AccEntry.COLUMN_ACCNAME)) + "\n";
        }
        accCursor.close();
        String combinedString3 = columnString3 + "\n" + dataString3;

        File file1 = null;
        File file2 = null;
        File file3 = null;
        File root = Environment.getExternalStorageDirectory();
        if (root.canWrite()){
            File dir = new File (root.getAbsolutePath() + "/BudgetData");
            dir.mkdirs();
            file1 = new File(dir, "Transactions.csv");
            file2 = new File(dir, "Categories.csv");
            file3 = new File(dir, "Accounts.csv");
            FileOutputStream out1 = null;
            FileOutputStream out2 = null;
            FileOutputStream out3 = null;
            try {
                out1 = new FileOutputStream(file1);
                out2 = new FileOutputStream(file2);
                out3 = new FileOutputStream(file3);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                out1.write(combinedString1.getBytes());
                out2.write(combinedString2.getBytes());
                out3.write(combinedString3.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                out1.close();
                out2.close();
                out3.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    Uri u1 = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".provider", file1);
    Uri u2 = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".provider", file2);
    Uri u3 = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".provider", file3);
    ArrayList<Uri> uris = new ArrayList<>();
    uris.add(u1);
    uris.add(u2);
    uris.add(u3);

    Intent sendIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
    sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Budget data");
    sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
    sendIntent.setType("text/html");
    startActivity(sendIntent);
    }
}