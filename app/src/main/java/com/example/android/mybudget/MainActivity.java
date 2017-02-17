package com.example.android.mybudget;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public int REQUEST_FILE_GET = 1;
    public int CHANGE_FILTER = 2;
    private static final int BUDGET_LOADER = 0;
    private static final int CAT_LOADER = 1;
    private static final int FILTER_ACC_LOADER = 2;
    private static final int FILTER_CAT_LOADER = 3;
    private TransCursorAdapter budAdapter;

    private ArrayList<Integer> catIDs = new ArrayList<>();
    private ArrayList<String> catNames = new ArrayList<>();

    private String filterAccSelection = "";
    private String filterCatSelection = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    }

    public void saveCSVToDatabase(BufferedReader buffer) throws IOException, ParseException {
        String line;
        String fullText = "";
        Date c = new Date(System.currentTimeMillis());
        long todayMilli = c.getTime();

        int reconciledFlag, recurringFlag, category, subcategory, taxFlag, accountID;
        long dateTrans, dateUpdated;
        String description, establishment;
        float amount;

        while ((line = buffer.readLine()) != null) {
            String[] RowData = line.split(",");
            if (TextUtils.isEmpty(RowData[0])) {
                reconciledFlag = 0;
            } else {
                reconciledFlag = 1;
            }

            String dateTransSt = RowData[1];
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date dateTransDT = sdf.parse(dateTransSt);
            dateTrans = dateTransDT.getTime();

            description = RowData[2];
            establishment = RowData[3];

            if (RowData[4].equals("NAB cheque")) {
                accountID = 0;
            } else {
                accountID = 1;
            }
            amount = Float.valueOf(RowData[5]);
            recurringFlag = taxFlag = 0;

            ContentValues values = new ContentValues();
            values.put(TransEntry.COLUMN_TRANS_RECONCILEDFLAG, reconciledFlag);
            values.put(TransEntry.COLUMN_TRANS_DATE, dateTrans);
            values.put(TransEntry.COLUMN_TRANS_DESC, description);
            values.put(TransEntry.COLUMN_TRANS_EST, establishment);
            values.put(TransEntry.COLUMN_TRANS_ACCOUNT, accountID);
            values.put(TransEntry.COLUMN_TRANS_AMOUNT, amount);
            values.put(TransEntry.COLUMN_TRANS_RECURRINGFLAG, recurringFlag);
            values.put(TransEntry.COLUMN_TRANS_TAXFLAG, taxFlag);
            values.put(TransEntry.COLUMN_TRANS_DATEUPD, todayMilli);

            Uri newUri = getContentResolver().insert(TransEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, "Error saving transaction", Toast.LENGTH_SHORT).show();
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

                if (!filterAccSelection.isEmpty()){
                    selectionSt = filterAccSelection;
                    if(!filterCatSelection.isEmpty()) {
                        selectionSt += " AND " + filterCatSelection;
                    }
                } else {
                    if(!filterCatSelection.isEmpty()){
                        selectionSt = filterCatSelection;
                    }
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
                    if(filterCatSelection.equals("")){
                        filterCatSelection = "(" + TransEntry.COLUMN_TRANS_CAT + "=" + cursor.getInt(cursor.getColumnIndexOrThrow(FilterCatEntry.COLUMN_CAT_ID));
                    } else {
                        filterCatSelection = filterCatSelection + " OR " + TransEntry.COLUMN_TRANS_CAT + "=" + cursor.getInt(cursor.getColumnIndexOrThrow(FilterCatEntry.COLUMN_CAT_ID));
                    }
                }
                if(!filterCatSelection.equals("")){
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
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            Toast.makeText(this, "External StorageState = true", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "External StorageState = false", Toast.LENGTH_SHORT).show();
        }
    }

}
