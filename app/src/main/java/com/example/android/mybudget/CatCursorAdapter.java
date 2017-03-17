package com.example.android.mybudget;

import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.mybudget.data.BudgetContract.CatEntry;
import com.example.android.mybudget.data.BudgetContract.TransEntry;

/**
 * Created by andre on 10/02/2017.
 */

public class CatCursorAdapter extends CursorAdapter {

    public CatCursorAdapter(Context context, Cursor c){super(context, c, 0);}

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.cat_list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        final Button deleteButton = (Button) view.findViewById(R.id.button_delete_cat);

        TextView catNameTV = (TextView) view.findViewById(R.id.cat_name_list);
        final int catID = cursor.getInt(cursor.getColumnIndexOrThrow(CatEntry._ID));
        String catName = cursor.getString(cursor.getColumnIndexOrThrow(CatEntry.COLUMN_CATNAME));
        catNameTV.setText(catName);

        deleteButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] deleteProj = {TransEntry._ID, TransEntry.COLUMN_TRANS_CAT}; // check if there are any transactions with this category number
                String selection = TransEntry.COLUMN_TRANS_CAT + "=?";
                String[] selArgs = {String.valueOf(catID)};
                Cursor deleteCursor = context.getContentResolver().query(TransEntry.CONTENT_URI, deleteProj, selection, selArgs, null);
                if(deleteCursor.getCount() > 0){ // if there are transactions with this category number, display message and exit function
                    Toast.makeText(context, context.getString(R.string.cat_with_trans), Toast.LENGTH_LONG).show();
                    return;
                }
                showDeleteConfirmationDialog(context, catID);
            }
        });

    }
    private void showDeleteConfirmationDialog(final Context context, final int catID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.delete_cat_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Uri mCurrentCatUri = ContentUris.withAppendedId(CatEntry.CONTENT_URI, catID);
                deleteCat(context, mCurrentCatUri);
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

    private void deleteCat(Context context, Uri mCurrentCatUri) {
        if (mCurrentCatUri != null) {
            int deletedRows = context.getContentResolver().delete(mCurrentCatUri, null, null);
            if (deletedRows > 0) {
                Toast.makeText(context, context.getString(R.string.toast_cat_deleted), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, context.getString(R.string.toast_cat_delete_failed), Toast.LENGTH_SHORT).show();
            }
        }
    }

}
