package com.example.android.mybudget.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.mybudget.FilterCatCursorAdapter;
import com.example.android.mybudget.data.BudgetContract.AccEntry;
import com.example.android.mybudget.data.BudgetContract.TransEntry;
import com.example.android.mybudget.data.BudgetContract.CatEntry;
import com.example.android.mybudget.data.BudgetContract.FilterAccEntry;
import com.example.android.mybudget.data.BudgetContract.FilterCatEntry;
import com.example.android.mybudget.data.BudgetContract.RecurrEntry;

import static android.R.attr.value;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;

/**
 * Created by andre on 6/02/2017.
 */

public class BudgetProvider extends ContentProvider {
    public static final String LOG_TAG = BudgetProvider.class.getSimpleName();
    private static final int TRANSACTIONS = 100;
    private static final int TRANS_ID = 101;
    private static final int CATS = 200;
    private static final int CAT_ID = 201;
    private static final int ACCOUNTS = 300;
    private static final int ACC_ID = 301;
    private static final int FILTER_CATS = 400;
    private static final int FILTER_CAT_ID = 401;
    private static final int FILTER_ACCOUNTS = 500;
    private static final int FILTER_ACCOUNT_ID = 501;
    private static final int RECURRING = 600;
    private static final int RECURRING_ID = 601;


