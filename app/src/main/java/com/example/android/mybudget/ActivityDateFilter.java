package com.example.android.mybudget;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.example.android.mybudget.R.id.date;


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

    private int year, month, day, year_end, month_end, day_end;
    private String selectDateType = "";
    private TextView mTVStartDate, mTVEndDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_filter);

        filterRadGroup = (RadioGroup) findViewById(R.id.date_filter_radgroup);

        SharedPreferences dateFilter = getSharedPreferences(PREFS_NAME, 0);
        stDateFrom = dateFilter.getString("stDateFrom", "");
        stDateTo = dateFilter.getString("stDateTo", "");
        stFilterType = dateFilter.getString("stFilterType", "All transactions");
        dateFrom = dateFilter.getLong("dateFrom", 0);
        dateTo = dateFilter.getLong("dateTo", 0);

        mTVStartDate = (TextView) findViewById(R.id.tv_start_date);
        mTVEndDate = (TextView) findViewById(R.id.tv_end_date);

        if (stDateFrom.isEmpty()) {
            Calendar calendar = Calendar.getInstance();
            year = year_end = calendar.get(Calendar.YEAR);
            month = month_end = calendar.get(Calendar.MONTH);
            day = day_end = calendar.get(Calendar.DAY_OF_MONTH);
            mTVStartDate.setText(showDate(day, month, year));
            mTVEndDate.setText(showDate(day, month, year));
        } else {
            mTVStartDate.setText(stDateFrom);
            mTVEndDate.setText(stDateTo);
            year = Integer.parseInt(dfYear.format(dateFrom));
            month = Integer.parseInt(dfMonth.format(dateFrom)) - 1;
            day = Integer.parseInt(dfDay.format(dateFrom));
            year_end = Integer.parseInt(dfYear.format(dateTo));
            month_end = Integer.parseInt(dfMonth.format(dateTo)) - 1;
            day_end = Integer.parseInt(dfDay.format(dateTo));
        }

        View thisView;
        RadioButton selectedButton;
        for (int i = 0; i < filterRadGroup.getChildCount(); i++ ){
            thisView = findViewById(filterRadGroup.getChildAt(i).getId());
            if (thisView instanceof RadioButton) {
                selectedButton = (RadioButton) findViewById(filterRadGroup.getChildAt(i).getId());
                if (selectedButton.getText().equals(stFilterType)) {
                    selectedButton.setChecked(true);
                    if(selectedButton.getText().equals("Custom period")){
                        LinearLayout custom_options = (LinearLayout) findViewById(R.id.custom_period_layout);
                        custom_options.setVisibility(View.VISIBLE);
                    }
                    break;
                }
            }
        }

        final RadioButton customButton = (RadioButton) findViewById(R.id.date_filter_option_custom);
        customButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                LinearLayout custom_options = (LinearLayout) findViewById(R.id.custom_period_layout);
                if(customButton.isChecked()){
                    custom_options.setVisibility(View.VISIBLE);
                } else {
                    custom_options.setVisibility(View.GONE);
                }
            }
        });

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
        // TODO: add check for dateTo < dateFrom
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
                stFilterType = "Month";
                try {
                    dateFrom = df.parse(stDateFrom).getTime();
                    dateTo = df.parse(stDateTo).getTime();
                    dateTo -= 86400000; // this number equals 24 hours: I've determined the first day in the next month in milliseconds, then deducted 24h
                    stDateTo = Integer.parseInt(dfDay.format(dateTo)) + "/" + Integer.parseInt(dfMonth.format(dateTo)) + "/" + Integer.parseInt(dfYear.format(dateTo));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;
            case "Custom period":
                stDateFrom = mTVStartDate.getText().toString();
                stDateTo = mTVEndDate.getText().toString();
                stFilterType = "Custom period";
                try {
                    dateFrom = df.parse(stDateFrom).getTime();
                    dateTo = df.parse(stDateTo).getTime();
                } catch (ParseException e){
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

    @SuppressWarnings("deprecation")
    public void showDatePickerDialog(View view) {
        if (getResources().getResourceName(view.getId()).contains("layout_startdate")) {
            showDialog(900);
        }
        if (getResources().getResourceName(view.getId()).contains("layout_enddate")) {
            showDialog(999);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    protected Dialog onCreateDialog(int id) {
        switch (id){
            case 900:
                selectDateType = "start";
                break;
            case 999:
                selectDateType = "end";
        }
        if (id == 900) {
            return new DatePickerDialog(this, myDateListener, year, month, day);
        }
        if (id == 999) {
            return new DatePickerDialog(this, myDateListener, year_end, month_end, day_end);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int thisYear, int thisMonth, int thisDay) {
            if (selectDateType.equals("start")){
                mTVStartDate.setText(showDate(thisDay, thisMonth, thisYear));
            }
            if (selectDateType.equals("end")){
                mTVEndDate.setText(showDate(thisDay, thisMonth, thisYear));
            }
        }
    };

    private StringBuilder showDate (int thisDay, int thisMonth, int thisYear){
        return new StringBuilder().append(thisDay).append("/").append(thisMonth + 1).append("/").append(thisYear);
    }
}

