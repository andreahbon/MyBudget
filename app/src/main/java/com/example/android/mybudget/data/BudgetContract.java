package com.example.android.mybudget.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by andre on 6/02/2017.
 */

public class BudgetContract {
    public static final String CONTENT_AUTHORITY = "com.example.android.mybudget";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_TRANS = "transactions";
    public static final String PATH_CATEGORIES = "categories";
    public static final String PATH_TRANS_JOIN_CAT = "trans_join_cat";

    public static final Uri JOIN_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_TRANS_JOIN_CAT);

    public static abstract class TransEntry implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_TRANS);
        public static final String TABLE_NAME = "transactions";

        public static final String _ID = BaseColumns._ID;
        public static final String FULL_ID = TABLE_NAME + "." + _ID;

        public static final String COLUMN_TRANS_RECONCILEDFLAG = "isReconciled";
        public static final String COLUMN_TRANS_DATE = "date";
        public static final String COLUMN_TRANS_DESC = "description";
        public static final String COLUMN_TRANS_EST = "establishment";
        public static final String COLUMN_TRANS_ACCOUNT = "account";
        public static final String COLUMN_TRANS_AMOUNT = "amount";
        public static final String COLUMN_TRANS_CAT = "category";
        public static final String COLUMN_TRANS_SUBCAT = "subcategory";
        public static final String COLUMN_TRANS_TAXFLAG = "forIncomeTax";
        public static final String COLUMN_TRANS_DATEUPD = "dateUpdated";
        public static final String COLUMN_TRANS_RECURRINGFLAG = "isRecurring";

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/"
                + CONTENT_AUTHORITY + "/" + PATH_TRANS;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/"
                + CONTENT_AUTHORITY + "/" + PATH_TRANS;
    }

    public static abstract class CatEntry implements BaseColumns{
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_CATEGORIES);
        public static final String TABLE_NAME = "categories";

        public static final String _ID = BaseColumns._ID;
        public static final String FULL_ID = TABLE_NAME + "." + _ID;

        public static final String COLUMN_CATNAME = "category";

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/"
                + CONTENT_AUTHORITY + "/" + PATH_CATEGORIES;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/"
                + CONTENT_AUTHORITY + "/" + PATH_CATEGORIES;
    }

}
