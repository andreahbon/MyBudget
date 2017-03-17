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

import com.example.android.mybudget.data.BudgetContract;
import com.example.android.mybudget.data.BudgetContract.AccEntry;
import com.example.android.mybudget.data.BudgetContract.TransEntry;

/**
 * Created by andre on 17/02/2017.
 */

public class AccCursorAdapter extends CursorAdapter {

    public AccCursorAdapter(Context context, Cursor c){super(context, c, 0);}

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.acc_list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        final Button deleteButton = (Button) view.findViewById(R.id.button_delete_acc);
        TextView accNameTV = (TextView) view.findViewById(R.id.acc_name_list);
        final int accID = cursor.getInt(cursor.getColumnIndexOrThrow(AccEntry._ID));
        String accName = cursor.getString(cursor.getColumnIndexOrThrow(AccEntry.COLUMN_ACCNAME));
        accNameTV.setText(accName);

        deleteButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] deleteProj = {TransEntry._ID, TransEntry.COLUMN_TRANS_ACCOUNT}; // check if there are any transactions with this account number
                String selection = TransEntry.COLUMN_TRANS_ACCOUNT + "=?";
                String[] selArgs = {String.valueOf(accID)};
                Cursor deleteCursor = context.getContentResolver().query(TransEntry.CONTENT_URI, deleteProj, selection, selArgs, null);
                if(deleteCursor.getCount() > 0){ // if there are transactions with this account number, display message and exit function
                    Toast.makeText(context, context.getString(R.string.acc_with_trans), Toast.LENGTH_LONG).show();
                    return;
                }
                showDeleteConfirmationDialog(context, accID);
            }
        });

    }

    private void showDeleteConfirmationDialog(final Context context, final int accID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.delete_acc_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Uri mCurrentAccUri = ContentUris.withAppendedId(BudgetContract.AccEntry.CONTENT_URI, accID);
                deleteAcc(context, mCurrentAccUri);
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

    private void deleteAcc(Context context, Uri mCurrentAccUri) {
        if (mCurrentAccUri != null) {
            int deletedRows = context.getContentResolver().delete(mCurrentAccUri, null, null);
            if (deletedRows > 0) {
                Toast.makeText(context, context.getString(R.string.toast_acc_deleted), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, context.getString(R.string.toast_acc_delete_failed), Toast.LENGTH_SHORT).show();
            }
        }
    }

}
