package me.bolanleonifade.remoteconnection;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import org.apache.commons.vfs2.FileSystemException;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;


public class SynchronisationService extends IntentService {
                    public static String ACTION_SYNC = "me.bolanleonifade.remoteconnection.service.sync";

                    private FTPSession ftpSession;
                    private SFTPSession sftpSession;

                    private Connection connection;
                    private ReportDataSource reportDataSource;

                    private String host = "";
                    private String user = "";
                    private int type = SessionContract.NONE;
                    private String localDirectory;
                    private String remoteDirectory;
                    private String status = "";
                    private String timeStamp = "";
                    private String filesUploaded = "";
                    private String filesDownloaded = "";
                    private String error = "";

                    public SynchronisationService() {
                        super("SynchronisationService");
                    }

                    @Override
                    protected void onHandleIntent(Intent intent) {
                        if (intent != null) {
                            try {
                                reportDataSource = new ReportDataSource(this.getApplicationContext());

                                List<String> toTransfer = null;
                                Bundle b = intent.getBundleExtra(SessionContract.CURRENT_SESSION);
                type = b.getInt(SessionContract.TYPE, SessionContract.NONE);
                boolean canConnectOnMobile = true;
                boolean canConnect = false;
                if (type == SessionContract.FTP) {
                    ftpSession = b.getParcelable(FileSystemActivity.SESSION_STRING);
                    connection = new Connection(ftpSession, type);
                    canConnectOnMobile = ftpSession.isMobileNetwork();

                } else if (type == SessionContract.SFTP) {
                    sftpSession = b.getParcelable(FileSystemActivity.SESSION_STRING);
                    connection = new Connection(sftpSession, type);
                    canConnectOnMobile = sftpSession.isMobileNetwork();
                }

                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo info = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                canConnect = info.isConnectedOrConnecting();

                SharedPreferences preferences = getSharedPreferences(SessionContract.PREFERENCES, Context.MODE_PRIVATE);
                boolean generalPrefTurnOnSynchronisation = preferences.getBoolean(SessionContract.TURN_ON_SYNCHRONISATION, true);

                if ((canConnect || canConnectOnMobile) && generalPrefTurnOnSynchronisation) {

                    connection.connect();
                    toTransfer = connection.sync();

                }else {
                    throw new FileSystemException("Cannot Synchronise directories. Possible causes: synchronisation may not be turned on," +
                            "session settings may not allow for connection on a mobile network and or there simply" +
                            " was a lack of network connectivity at the time of synchronisation.");
                }
                if (type == SessionContract.FTP) {
                    host = ftpSession.getHost();
                    user = ftpSession.getUser();
                    localDirectory = ftpSession.getSyncLocalDirectory();
                    remoteDirectory = ftpSession.getSyncRemoteDirectory();
                    if (ftpSession.getMaster().equals("Local")) {
                        if (!Utils.listToString(toTransfer, ",").trim().isEmpty())
                            filesUploaded = Utils.listToString(toTransfer, ",");
                        else
                            filesUploaded = "None";
                        filesDownloaded = "None";
                    } else {
                        filesUploaded = "None";
                        if (!Utils.listToString(toTransfer, ",").trim().isEmpty())
                            filesDownloaded = Utils.listToString(toTransfer, ", ");
                        else
                            filesDownloaded = "None";

                    }
                } else if (type == SessionContract.SFTP) {
                    host = sftpSession.getHost();
                    user = sftpSession.getUser();
                    localDirectory = sftpSession.getSyncLocalDirectory();
                    remoteDirectory = sftpSession.getSyncRemoteDirectory();

                    if (sftpSession.getMaster().equals("Local")) {
                        if (!Utils.listToString(toTransfer, ",").trim().isEmpty())
                            filesUploaded = Utils.listToString(toTransfer, ",");
                        else
                            filesUploaded = "None";
                        filesDownloaded = "None";
                    } else {
                        filesUploaded = "None";
                        if (!Utils.listToString(toTransfer, ",").trim().isEmpty())
                            filesDownloaded = Utils.listToString(toTransfer, ", ");
                        else
                            filesDownloaded = "None";

                    }

                }
                this.status = "OK";
                timeStamp = DateFormat.getInstance()
                        .format(new Date(System.currentTimeMillis()));

                error = "None";
                reportDataSource.createSession(status, host, user, type, localDirectory, remoteDirectory, timeStamp, filesUploaded, filesDownloaded, error);
            } catch (FileSystemException e) {
                if (type == SessionContract.FTP) {
                    host = ftpSession.getHost();
                    user = ftpSession.getUser();
                    localDirectory = ftpSession.getSyncLocalDirectory();
                    remoteDirectory = ftpSession.getSyncRemoteDirectory();

                } else if (type == SessionContract.SFTP) {
                    host = sftpSession.getHost();
                    user = sftpSession.getUser();
                    localDirectory = sftpSession.getSyncLocalDirectory();
                    remoteDirectory = sftpSession.getSyncRemoteDirectory();
                }
                filesUploaded = "None";
                filesDownloaded = "None";
                this.status = "ERROR";
                timeStamp = DateFormat.getInstance()
                        .format(new Date(System.currentTimeMillis()));

                error = e.getMessage().replace("Unknown message with code", "").replace("\"", "");

                reportDataSource.createSession(status, host, user, type, localDirectory, remoteDirectory, timeStamp, filesUploaded, filesDownloaded, error);
            }
        }
    }
}
