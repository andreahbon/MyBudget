package com.example.android.mybudget;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
}
