package com.example.android.mybudget;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by andre on 1/04/2017.
 */

public class NotificationResult extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null) {
            String action = intent.getAction();
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(122);
            if (action.equals("Backup")) {
                Log.i("Notification result", "Pressed BACKUP");
                Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                context.sendBroadcast(it);
                FunctionHelper.exportToCSV(context);
            } else if (action.equals("Cancel")) {
                Log.i("Notification result", "Pressed CANCEL");
            }
        }
    }
}
