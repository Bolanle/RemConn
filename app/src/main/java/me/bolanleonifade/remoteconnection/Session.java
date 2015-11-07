package me.bolanleonifade.remoteconnection;

/**
 * Created by Ester on 18/06/2015.
 */
public abstract class Session {

    private int id;
    private String host;
    private String user;
    private int port;
    private String password;

    private String remoteDirectory;
    private String localDirectory;
    private boolean saveDeletedFiles;
    private boolean saveOverwrittenFile;
    private String recycleBin;

    //private String sshKey;

    private boolean turnOnSynchronisation;
    private String master;
    private String syncLocalDirectory;
    private String syncRemoteDirectory;
    private boolean recursive;
    private boolean mobileNetwork;
    private String days;
    private String time;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLocalDirectory() {
        return localDirectory;
    }

    public void setLocalDirectory(String localDirectory) {
        this.localDirectory = localDirectory;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRemoteDirectory() {
        return remoteDirectory;
    }

    public void setRemoteDirectory(String remoteDirectory) {
        this.remoteDirectory = remoteDirectory;
    }

    public boolean isSaveDeletedFiles() {
        return saveDeletedFiles;
    }

    public void setSaveDeletedFiles(boolean saveDeletedFiles) {
        this.saveDeletedFiles = saveDeletedFiles;
    }

    public boolean isSaveOverwrittenFile() {
        return saveOverwrittenFile;
    }

    public void setSaveOverwrittenFile(boolean saveOverwrittenFile) {
        this.saveOverwrittenFile = saveOverwrittenFile;
    }

    public String getRecycleBin() {
        return recycleBin;
    }

    public void setRecycleBin(String recycleBin) {
        this.recycleBin = recycleBin;
    }

    public boolean isTurnOnSynchronisation() {
        return turnOnSynchronisation;
    }

    public void setTurnOnSynchronisation(boolean turnOnSynchronisation) {
        this.turnOnSynchronisation = turnOnSynchronisation;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public String getSyncRemoteDirectory() {
        return syncRemoteDirectory;
    }

    public void setSyncRemoteDirectory(String syncRemoteDirectory) {
        this.syncRemoteDirectory = syncRemoteDirectory;
    }

    public String getSyncLocalDirectory() {
        return syncLocalDirectory;
    }

    public void setSyncLocalDirectory(String syncLocalDirectory) {
        this.syncLocalDirectory = syncLocalDirectory;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public boolean isMobileNetwork() {
        return mobileNetwork;
    }

    public void setMobileNetwork(boolean mobileNetwork) {
        this.mobileNetwork = mobileNetwork;
    }

    public String getDays() {
        return days;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
