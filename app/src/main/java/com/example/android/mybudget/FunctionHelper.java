package com.example.android.mybudget;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.Toast;

import com.example.android.mybudget.data.BudgetContract;
import com.example.android.mybudget.data.BudgetContract.TransEntry;
import com.example.android.mybudget.data.BudgetContract.AccEntry;
import com.example.android.mybudget.data.BudgetContract.CatEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static java.security.AccessController.getContext;

/**
 * Created by andre on 28/02/2017.
 */

public class FunctionHelper {

    public static Date calculateNextDate(int periodID, long currDate) {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat dfYear = new SimpleDateFormat("yyyy");
        SimpleDateFormat dfMonth = new SimpleDateFormat("MM");
        SimpleDateFormat dfDay = new SimpleDateFormat("dd");

        int currYear = Integer.parseInt(dfYear.format(currDate));
        int currMonth = Integer.parseInt(dfMonth.format(currDate));
        int currDay = Integer.parseInt(dfDay.format(currDate));

        long longNextDate;
        Date nextDate = null;
        switch (periodID) {
            case 1: // weekly
                longNextDate = currDate + 86400000 * 7;
                nextDate = new Date(longNextDate);
                break;
            case 2: // fortnightly
                longNextDate = currDate + 86400000 * 14;
                nextDate = new Date(longNextDate);
                break;
            case 3: // monthly
                int nextMonth = currMonth + 1;
                if (nextMonth == 13) {
                    nextMonth = 1;
                    currYear += 1;
                }
                if ((currDay > 28 && nextMonth == 2) || (currDay > 30 && (nextMonth == 4 || nextMonth == 6 || nextMonth == 9 || nextMonth == 11))) {
                    currDay = 1;
                    nextMonth += 1;
                }
                String dateString = currDay + "/" + nextMonth + "/" + currYear;
                try {
                    nextDate = df.parse(dateString);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;
            case 4: // quarterly
                int nextMonth3 = currMonth + 3;
                if (nextMonth3 > 12) {
                    nextMonth3 -= 12;
                    currYear += 1;
                }
                if ((currDay > 28 && nextMonth3 == 2) || (currDay > 30 && (nextMonth3 == 4 || nextMonth3 == 6 || nextMonth3 == 9 || nextMonth3 == 11))) {
                    currDay = 1;
                    nextMonth3 += 1;
                }
                String dateString3 = currDay + "/" + nextMonth3 + "/" + currYear;
                try {
                    nextDate = df.parse(dateString3);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;
            case 5: // yearly
                int nextYear = currYear + 1;
                String dateString5 = currDay + "/" + currMonth + "/" + nextYear;
                try {
                    nextDate = df.parse(dateString5);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;
        }
        return nextDate;
    }

    public static String calculatePrevOrNextMonth(String prevornext, long currDate) {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat dfYear = new SimpleDateFormat("yyyy");
        SimpleDateFormat dfMonth = new SimpleDateFormat("MM");

        String startDateString, endDateString;
        int nextMonth, nextYear, prevMonth, prevYear, monthAfter, yearAfter;
        monthAfter = yearAfter = 0;
        startDateString = endDateString = "";
        int currYear = Integer.parseInt(dfYear.format(currDate));
        int currMonth = Integer.parseInt(dfMonth.format(currDate));

        if (prevornext.equals("next")) {
            nextMonth = currMonth + 1;
            nextYear = currYear;
            if (nextMonth == 13) {
                nextMonth = 1;
                nextYear += 1;
            }
            monthAfter = nextMonth + 1;
            yearAfter = nextYear;
            if (monthAfter == 13) {
                monthAfter = 1;
                yearAfter += 1;
            }

            startDateString = "01/" + nextMonth + "/" + nextYear;
            endDateString = "01/" + monthAfter + "/" + yearAfter;
        } else {
            prevMonth = currMonth - 1;
            prevYear = currYear;
            if (prevMonth == 0){
                prevMonth = 12;
                prevYear -=1;
            }
            startDateString = "01/" + prevMonth + "/" + prevYear;
            endDateString = "01/" + currMonth + "/" + currYear;
        }
        long endDate = 0;
        try {
            endDate = df.parse(endDateString).getTime() - 86400000;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date date = new Date(endDate);
        endDateString = df.format(date);
        return startDateString + "-" + endDateString;
    }

    public static void updateBalance(Context context, int transID, long oldDate, long newDate, int accNumber){
        long thisDate;
        float currBalance = 0;

        if(newDate > oldDate){
            thisDate = oldDate;
        } else {
            thisDate = newDate;
        }

        // finding the transaction immediately before the relevant transaction to get its balance
        String[] projection = {
                TransEntry._ID,
                TransEntry.COLUMN_TRANS_DATE,
                TransEntry.COLUMN_TRANS_ACCOUNT,
                TransEntry.COLUMN_TRANS_AMOUNT,
                TransEntry.COLUMN_ACCOUNT_BALANCE};
        String selection1 = TransEntry.COLUMN_TRANS_ACCOUNT + "=? AND " + TransEntry.COLUMN_TRANS_DATE + "<=? AND " + TransEntry._ID + "<?";
        String[] selectionArgs1 = {String.valueOf(accNumber),
                String.valueOf(thisDate), String.valueOf(transID)};
        String sortBy1 = TransEntry.COLUMN_TRANS_DATE + " DESC, " + TransEntry._ID + " DESC LIMIT 1";
        Cursor cursor1 = context.getContentResolver().query(TransEntry.CONTENT_URI, projection, selection1, selectionArgs1, sortBy1);

        if(cursor1.getCount() >= 1){
            cursor1.moveToFirst();
            currBalance = cursor1.getFloat(cursor1.getColumnIndexOrThrow(TransEntry.COLUMN_ACCOUNT_BALANCE));
        }

        // now, we'll update all transactions that take place after the relevant transaction
        String selection2 = TransEntry.COLUMN_TRANS_ACCOUNT + "=? AND " + TransEntry.COLUMN_TRANS_DATE + ">=?";
        String[] selectionArgs2 = {String.valueOf(accNumber), String.valueOf(thisDate)};
        String sortBy2 = TransEntry.COLUMN_TRANS_DATE + " ASC, " + TransEntry._ID + " ASC";
        Cursor cursor2 = context.getContentResolver().query(TransEntry.CONTENT_URI, projection, selection2, selectionArgs2, sortBy2);
        while (cursor2.moveToNext()){
            long cursorDate = cursor2.getLong(cursor2.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_DATE));
            int cursorID = cursor2.getInt(cursor2.getColumnIndexOrThrow(TransEntry._ID));
            float cursorAmount = cursor2.getFloat(cursor2.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_AMOUNT));
            if(cursorDate == thisDate && cursorID < transID){
                continue;
            }
            currBalance += cursorAmount;
            // update transaction with new balance
            ContentValues values = new ContentValues();
            values.put(TransEntry.COLUMN_ACCOUNT_BALANCE, currBalance);
            String selection3 = TransEntry._ID + "=?";
            String[] selectionArgs3 = {String.valueOf(cursorID)};
            int updatedRows = context.getContentResolver().update(TransEntry.CONTENT_URI, values, selection3, selectionArgs3);
        }
    }

    public static float displayBalance(Context context, ArrayList<Integer> accIDs, long dateTo){

        float totalBalance = 0;
        if(accIDs.size()<1){ // if there are no accounts selected in the filter, tally up the total for all accounts
            String[] projection0 = {AccEntry._ID};
            Cursor cursor0 = context.getContentResolver().query(AccEntry.CONTENT_URI, projection0, null, null, null);
            while(cursor0.moveToNext()){
                accIDs.add(cursor0.getInt(cursor0.getColumnIndexOrThrow(AccEntry._ID)));
            }
        }
        for(int i=0; i < accIDs.size(); i++){ // cycle through all accounts in the array and add the latest balance at dateTo to the total balance
            int accID = accIDs.get(i);
            String[] projection = {
                    TransEntry._ID,
                    TransEntry.COLUMN_TRANS_DATE,
                    TransEntry.COLUMN_TRANS_ACCOUNT,
                    TransEntry.COLUMN_ACCOUNT_BALANCE};
            String selection = TransEntry.COLUMN_TRANS_ACCOUNT + "=? AND " + TransEntry.COLUMN_TRANS_DATE + "<=?";
            String[] selectionArgs = {String.valueOf(accID), String.valueOf(dateTo)};
            String sortBy = TransEntry.COLUMN_TRANS_DATE + " DESC, " + TransEntry._ID + " DESC LIMIT 1";
            Cursor cursor = context.getContentResolver().query(TransEntry.CONTENT_URI, projection, selection, selectionArgs, sortBy);
            if(cursor.moveToNext()){
                totalBalance += cursor.getFloat(cursor.getColumnIndexOrThrow(TransEntry.COLUMN_ACCOUNT_BALANCE));
            }
        }
        return totalBalance;
    }

    public static NotificationManager mManager;

    @SuppressWarnings("static-access")
    public static void generateNotification (Context context){
        mManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        Intent intent1 = new Intent(context, ActivityRecurring.class);
        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(context, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent backupIntent = new Intent(context, NotificationResult.class);
        backupIntent.setAction("Backup");
        PendingIntent pendingIntentBackup = PendingIntent.getBroadcast(context, 12345, backupIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent cancelIntent = new Intent(context, NotificationResult.class);
        cancelIntent.setAction("Cancel");
        PendingIntent pendingIntentCancel = PendingIntent.getBroadcast(context, 12345, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.money)
                    .setContentTitle(context.getString(R.string.notification_title))
                    .setContentText(context.getString(R.string.notification_text))
                    .setAutoCancel(true)
                    .setContentIntent(pendingNotificationIntent)
                    .addAction(0, context.getString(R.string.backup), pendingIntentBackup)
                    .addAction(0, context.getString(R.string.cancel), pendingIntentCancel);
        mManager.notify(122, mBuilder.build());
    }

    public static void triggerAlarm(Context context){
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 2);
        calendar.set(Calendar.HOUR_OF_DAY, 7);
        calendar.set(Calendar.MINUTE, 32);

        Intent myIntent = new Intent (context, BudgetReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent, 0);
        // alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000 * 60 * 60 * 24 * 28, pendingIntent);
    }

    public static void exportToCSV(Context context) {
        String[] projection10 = {
                TransEntry._ID,
                TransEntry.COLUMN_TRANS_RECONCILEDFLAG,
                TransEntry.COLUMN_TRANS_DATE,
                TransEntry.COLUMN_TRANS_DESC,
                TransEntry.COLUMN_TRANS_EST,
                TransEntry.COLUMN_TRANS_ACCOUNT,
                TransEntry.COLUMN_TRANS_AMOUNT,
                TransEntry.COLUMN_TRANS_CAT,
                TransEntry.COLUMN_TRANS_SUBCAT,
                TransEntry.COLUMN_TRANS_TAXFLAG,
                TransEntry.COLUMN_TRANS_DATEUPD,
                TransEntry.COLUMN_TRANS_RECURRINGID,
                TransEntry.COLUMN_ACCOUNT_BALANCE};
        String sortBy10 = TransEntry.COLUMN_TRANS_DATE + " ASC";
        Cursor transCursor = context.getContentResolver().query(TransEntry.CONTENT_URI, projection10, null, null, sortBy10);

        String columnString1 = TransEntry._ID + "," +
                TransEntry.COLUMN_TRANS_RECONCILEDFLAG + "," +
                TransEntry.COLUMN_TRANS_DATE + "," +
                TransEntry.COLUMN_TRANS_DESC + "," +
                TransEntry.COLUMN_TRANS_EST + "," +
                TransEntry.COLUMN_TRANS_ACCOUNT + "," +
                TransEntry.COLUMN_TRANS_AMOUNT + "," +
                TransEntry.COLUMN_TRANS_CAT + "," +
                TransEntry.COLUMN_TRANS_SUBCAT + "," +
                TransEntry.COLUMN_TRANS_TAXFLAG + "," +
                TransEntry.COLUMN_TRANS_DATEUPD + "," +
                TransEntry.COLUMN_TRANS_RECURRINGID + "," +
                TransEntry.COLUMN_ACCOUNT_BALANCE;

        String dataString1 = "";
        while(transCursor.moveToNext()){
            dataString1 += transCursor.getInt(transCursor.getColumnIndexOrThrow(TransEntry._ID)) + ",";
            dataString1 += transCursor.getInt(transCursor.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_RECONCILEDFLAG)) + ",";
            dataString1 += transCursor.getLong(transCursor.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_DATE)) + ",";
            dataString1 += transCursor.getString(transCursor.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_DESC)) + ",";
            dataString1 += transCursor.getString(transCursor.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_EST)) + ",";
            dataString1 += transCursor.getInt(transCursor.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_ACCOUNT)) + ",";
            dataString1 += transCursor.getFloat(transCursor.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_AMOUNT)) + ",";
            dataString1 += transCursor.getInt(transCursor.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_CAT)) + ",";
            dataString1 += transCursor.getInt(transCursor.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_SUBCAT)) + ",";
            dataString1 += transCursor.getInt(transCursor.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_TAXFLAG)) + ",";
            dataString1 += transCursor.getLong(transCursor.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_DATEUPD)) + ",";
            dataString1 += transCursor.getInt(transCursor.getColumnIndexOrThrow(TransEntry.COLUMN_TRANS_RECURRINGID)) + ",";
            dataString1 += transCursor.getFloat(transCursor.getColumnIndexOrThrow(TransEntry.COLUMN_ACCOUNT_BALANCE)) + "\n";
        }
        transCursor.close();
        String combinedString1 = columnString1 + "\n" + dataString1;

        String[] projection11 = {
                CatEntry._ID,
                CatEntry.COLUMN_CATNAME};
        String sortBy11 = CatEntry._ID + " ASC";
        Cursor catCursor = context.getContentResolver().query(CatEntry.CONTENT_URI, projection11, null, null, sortBy11);

        String columnString2 = CatEntry._ID + "," +
                CatEntry.COLUMN_CATNAME;

        String dataString2 = "";
        while(catCursor.moveToNext()){
            dataString2 += catCursor.getInt(catCursor.getColumnIndexOrThrow(CatEntry._ID)) + ",";
            dataString2 += catCursor.getString(catCursor.getColumnIndexOrThrow(CatEntry.COLUMN_CATNAME)) + "\n";
        }
        catCursor.close();
        String combinedString2 = columnString2 + "\n" + dataString2;

        String[] projection12 = {
                AccEntry._ID,
                AccEntry.COLUMN_ACCNAME};
        String sortBy12 = AccEntry._ID + " ASC";
        Cursor accCursor = context.getContentResolver().query(AccEntry.CONTENT_URI, projection12, null, null, sortBy12);

        String columnString3 = AccEntry._ID + "," + AccEntry.COLUMN_ACCNAME;

        String dataString3 = "";
        while(accCursor.moveToNext()){
            dataString3 += accCursor.getInt(accCursor.getColumnIndexOrThrow(AccEntry._ID)) + ",";
            dataString3 += accCursor.getString(accCursor.getColumnIndexOrThrow(AccEntry.COLUMN_ACCNAME)) + "\n";
        }
        accCursor.close();
        String combinedString3 = columnString3 + "\n" + dataString3;

        File file1 = null;
        File file2 = null;
        File file3 = null;
        File root = Environment.getExternalStorageDirectory();
        if (root.canWrite()){
            File dir = new File (root.getAbsolutePath() + "/BudgetData");
            dir.mkdirs();
            file1 = new File(dir, "Transactions.csv");
            file2 = new File(dir, "Categories.csv");
            file3 = new File(dir, "Accounts.csv");
            FileOutputStream out1 = null;
            FileOutputStream out2 = null;
            FileOutputStream out3 = null;
            try {
                out1 = new FileOutputStream(file1);
                out2 = new FileOutputStream(file2);
                out3 = new FileOutputStream(file3);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                out1.write(combinedString1.getBytes());
                out2.write(combinedString2.getBytes());
                out3.write(combinedString3.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                out1.close();
                out2.close();
                out3.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Uri u1 = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file1);
        Uri u2 = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file2);
        Uri u3 = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file3);
        ArrayList<Uri> uris = new ArrayList<>();
        uris.add(u1);
        uris.add(u2);
        uris.add(u3);

        Intent sendIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Budget data");
        sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        sendIntent.setType("text/html");
        context.startActivity(sendIntent);
    }
}
