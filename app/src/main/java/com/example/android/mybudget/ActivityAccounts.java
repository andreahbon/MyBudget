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
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import com.example.android.mybudget.data.BudgetContract;
import com.example.android.mybudget.data.BudgetContract.AccEntry;

public class ActivityAccounts extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    final Context context = this;
    private static final int ACC_LOADER = 0;
    private AccCursorAdapter accAdapter;

    private Uri accUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounts);

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab_accounts);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAccPopup(null, null);
            }
        });

        final ListView accListView = (ListView)findViewById(R.id.acc_listview);
        accAdapter = new AccCursorAdapter(this, null);
        accListView.setAdapter(accAdapter);

        getLoaderManager().initLoader(ACC_LOADER, null, this);

        accListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                accUri = new ContentUris().withAppendedId(AccEntry.CONTENT_URI, id);
                String currAccName = accAdapter.getCursor().getString(accAdapter.getCursor().getColumnIndexOrThrow(AccEntry.COLUMN_ACCNAME));
                displayAccPopup(accUri, currAccName);
            }
        });
    }

    private void displayAccPopup(final Uri accUri, String currAccName) {
        LayoutInflater li = LayoutInflater.from(context);
        View promptView = li.inflate(R.layout.prompt_additem, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptView);
        TextView labelTV = (TextView) promptView.findViewById(R.id.prompt_name_label);
        labelTV.setText(getString(R.string.prompt_acc_name));
        final EditText accNameET = (EditText) promptView.findViewById(R.id.item_name);

        if(currAccName != null){
            accNameET.setText(currAccName);
        }
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Save",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if(!TextUtils.isEmpty(accNameET.getText())){
                                    ContentValues values = new ContentValues();
                                    values.put(AccEntry.COLUMN_ACCNAME, accNameET.getText().toString());
                                    if(accUri == null) {
                                        Uri newUri = getContentResolver().insert(AccEntry.CONTENT_URI, values);
                                        if(newUri != null) {
                                            Toast.makeText(context, getString(R.string.toast_acc_saved), Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(context, getString(R.string.toast_acc_save_failed), Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        int updatedRows = getContentResolver().update(accUri, values, null, null);
                                        if (updatedRows > 0) {
                                            Toast.makeText(context, getString(R.string.toast_acc_updated), Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(context, getString(R.string.toast_failed_acc_update), Toast.LENGTH_SHORT).show();
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
                AccEntry._ID,
                AccEntry.COLUMN_ACCNAME};
        String sortBy = AccEntry._ID + " ASC";
        return new CursorLoader(this, AccEntry.CONTENT_URI, projection, null, null, sortBy);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) { accAdapter.swapCursor(cursor);}

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { accAdapter.swapCursor(null);}
}