    private BudgetDBHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new BudgetDBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match){
            case TRANSACTIONS:
                cursor = database.query(TransEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case TRANS_ID:
                selection = TransEntry._ID +"=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(TransEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case CATS:
                cursor = database.query(CatEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case CAT_ID:
                selection = CatEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(CatEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case ACCOUNTS:
                cursor = database.query(AccEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case ACC_ID:
                selection = AccEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(AccEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case FILTER_ACCOUNTS:
                cursor = database.query(FilterAccEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case FILTER_ACCOUNT_ID:
                selection = FilterAccEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(FilterAccEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case FILTER_CATS:
                cursor = database.query(FilterCatEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case FILTER_CAT_ID:
                selection = FilterCatEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(FilterCatEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case RECURRING:
                cursor = database.query(RecurrEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case RECURRING_ID:
                selection = RecurrEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(RecurrEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(BudgetContract.CONTENT_AUTHORITY, BudgetContract.PATH_TRANS, TRANSACTIONS);
        sUriMatcher.addURI(BudgetContract.CONTENT_AUTHORITY, BudgetContract.PATH_TRANS + "/#", TRANS_ID);
        sUriMatcher.addURI(BudgetContract.CONTENT_AUTHORITY, BudgetContract.PATH_CATEGORIES, CATS);
        sUriMatcher.addURI(BudgetContract.CONTENT_AUTHORITY, BudgetContract.PATH_CATEGORIES + "/#", CAT_ID);
        sUriMatcher.addURI(BudgetContract.CONTENT_AUTHORITY, BudgetContract.PATH_ACCOUNTS, ACCOUNTS);
        sUriMatcher.addURI(BudgetContract.CONTENT_AUTHORITY, BudgetContract.PATH_ACCOUNTS + "/#", ACC_ID);
        sUriMatcher.addURI(BudgetContract.CONTENT_AUTHORITY, BudgetContract.PATH_FILTER_ACCOUNTS, FILTER_ACCOUNTS);
        sUriMatcher.addURI(BudgetContract.CONTENT_AUTHORITY, BudgetContract.PATH_FILTER_ACCOUNTS + "/#", FILTER_ACCOUNT_ID);
        sUriMatcher.addURI(BudgetContract.CONTENT_AUTHORITY, BudgetContract.PATH_FILTER_CATS, FILTER_CATS);
        sUriMatcher.addURI(BudgetContract.CONTENT_AUTHORITY, BudgetContract.PATH_FILTER_CATS + "/#", FILTER_CAT_ID);
        sUriMatcher.addURI(BudgetContract.CONTENT_AUTHORITY, BudgetContract.PATH_RECURRING, RECURRING);
        sUriMatcher.addURI(BudgetContract.CONTENT_AUTHORITY, BudgetContract.PATH_RECURRING + "/#", RECURRING_ID);
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case TRANSACTIONS:
                return TransEntry.CONTENT_LIST_TYPE;
            case TRANS_ID:
                return TransEntry.CONTENT_ITEM_TYPE;
            case CATS:
                return CatEntry.CONTENT_LIST_TYPE;
            case CAT_ID:
                return CatEntry.CONTENT_ITEM_TYPE;
            case ACCOUNTS:
                return AccEntry.CONTENT_LIST_TYPE;
            case ACC_ID:
                return AccEntry.CONTENT_ITEM_TYPE;
            case FILTER_ACCOUNTS:
                return FilterAccEntry.CONTENT_LIST_TYPE;
            case FILTER_ACCOUNT_ID:
                return FilterAccEntry.CONTENT_ITEM_TYPE;
            case FILTER_CATS:
                return FilterCatEntry.CONTENT_LIST_TYPE;
            case FILTER_CAT_ID:
                return FilterCatEntry.CONTENT_ITEM_TYPE;
            case RECURRING:
                return RecurrEntry.CONTENT_LIST_TYPE;
            case RECURRING_ID:
                return RecurrEntry.CONTENT_ITEM_TYPE;
            default:
                throw new   IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String strTable;
        switch (match){
            case TRANSACTIONS:
                strTable = TransEntry.TABLE_NAME;
                break;
            case CATS:
                strTable = CatEntry.TABLE_NAME;
                break;
            case ACCOUNTS:
                strTable = AccEntry.TABLE_NAME;
                break;
            case FILTER_ACCOUNTS:
                strTable = FilterAccEntry.TABLE_NAME;
                break;
            case FILTER_CATS:
                strTable = FilterCatEntry.TABLE_NAME;
                break;
            case RECURRING:
                strTable = RecurrEntry.TABLE_NAME;
                break;
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
        long mNewID = db.insert(strTable, null, values);
        if (mNewID == -1){
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, mNewID);
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match){
            case TRANSACTIONS:
                rowsDeleted = db.delete(TransEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TRANS_ID:
                selection = TransEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(TransEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CATS:
                rowsDeleted = db.delete(CatEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CAT_ID:
                selection = CatEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(CatEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ACCOUNTS:
                rowsDeleted = db.delete(AccEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ACC_ID:
                selection = AccEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(AccEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case FILTER_ACCOUNTS:
                rowsDeleted = db.delete(FilterAccEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case FILTER_ACCOUNT_ID:
                selection = FilterAccEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(FilterAccEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case FILTER_CATS:
                rowsDeleted = db.delete(FilterCatEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case FILTER_CAT_ID:
                selection = FilterCatEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(FilterCatEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case RECURRING:
                rowsDeleted = db.delete(RecurrEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case RECURRING_ID:
                selection = RecurrEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(RecurrEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if(rowsDeleted > 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if(values.size()==0){
            return 0;
        }
        final int match = sUriMatcher.match(uri);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String strTable;

        switch (match){
            case TRANSACTIONS:
                strTable = TransEntry.TABLE_NAME;
                break;
            case TRANS_ID:
                strTable = TransEntry.TABLE_NAME;
                selection = TransEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                break;
            case CATS:
                strTable = CatEntry.TABLE_NAME;
                break;
            case CAT_ID:
                strTable = CatEntry.TABLE_NAME;
                selection = CatEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                break;
            case ACCOUNTS:
                strTable = AccEntry.TABLE_NAME;
                break;
            case ACC_ID:
                strTable = AccEntry.TABLE_NAME;
                selection = AccEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                break;
            case RECURRING:
                strTable = RecurrEntry.TABLE_NAME;
                break;
            case RECURRING_ID:
                strTable = RecurrEntry.TABLE_NAME;
                selection = RecurrEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                break;
            case FILTER_ACCOUNT_ID:
            case FILTER_ACCOUNTS:
            case FILTER_CAT_ID:
            case FILTER_CATS:
                return -1;
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
        int rowsUpdated = db.update(strTable, values, selection, selectionArgs);
        if(rowsUpdated > 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
