package com.example.android.mybudget;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CursorAdapter;
import android.widget.ListView;

import com.example.android.mybudget.data.BudgetContract.AccEntry;
import com.example.android.mybudget.data.BudgetContract.CatEntry;

import java.text.ParseException;

public class ActivityFilter extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final int CAT_LOADER = 0;
    private static final int ACC_LOADER = 1;

    private CursorAdapter catAdapter;
    private CursorAdapter accAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        ListView catListView = (ListView) findViewById(R.id.filter_catlist);
        ListView accListView = (ListView) findViewById(R.id.filter_acclist);
        catAdapter = new FilterCatCursorAdapter(this, null);
        accAdapter = new FilterAccCursorAdapter(this, null);

        catListView.setAdapter(catAdapter);
        accListView.setAdapter(accAdapter);

        getLoaderManager().initLoader(CAT_LOADER, null, this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_filter, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_filter:
                setResult(RESULT_OK, null);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        switch(i){
            case CAT_LOADER:
                String [] projection = {
                        CatEntry._ID,
                        CatEntry.COLUMN_CATNAME};
                String sortBy = CatEntry.COLUMN_CATNAME + " ASC";
                return new CursorLoader(this, CatEntry.CONTENT_URI, projection, null, null, sortBy);
            case ACC_LOADER:
                String [] projection2 = {
                        AccEntry._ID,
                        AccEntry.COLUMN_ACCNAME};
                String sortBy2 = AccEntry._ID + " ASC";
                return new CursorLoader(this, AccEntry.CONTENT_URI, projection2, null, null, sortBy2);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()){
            case CAT_LOADER:
                catAdapter.swapCursor(cursor);
                getLoaderManager().initLoader(ACC_LOADER, null, this);
                break;
            case ACC_LOADER:
                accAdapter.swapCursor(cursor);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()){
            case CAT_LOADER:
                catAdapter.swapCursor(null);
                break;
            case ACC_LOADER:
                accAdapter.swapCursor(null);
                break;
        }
    }
}
