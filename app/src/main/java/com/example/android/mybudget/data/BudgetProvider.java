package com.example.android.mybudget.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.mybudget.data.BudgetContract.AccEntry;
import com.example.android.mybudget.data.BudgetContract.TransEntry;
import com.example.android.mybudget.data.BudgetContract.CatEntry;

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
            default:
                throw new   IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case TRANSACTIONS:
                return insertTransaction(uri, values);
            case CATS:
                return insertCategory(uri, values);
            case ACCOUNTS:
                return insertAccount(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    public Uri insertTransaction(Uri uri, ContentValues values){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long mNewID = db.insert(TransEntry.TABLE_NAME, null, values);
        if (mNewID == -1){
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, mNewID);
    }

    public Uri insertCategory(Uri uri, ContentValues values){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long mNewID = db.insert(CatEntry.TABLE_NAME, null, values);
        if (mNewID == -1){
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, mNewID);
    }

    public Uri insertAccount(Uri uri, ContentValues values){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long mNewID = db.insert(AccEntry.TABLE_NAME, null, values);
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
        switch (match){
            case TRANSACTIONS:
                return updateTransaction(uri, values, selection, selectionArgs);
            case TRANS_ID:
                selection = TransEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return updateTransaction(uri, values, selection, selectionArgs);
            case CATS:
                return updateCategory(uri, values, selection, selectionArgs);
            case CAT_ID:
                selection = CatEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return updateCategory(uri, values, selection, selectionArgs);
            case ACCOUNTS:
                return updateAccount(uri, values, selection, selectionArgs);
            case ACC_ID:
                selection = AccEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return updateAccount(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    public int updateTransaction(Uri uri, ContentValues values, String selection, String[] selectionArgs){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsUpdated = db.update(TransEntry.TABLE_NAME, values, selection, selectionArgs);
        if(rowsUpdated > 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    public int updateCategory(Uri uri, ContentValues values, String selection, String[] selectionArgs){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsUpdated = db.update(CatEntry.TABLE_NAME, values, selection, selectionArgs);
        if(rowsUpdated > 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
    public int updateAccount(Uri uri, ContentValues values, String selection, String[] selectionArgs){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsUpdated = db.update(AccEntry.TABLE_NAME, values, selection, selectionArgs);
        if(rowsUpdated > 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
