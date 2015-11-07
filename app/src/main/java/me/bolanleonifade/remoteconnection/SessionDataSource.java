package me.bolanleonifade.remoteconnection;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

//Wrapper class for provider - provides a layer between data and activities
public class SessionDataSource {
    private Context context;
    private SQLiteDatabase db;
    private SessionDBHelper dbHelper;

    public SessionDataSource(Context context) {
        this.context = context;
        dbHelper = new SessionDBHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    private Session cursorToSession(Cursor cursor) {
        int type = cursor.getInt(1);
        if (type == SessionContract.FTP) {
            FTPSession session = new FTPSession();
            session.setId(cursor.getInt(0));
            session.setHost(cursor.getString(2));
            session.setUser(cursor.getString(3));
            session.setPort(cursor.getInt(4));
            session.setPassword(cursor.getString(5));

            session.setRemoteDirectory(cursor.getString(6));
            session.setLocalDirectory(cursor.getString(7));
            session.setSaveDeletedFiles(cursor.getInt(8) > 0);
            session.setSaveOverwrittenFile(cursor.getInt(9) > 0);
            session.setRecycleBin(cursor.getString(10));

            session.setTurnOnSynchronisation(cursor.getInt(13) > 0);
            session.setMaster(cursor.getString(14));
            session.setSyncLocalDirectory(cursor.getString(15));
            session.setSyncRemoteDirectory(cursor.getString(16));
            session.setRecursive(cursor.getInt(17) > 0);
            session.setMobileNetwork(cursor.getInt(18) > 0);

            session.setDays((cursor.getString(19)));
            session.setTime(cursor.getString(20));
            return session;

        } else {
            SFTPSession session = new SFTPSession();
            session.setId(cursor.getInt(0));
            session.setHost(cursor.getString(2));
            session.setUser(cursor.getString(3));
            session.setPort(cursor.getInt(4));
            session.setPassword(cursor.getString(5));

            session.setRemoteDirectory(cursor.getString(6));
            session.setLocalDirectory(cursor.getString(7));
            session.setSaveDeletedFiles(cursor.getInt(8) > 0);
            session.setSaveOverwrittenFile(cursor.getInt(9) > 0);
            session.setRecycleBin(cursor.getString(10));

            session.setSSHKey(cursor.getString(11));
            session.setSSHPass(cursor.getString(12));

            session.setTurnOnSynchronisation(cursor.getInt(13) > 0);
            session.setMaster(cursor.getString(14));
            session.setSyncLocalDirectory(cursor.getString(15));
            session.setSyncRemoteDirectory(cursor.getString(16));
            session.setRecursive(cursor.getInt(17) > 0);
            session.setMobileNetwork(cursor.getInt(18) > 0);

            session.setDays(cursor.getString(19));
            session.setTime(cursor.getString(20));
            return session;
        }
    }

    public int createSession(String host, String user, int type, int port, String password,
                              String remoteDirectory, String localDirectory, boolean saveDeletedFiles,
                              boolean saveOverwrittenFile, String recycleBin,
                               String sshKey, String sshPass, boolean turnOnSynchronisation, String master,
                              String syncLocalDirectory, String syncRemoteDirectory,
                              boolean recursive, boolean mobileNetwork, String days,
                              String time) {
        int newId = 0;
        ContentValues values = new ContentValues();
        values.put(SessionContract.HOST, host);
        values.put(SessionContract.USER, user);
        values.put(SessionContract.PORT, port);
        values.put(SessionContract.TYPE, type);
        values.put(SessionContract.PASSWORD, password);

        values.put(SessionContract.REMOTE_DIRECTORY, remoteDirectory);
        values.put(SessionContract.LOCAL_DIRECTORY, localDirectory);
        values.put(SessionContract.SAVE_DELETED_FILES, saveDeletedFiles);
        values.put(SessionContract.SAVE_OVERWRITTEN_FILES, saveOverwrittenFile);
        values.put(SessionContract.RECYCLE_BIN, recycleBin);

        values.put(SessionContract.SSHKEY, sshKey);
        values.put(SessionContract.SSHPASS, sshPass);

        values.put(SessionContract.TURN_ON_SYNCHRONISATION, turnOnSynchronisation);
        values.put(SessionContract.MASTER, master);
        values.put(SessionContract.SYNC_LOCAL_DIRECTORY, syncLocalDirectory);
        values.put(SessionContract.SYNC_REMOTE_DIRECTORY, syncRemoteDirectory);
        values.put(SessionContract.RECURSIVE, recursive);
        values.put(SessionContract.MOBILE_NETWORK, mobileNetwork);

        values.put(SessionContract.DAYS, days);
        values.put(SessionContract.TIME, time);

        db.insert(SessionContract.TABLE, null, values);
        String query = "SELECT ROWID FROM "+ SessionContract.TABLE +
                    " ORDER BY ROWID DESC limit 1";
        Cursor c = db.rawQuery(query, null);
        if (c != null && c.moveToFirst()) {
            newId =  ((int) c.getLong(0));
        }
        return  newId;
    }

    public void updateSession(int id, String host, String user, int type, int port, String password,
                              String remoteDirectory, String localDirectory, boolean saveDeletedFiles,
                              boolean saveOverwrittenFile, String recycleBin,
                               String sshKey, String sshPass, boolean turnOnSynchronisation, String master,
                              String syncLocalDirectory, String syncRemoteDirectory,
                              boolean recursive, boolean mobileNetwork, String days,
                              String time) {
        ContentValues values = new ContentValues();
        values.put(SessionContract.HOST, host);
        values.put(SessionContract.USER, user);
        values.put(SessionContract.TYPE, type);
        values.put(SessionContract.PORT, port);
        values.put(SessionContract.PASSWORD, password);

        values.put(SessionContract.REMOTE_DIRECTORY, remoteDirectory);
        values.put(SessionContract.LOCAL_DIRECTORY, localDirectory);
        values.put(SessionContract.SAVE_DELETED_FILES, saveDeletedFiles);
        values.put(SessionContract.SAVE_OVERWRITTEN_FILES, saveOverwrittenFile);
        values.put(SessionContract.RECYCLE_BIN, recycleBin);

        values.put(SessionContract.SSHKEY, sshKey);
        values.put(SessionContract.SSHPASS, sshPass);

        values.put(SessionContract.TURN_ON_SYNCHRONISATION, turnOnSynchronisation);
        values.put(SessionContract.MASTER, master);
        values.put(SessionContract.SYNC_LOCAL_DIRECTORY, syncLocalDirectory);
        values.put(SessionContract.SYNC_REMOTE_DIRECTORY, syncRemoteDirectory);
        values.put(SessionContract.RECURSIVE, recursive);
        values.put(SessionContract.MOBILE_NETWORK, mobileNetwork);

        values.put(SessionContract.DAYS, days);
        values.put(SessionContract.TIME, time);

        db.update(SessionContract.TABLE, values, SessionContract.ID + "=" + id, null);
    }

    public void deleteSession(Session session) {
        int id = session.getId();

        db.delete(SessionContract.TABLE, SessionContract.ID + "=" + id, null);
    }

    public ArrayList<Session> getSessions() {
        ArrayList<Session> sessions = new ArrayList<Session>();

        Cursor cursor = db.query(false, SessionContract.TABLE,
                null, null, null, null, null,
                SessionContract.SORT_ORDER_DEFAULT, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Session session = cursorToSession(cursor);
            sessions.add(session);
            cursor.moveToNext();
        }
        cursor.close();
        return sessions;
    }

    public Session getSession(int id) {

        Cursor cursor = db.query(false, SessionContract.TABLE,
                null, "id == " + id, null, null, null,
                SessionContract.SORT_ORDER_DEFAULT, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Session session = cursorToSession(cursor);
            return  session;
        }
        return null;
    }
}
