package com.example.android.mybudget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static android.R.attr.action;

/**
 * Created by andre on 1/04/2017.
 */

public class BudgetReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i("App", "called receiver method ");
        try {
            FunctionHelper.generateNotification(context);
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
