package com.example.android.mybudget.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by andre on 6/02/2017.
 */

public class BudgetContract {
    static final String CONTENT_AUTHORITY = "com.example.android.mybudget";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    static final String PATH_TRANS = "transactions";
    static final String PATH_CATEGORIES = "categories";
    static final String PATH_ACCOUNTS = "accounts";

    public static abstract class TransEntry implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_TRANS);
        static final String TABLE_NAME = "transactions";

        public static final String _ID = BaseColumns._ID;
        public static final String FULL_ID = TABLE_NAME + "." + _ID;

        public static final String COLUMN_TRANS_RECONCILEDFLAG = "isReconciled";
        public static final String COLUMN_TRANS_DATE = "date";
        public static final String COLUMN_TRANS_DESC = "description";
        public static final String COLUMN_TRANS_EST = "establishment";
        public static final String COLUMN_TRANS_ACCOUNT = "account";
        public static final String COLUMN_TRANS_AMOUNT = "amount";
        public static final String COLUMN_TRANS_CAT = "category";
        static final String COLUMN_TRANS_SUBCAT = "subcategory";
        public static final String COLUMN_TRANS_TAXFLAG = "forIncomeTax";
        public static final String COLUMN_TRANS_DATEUPD = "dateUpdated";
        public static final String COLUMN_TRANS_RECURRINGFLAG = "isRecurring";

        static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/"
                + CONTENT_AUTHORITY + "/" + PATH_TRANS;
        static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/"
                + CONTENT_AUTHORITY + "/" + PATH_TRANS;
    }

    public static abstract class CatEntry implements BaseColumns{
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_CATEGORIES);
        static final String TABLE_NAME = "categories";

        public static final String _ID = BaseColumns._ID;
        public static final String FULL_ID = TABLE_NAME + "." + _ID;

        public static final String COLUMN_CATNAME = "category";

        static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/"
                + CONTENT_AUTHORITY + "/" + PATH_CATEGORIES;
        static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/"
                + CONTENT_AUTHORITY + "/" + PATH_CATEGORIES;
    }

    public static abstract class AccEntry implements BaseColumns{
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ACCOUNTS);
        static final String TABLE_NAME = "accounts";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_ACCNAME = "accountName";

        static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/"
                + CONTENT_AUTHORITY + "/" + PATH_ACCOUNTS;
        static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/"
                + CONTENT_AUTHORITY + "/" + PATH_ACCOUNTS;
    }

}