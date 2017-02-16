package com.example.android.mybudget.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android.mybudget.data.BudgetContract.TransEntry;
import com.example.android.mybudget.data.BudgetContract.CatEntry;


/**
 * Created by andre on 6/02/2017.
 */

public class BudgetDBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "mybudget.db";

    public BudgetDBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TransEntry.TABLE_NAME + " (" +
            TransEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            TransEntry.COLUMN_TRANS_RECONCILEDFLAG + " INTEGER, " +
            TransEntry.COLUMN_TRANS_DATE + " INTEGER NOT NULL, " +
            TransEntry.COLUMN_TRANS_DESC + " TEXT NOT NULL, " +
            TransEntry.COLUMN_TRANS_EST + " TEXT NOT NULL, " +
            TransEntry.COLUMN_TRANS_ACCOUNT + " INTEGER NOT NULL, " +
            TransEntry.COLUMN_TRANS_AMOUNT + " REAL NOT NULL, " +
            TransEntry.COLUMN_TRANS_CAT + " INTEGER, " +
            TransEntry.COLUMN_TRANS_SUBCAT + " INTEGER, " +
            TransEntry.COLUMN_TRANS_TAXFLAG + " INTEGER, " +
            TransEntry.COLUMN_TRANS_DATEUPD + " INTEGER NOT NULL, " +
            TransEntry.COLUMN_TRANS_RECURRINGFLAG + " INTEGER)";
        Log.i("BudgetDBHelper", SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES);

        String SQL_CREATE_ENTRIES_CAT =
            "CREATE TABLE " + CatEntry.TABLE_NAME + " (" +
            CatEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            CatEntry.COLUMN_CATNAME + " TEXT NOT NULL)";
        Log.i("BudgetDBHelper",SQL_CREATE_ENTRIES_CAT);
        db.execSQL(SQL_CREATE_ENTRIES_CAT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        Log.i("this", "Entered onUpgrade method");

    }
}
