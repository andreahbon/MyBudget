package com.example.android.mybudget;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ActivityDateFilter extends AppCompatActivity {
    public static final String PREFS_NAME = "DatePrefs";

    SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
    SimpleDateFormat dfYear = new SimpleDateFormat("yyyy");
    SimpleDateFormat dfMonth = new SimpleDateFormat("MM");
    SimpleDateFormat dfDay = new SimpleDateFormat("dd");

    String stDateFrom = "";
    String stDateTo = "";
    long dateFrom = 0;
    long dateTo = 0;
    String stFilterType = "";
    RadioGroup filterRadGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_filter);

        filterRadGroup = (RadioGroup) findViewById(R.id.date_filter_radgroup);
        // TODO: if preferences are set, change this
        int selectedButtonID = filterRadGroup.getChildAt(3).getId();
        RadioButton selectedButton = (RadioButton) findViewById(selectedButtonID);
        selectedButton.setChecked(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_filter, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_filter:
                saveFilter();
                SharedPreferences datefilter = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = datefilter.edit();
                editor.putString("stDateFrom", stDateFrom);
                editor.putString("stDateTo", stDateTo);
                editor.putString("stFilterType",stFilterType);
                editor.putLong("dateFrom", dateFrom);
                editor.putLong("dateTo", dateTo);
                editor.commit();
                setResult(RESULT_OK, null);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveFilter(){
        int filterSelectedButton = filterRadGroup.getCheckedRadioButtonId();
        RadioButton selectedButton = (RadioButton) findViewById(filterSelectedButton);
        switch (selectedButton.getText().toString()) {
            case "Month":
                Date todayDate = new Date(System.currentTimeMillis());
                long todayMilli = todayDate.getTime();
                int todayMonth = Integer.parseInt(dfMonth.format(todayMilli));
                int todayYear = Integer.parseInt(dfYear.format(todayMilli));
                stDateFrom = "01/" + todayMonth + "/" + todayYear;
                int nextMonth = todayMonth + 1;
                int nextMonthYear = todayYear;
                if (nextMonth == 13) {
                    nextMonth = 1;
                    nextMonthYear += 1;
                }
                stDateTo = "01/" + nextMonth + "/" + nextMonthYear;
                stFilterType = "This month";
                try {
                    dateFrom = df.parse(stDateFrom).getTime();
                    dateTo = df.parse(stDateTo).getTime();
                    dateTo -= 86400000;
                    stDateTo = Integer.parseInt(dfDay.format(dateTo)) + "/" + Integer.parseInt(dfMonth.format(dateTo)) + "/" + Integer.parseInt(dfYear.format(dateTo));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;
            case "All transactions":
                stDateFrom = stDateTo = "";
                stFilterType = "All transactions";
                dateFrom = dateTo = 0;
                break;
        }
    }
}

