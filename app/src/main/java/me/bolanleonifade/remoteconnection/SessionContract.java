package me.bolanleonifade.remoteconnection;

public final class SessionContract {

    public static String PREFERENCES = "PREFERENCES";
    public static String TABLE = "SESSION";
    public static String ID = "ID";

    public static String HOST = "HOST";
    public static String USER = "USER";
    public static String PORT = "PORT";
    public static String PASSWORD = "PASSWORD";

    public static String REMOTE_DIRECTORY = "REMOTE_DIRETORY";
    public static String LOCAL_DIRECTORY = "LOCAL_DIRECTORY";
    public static String SAVE_DELETED_FILES = "SAVE_DELETED_FILES";
    public static String SAVE_OVERWRITTEN_FILES = "SAVE_OVERWRITTEN_FILES";
    public static String RECYCLE_BIN = "RECYCLE_BIN";

    public static String TIME_OUT = "TIME_OUT";
    public static String KEEP_ALIVE = "KEEP_ALIVE";

    public static String SSHKEY = "SSHKEY";
    public static String SSHPASS = "SSHPASS";

    public static String TURN_ON_SYNCHRONISATION = "TURN_ON_SYNCHRONISATION";
    public static String MASTER = "MASTER";
    public static String SYNC_LOCAL_DIRECTORY = "SYNC_LOCAL_DIRECTORY";
    public static String SYNC_REMOTE_DIRECTORY = "SYNC_REMOTE_DIRECTORY";
    public static String RECURSIVE = "RECURSIVE";
    public static String MOBILE_NETWORK = "MOBILE_NETWORK";
    public static String DAYS = "DAYS";
    public static String TIME = "TIME";

    public static int FTP = 1;
    public static int SFTP = 2;
    public static int NONE = -1;

    public static String CURRENT_SESSION = "CURRENT_SESSION";
    public static String TYPE = "TYPE";
    public static final String SORT_ORDER_DEFAULT = ID + " COLLATE NOCASE";

    public static String PREFERRED_LOCAL_DIR = "PREFERRED_LOCAL_DIR";
    public static String PREFERRED_REMOTE_DIR = "PREFERRED_REMOTE_DIR";
    public static String PREFERRED_HIDDEN_FILE_DISPLAY = "PREFERRED_HIDDEN_FILE_DISPLAY";
    public static String PREFERRED_DELETE_WHEN = "PREFERRED_DELETE_WHEN";


    public static int SORT_ASC = 1;
    public static int SORT_DESC = 2;
    public static int SORT_MODIFIED = 3;


}