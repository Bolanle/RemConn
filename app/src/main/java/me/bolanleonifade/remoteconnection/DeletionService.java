package me.bolanleonifade.remoteconnection;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class DeletionService extends IntentService {
    public static String ACTION_DELETE = "me.bolanleonifade.remoteconnection.service.delete";


    public DeletionService() {
        super("DeletionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            ReportDataSource reportDataSource = new ReportDataSource(this.getApplicationContext());
            ArrayList<Report> reports = reportDataSource.getReports();
            SharedPreferences preferences = getSharedPreferences(SessionContract.PREFERENCES, MODE_PRIVATE);
            int deleteWhen = preferences.getInt(SessionContract.PREFERRED_DELETE_WHEN, 0);
            for (Report report : reports) {
                DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm");
                try {
                    Calendar reportCalendar = Calendar.getInstance();
                    reportCalendar.setTime(formatter.parse(report.getTimeStamp().trim()));

                    Calendar todayCalendar = Calendar.getInstance();
                    todayCalendar.setTimeInMillis(System.currentTimeMillis());

                    if (deleteWhen == 0) {
                        todayCalendar.add(Calendar.DAY_OF_YEAR, -7);

                        if (reportCalendar.compareTo(todayCalendar) != 1) {
                            reportDataSource.deleteReport(report);
                        }
                    }else if(deleteWhen == 1) {
                        todayCalendar.add(Calendar.DAY_OF_YEAR, -14);

                        if (reportCalendar.compareTo(todayCalendar) != 1) {
                            reportDataSource.deleteReport(report);
                        }
                    }else if(deleteWhen == 2) {
                        todayCalendar.add(Calendar.MONTH, -1);

                        if (reportCalendar.compareTo(todayCalendar) != 1) {
                            reportDataSource.deleteReport(report);
                        }
                    }else if(deleteWhen == 3) {
                        todayCalendar.add(Calendar.MONTH, -6);

                        if (reportCalendar.compareTo(todayCalendar) != 1) {
                            reportDataSource.deleteReport(report);
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
