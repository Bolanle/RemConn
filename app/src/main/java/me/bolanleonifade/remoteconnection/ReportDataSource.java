package me.bolanleonifade.remoteconnection;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

//Wrapper class for provider - provides a layer between data and activities
public class ReportDataSource {
    private Context context;
    private SQLiteDatabase db;
    private ReportDBHelper dbHelper;

    public ReportDataSource(Context context) {
        this.context = context;
        dbHelper = new ReportDBHelper(context);
        db = dbHelper.getWritableDatabase();

    }

    private Report cursorToReport(Cursor cursor) {
        Report report = new Report();
        report.setId(cursor.getInt(0));
        report.setStatus(cursor.getString(1));
        report.setHost(cursor.getString(2));
        report.setUser(cursor.getString(3));
        report.setType(cursor.getInt(4));

        report.setLocalDirectory(cursor.getString(5));
        report.setRemoteDirectory(cursor.getString(6));

        report.setTimeStamp(cursor.getString(7));
        report.setFilesUploaded(cursor.getString(8));
        report.setFilesDownloaded(cursor.getString(9));
        report.setError(cursor.getString(10));

        return report;
    }

    public int createSession(String status, String host, String user, int type, String localDirectory, String remoteDirectory,
                              String timeStamp, String filesUploaded, String filesDownloaded,
                             String error) {
        int newId = 0;
        ContentValues values = new ContentValues();
        values.put(ReportContract.STATUS, status);
        values.put(ReportContract.HOST, host);
        values.put(ReportContract.USER, user);
        values.put(ReportContract.TYPE, type);
        values.put(ReportContract.LOCAL_DIRECTORY, localDirectory);
        values.put(ReportContract.REMOTE_DIRECTORY, remoteDirectory);

        values.put(ReportContract.TIMESTAMP, timeStamp);
        values.put(ReportContract.FILES_UPLOADED, filesUploaded);
        values.put(ReportContract.FILES_DOWNLOADED, filesDownloaded);
        values.put(ReportContract.ERROR, error);

        db.insert(ReportContract.TABLE, null, values);
        String query = "SELECT ROWID FROM " + ReportContract.TABLE +
                " ORDER BY ROWID DESC limit 1";
        Cursor c = db.rawQuery(query, null);
        if (c != null && c.moveToFirst()) {
            newId = ((int) c.getLong(0));
        }
        return newId;
    }

    public void updateSession(int id, String status, String host, String user, int type,
                              String localDirectory, String remoteDirectory,
                              String timeStamp, String filesUploaded, String filesDownloaded,
                              String error) {
        ContentValues values = new ContentValues();
        values.put(ReportContract.STATUS, status);
        values.put(ReportContract.HOST, host);
        values.put(ReportContract.USER, user);
        values.put(ReportContract.TYPE, type);
        values.put(ReportContract.LOCAL_DIRECTORY, localDirectory);
        values.put(ReportContract.REMOTE_DIRECTORY, remoteDirectory);
        values.put(ReportContract.TIMESTAMP, timeStamp);

        values.put(ReportContract.FILES_UPLOADED, filesUploaded);
        values.put(ReportContract.FILES_DOWNLOADED, filesDownloaded);
        values.put(ReportContract.ERROR, error);

        db.update(ReportContract.TABLE, values, ReportContract.ID + "=" + id, null);
    }

    public void deleteReport(Report report) {
        int id = report.getId();

        db.delete(ReportContract.TABLE, ReportContract.ID + "=" + id, null);
    }

    public ArrayList<Report> getReports() {
        ArrayList<Report> reports = new ArrayList<Report>();

        Cursor cursor = db.query(false, ReportContract.TABLE,
                null, null, null, null, null,
                ReportContract.SORT_ORDER_DEFAULT, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Report report = cursorToReport(cursor);
            reports.add(report);
            cursor.moveToNext();
        }
        cursor.close();
        return reports;
    }
}
