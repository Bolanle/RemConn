package me.bolanleonifade.remoteconnection;

public final class ReportContract {
    public static String TABLE = "REPORT";
    public static String ID = "ID";

    public static String HOST = "HOST";
    public static String USER = "USER";
    public static String TYPE = "TYPE";
    public static String LOCAL_DIRECTORY = "LOCAL_DIRECTORY";
    public static String REMOTE_DIRECTORY = "REMOTE_DIRECTORY";
    public static String STATUS = "STATUS";
    public static String TIMESTAMP = "TIMESTAMP";
    public static String FILES_UPLOADED = "FILES_UPLOADED";
    public static String FILES_DOWNLOADED = "FILES_DOWNLOADED";
    public static String ERROR = "ERROR";

    public static final String SORT_ORDER_DEFAULT = ID + " COLLATE NOCASE";

    public static int SORT_ASC = 1;
    public static int SORT_DESC = 2;
    public static int SORT_MODIFIED = 3;


}