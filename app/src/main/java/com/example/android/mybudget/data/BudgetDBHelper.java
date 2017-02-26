package com.example.android.mybudget.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android.mybudget.data.BudgetContract.TransEntry;
import com.example.android.mybudget.data.BudgetContract.CatEntry;
import com.example.android.mybudget.data.BudgetContract.AccEntry;
import com.example.android.mybudget.data.BudgetContract.FilterAccEntry;
import com.example.android.mybudget.data.BudgetContract.FilterCatEntry;
import com.example.android.mybudget.data.BudgetContract.RecurrEntry;



/**
 * Created by andre on 6/02/2017.
 */

class BudgetDBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 5;
    private static final String DATABASE_NAME = "mybudget.db";

    BudgetDBHelper(Context context){
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
            TransEntry.COLUMN_TRANS_RECURRINGID + " INTEGER)";
        Log.i("BudgetDBHelper", SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES);

        String SQL_CREATE_ENTRIES_CAT =
            "CREATE TABLE " + CatEntry.TABLE_NAME + " (" +
            CatEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            CatEntry.COLUMN_CATNAME + " TEXT NOT NULL)";
        Log.i("BudgetDBHelper",SQL_CREATE_ENTRIES_CAT);
        db.execSQL(SQL_CREATE_ENTRIES_CAT);

        String SQL_CREATE_ENTRIES_ACC =
                "CREATE TABLE " + AccEntry.TABLE_NAME + " (" +
                        AccEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        AccEntry.COLUMN_ACCNAME + " TEXT NOT NULL)";
        Log.i("BudgetDBHelper", SQL_CREATE_ENTRIES_ACC);
        db.execSQL(SQL_CREATE_ENTRIES_ACC);

        String SQL_CREATE_ENTRIES_FILTER_ACC =
                "CREATE TABLE " + FilterAccEntry.TABLE_NAME + " (" +
                        FilterAccEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        FilterAccEntry.COLUMN_ACC_ID + " INTEGER NOT NULL)";
        Log.i("BudgetDBHelper", SQL_CREATE_ENTRIES_FILTER_ACC);
        db.execSQL(SQL_CREATE_ENTRIES_FILTER_ACC);
        String SQL_CREATE_ENTRIES_FILTER_CAT =
                "CREATE TABLE " + FilterCatEntry.TABLE_NAME + " (" +
                        FilterCatEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        FilterCatEntry.COLUMN_CAT_ID+ " INTEGER NOT NULL)";
        Log.i("BudgetDBHelper", SQL_CREATE_ENTRIES_FILTER_CAT);
        db.execSQL(SQL_CREATE_ENTRIES_FILTER_CAT);
        String SQL_CREATE_ENTRIES_RECURRING =
                "CREATE TABLE " + RecurrEntry.TABLE_NAME + " (" +
                        RecurrEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        RecurrEntry.COLUMN_INIT_TRANS_ID + " INTEGER NOT NULL, " +
                        RecurrEntry.COLUMN_PERIOD + " INTEGER NOT NULL, " +
                        RecurrEntry.COLUMN_START_DATE + " INTEGER NOT NULL, " +
                        RecurrEntry.COLUMN_EXP_DATE + " INTEGER)";
        Log.i("BudgetDBHelper", SQL_CREATE_ENTRIES_RECURRING);
        db.execSQL(SQL_CREATE_ENTRIES_RECURRING);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2){
            String SQL_CREATE_ENTRIES_ACC =
                    "CREATE TABLE " + AccEntry.TABLE_NAME + " (" +
                    AccEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    AccEntry.COLUMN_ACCNAME + " TEXT NOT NULL)";
            Log.i("BudgetDBHelper", SQL_CREATE_ENTRIES_ACC);
            db.execSQL(SQL_CREATE_ENTRIES_ACC);
        }
        if(oldVersion < 3){
            String SQL_CREATE_ENTRIES_FILTER_ACC =
                    "CREATE TABLE " + FilterAccEntry.TABLE_NAME + " (" +
                            FilterAccEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            FilterAccEntry.COLUMN_ACC_ID + " INTEGER NOT NULL)";
            Log.i("BudgetDBHelper", SQL_CREATE_ENTRIES_FILTER_ACC);
            db.execSQL(SQL_CREATE_ENTRIES_FILTER_ACC);
            String SQL_CREATE_ENTRIES_FILTER_CAT =
                    "CREATE TABLE " + FilterCatEntry.TABLE_NAME + " (" +
                            FilterCatEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            FilterCatEntry.COLUMN_CAT_ID+ " INTEGER NOT NULL)";
            Log.i("BudgetDBHelper", SQL_CREATE_ENTRIES_FILTER_CAT);
            db.execSQL(SQL_CREATE_ENTRIES_FILTER_CAT);
        }
        if(oldVersion < 4){
            String SQL_CREATE_ENTRIES_RECURRING =
                    "CREATE TABLE " + RecurrEntry.TABLE_NAME + " (" +
                            RecurrEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            RecurrEntry.COLUMN_INIT_TRANS_ID + " INTEGER NOT NULL, " +
                            RecurrEntry.COLUMN_START_DATE + " INTEGER NOT NULL, " +
                            RecurrEntry.COLUMN_EXP_DATE + " INTEGER)";
            Log.i("BudgetDBHelper", SQL_CREATE_ENTRIES_RECURRING);
            db.execSQL(SQL_CREATE_ENTRIES_RECURRING);
        }
        if(oldVersion < 5){
            String DROP_TABLE_RECURR = "DROP TABLE IF EXISTS " + RecurrEntry.TABLE_NAME ;
            db.execSQL(DROP_TABLE_RECURR);
            String SQL_CREATE_ENTRIES_RECURRING =
                    "CREATE TABLE " + RecurrEntry.TABLE_NAME + " (" +
                            RecurrEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            RecurrEntry.COLUMN_INIT_TRANS_ID + " INTEGER NOT NULL, " +
                            RecurrEntry.COLUMN_PERIOD + " INTEGER NOT NULL, " +
                            RecurrEntry.COLUMN_START_DATE + " INTEGER NOT NULL, " +
                            RecurrEntry.COLUMN_EXP_DATE + " INTEGER)";
            Log.i("BudgetDBHelper", SQL_CREATE_ENTRIES_RECURRING);
            db.execSQL(SQL_CREATE_ENTRIES_RECURRING);
        }

    }
}
