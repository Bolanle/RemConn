package me.bolanleonifade.remoteconnection;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SessionDBHelper extends SQLiteOpenHelper {

    private static final String DBNAME = "session.db";
    private static final int VERSION = 1;

    //Create table, using contract constants
    private static String CREATE_DB = " CREATE TABLE "
            + SessionContract.TABLE + " ( "
            + SessionContract.ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + SessionContract.TYPE + " INT NOT NULL, "
            + SessionContract.HOST + " CHAR(100) NOT NULL, "
            + SessionContract.USER + " CHAR(15) NULL, "
            + SessionContract.PORT + " INT  NULL, "
            + SessionContract.PASSWORD + " CHAR(100)  NULL, "
            + SessionContract.REMOTE_DIRECTORY + " CHAR(1000)  NULL, "
            + SessionContract.LOCAL_DIRECTORY + " CHAR(1000)  NULL, "
            + SessionContract.SAVE_DELETED_FILES + " BOOLEAN  NULL, "
            + SessionContract.SAVE_OVERWRITTEN_FILES + " BOOLEAN  NULL, "
            + SessionContract.RECYCLE_BIN + " CHAR(200)  NULL, "
            + SessionContract.SSHKEY + " CHAR(500)  NULL, "
            + SessionContract.SSHPASS + " CHAR(500)  NULL, "
            + SessionContract.TURN_ON_SYNCHRONISATION + " BOOLEAN  NULL, "
            + SessionContract.MASTER + " CHAR(100)  NULL, "
            + SessionContract.SYNC_LOCAL_DIRECTORY + " CHAR(1000)  NULL, "
            + SessionContract.SYNC_REMOTE_DIRECTORY + " CHAR(1000)  NULL, "
            + SessionContract.RECURSIVE + " BOOLEAN NULL, "
            + SessionContract.MOBILE_NETWORK + " BOOLEAN NULL, "
            + SessionContract.DAYS + " CHAR(300)  NULL, "
            + SessionContract.TIME + " CHAR(300)  NULL "
            + ");";

    private static String DROP_DB = "DROP TABLE IF EXISTS "
            + SessionContract.TABLE;

    public SessionDBHelper(Context context) {
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