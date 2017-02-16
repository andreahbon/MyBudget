package com.example.android.mybudget;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.mybudget.data.BudgetContract;
import com.example.android.mybudget.data.BudgetContract.CatEntry;

public class ActivityCategory extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    final Context context = this;
    private static final int BUDGET_LOADER = 0;
    private  CatCursorAdapter catAdapter;

    private Uri catUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                displayCatPopup(null, null);
            }
        });

        final ListView catListView = (ListView)findViewById(R.id.cat_listview);
        catAdapter = new CatCursorAdapter(this, null);
        catListView.setAdapter(catAdapter);

        getLoaderManager().initLoader(BUDGET_LOADER, null, this);

        catListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                catUri = new ContentUris().withAppendedId(CatEntry.CONTENT_URI, id);
                String currCatName = catAdapter.getCursor().getString(catAdapter.getCursor().getColumnIndexOrThrow(CatEntry.COLUMN_CATNAME));
                displayCatPopup(catUri, currCatName);
            }
        });
    }

    private void displayCatPopup(final Uri catUri, String currCatName){
        LayoutInflater li = LayoutInflater.from(context);
        View promptView = li.inflate(R.layout.prompt_addcat, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptView);
        final EditText catNameET = (EditText) promptView.findViewById(R.id.cat_name);

        if(currCatName != null){
            catNameET.setText(currCatName);
        }

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Save",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if(!TextUtils.isEmpty(catNameET.getText())){
                                    ContentValues values = new ContentValues();
                                    values.put(CatEntry.COLUMN_CATNAME, catNameET.getText().toString());
                                    if(catUri == null) {
                                        Uri newUri = getContentResolver().insert(CatEntry.CONTENT_URI, values);
                                        if(newUri != null) {
                                            Toast.makeText(context, getString(R.string.toast_cat_saved), Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(context, getString(R.string.toast_cat_save_failed), Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        int updatedRows = getContentResolver().update(catUri, values, null, null);
                                        if (updatedRows > 0) {
                                            Toast.makeText(context, getString(R.string.toast_cat_updated), Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(context, getString(R.string.toast_cat_update_failed), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String [] projection = {
                CatEntry._ID,
                CatEntry.COLUMN_CATNAME};
        String sortBy = CatEntry.COLUMN_CATNAME + " ASC";
        return new CursorLoader(this, CatEntry.CONTENT_URI, projection, null, null, sortBy);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) { catAdapter.swapCursor(cursor);    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { catAdapter.swapCursor(null);    }
}
