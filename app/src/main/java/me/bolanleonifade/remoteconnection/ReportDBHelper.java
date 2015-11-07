package me.bolanleonifade.remoteconnection;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ReportDBHelper extends SQLiteOpenHelper {

    private static final String DBNAME = "report.db";
    private static final int VERSION = 1;

    //Create table, using contract constants
    private static String CREATE_DB = " CREATE TABLE "
            + ReportContract.TABLE + " ( "
            + ReportContract.ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ReportContract.STATUS + " CHAR(10)  NULL, "
            + ReportContract.HOST + " CHAR(100) NOT NULL, "
            + ReportContract.USER + " CHAR(15) NULL, "
            + ReportContract.TYPE + " INT NOT NULL, "
            + ReportContract.LOCAL_DIRECTORY + " CHAR(1000) NULL, "
            + ReportContract.REMOTE_DIRECTORY + " CHAR(1000) NULL, "
            + ReportContract.TIMESTAMP + " CHAR(50)  NULL, "
            + ReportContract.FILES_UPLOADED + " CHAR(1000)  NULL, "
            + ReportContract.FILES_DOWNLOADED + " CHAR(1000)  NULL, "
            + ReportContract.ERROR + " CHAR(1000)  NULL "
            + ");";

    private static String DROP_DB = "DROP TABLE IF EXISTS "
            + ReportContract.TABLE;

    public ReportDBHelper(Context context) {
        super(context, DBNAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DB);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // DROP OLD TABLE
        db.execSQL(DROP_DB);
        onCreate(db);
    }

}