package me.bolanleonifade.remoteconnection;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Ester on 18/06/2015.
 */
public class SFTPSession extends Session implements Parcelable {
    private String SSHKey;
    private String SSHPass;

    public static final Parcelable.Creator<SFTPSession> CREATOR = new Parcelable.Creator<SFTPSession>() {

        @Override
        public SFTPSession[] newArray(int size) {

            return new SFTPSession[size];
        }

        @Override
        public SFTPSession createFromParcel(Parcel source) {
            SFTPSession sftpSession = new SFTPSession();
            sftpSession.setId(source.readInt());
            sftpSession.setHost(source.readString());
            sftpSession.setUser(source.readString());
            sftpSession.setPort(source.readInt());
            sftpSession.setPassword(source.readString());

            sftpSession.setRemoteDirectory(source.readString());
            sftpSession.setLocalDirectory(source.readString());
            sftpSession.setSaveDeletedFiles((Boolean) source.readValue(null));
            sftpSession.setSaveOverwrittenFile((Boolean) source.readValue(null));
            sftpSession.setRecycleBin(source.readString());

            sftpSession.setSSHKey(source.readString());
            sftpSession.setSSHPass(source.readString());

            sftpSession.setTurnOnSynchronisation((Boolean) source.readValue(null));
            sftpSession.setMaster(source.readString());
            sftpSession.setSyncLocalDirectory(source.readString());
            sftpSession.setSyncRemoteDirectory(source.readString());
            sftpSession.setRecursive((Boolean) source.readValue(null));
            sftpSession.setMobileNetwork((Boolean) source.readValue(null));

            sftpSession.setDays(source.readString());
            sftpSession.setTime(source.readString());
            return sftpSession;
        }

    };
    public String getSSHPass() {
        return SSHPass;
    }

    public void setSSHPass(String SSHPass) {
        this.SSHPass = SSHPass;
    }

    public String getSSHKey() {
        return SSHKey;
    }

    public void setSSHKey(String SSHKey) {
        this.SSHKey = SSHKey;
    }

    @Override
    public int describeContents() {
        return 2;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(getId());
        dest.writeString(getHost());
        dest.writeString(getUser());
        dest.writeInt(getPort());
        dest.writeString(getPassword());

        dest.writeString(getRemoteDirectory());
        dest.writeString(getLocalDirectory());
        dest.writeValue(isSaveDeletedFiles());
        dest.writeValue(isSaveOverwrittenFile());
        dest.writeString(getRecycleBin());
        
        dest.writeString(getSSHKey());
        dest.writeString(getSSHPass());

        dest.writeValue(isTurnOnSynchronisation());
        dest.writeString(getMaster());
        dest.writeString(getSyncLocalDirectory());
        dest.writeString(getSyncRemoteDirectory());
        dest.writeValue(isRecursive());
        dest.writeValue(isMobileNetwork());
        dest.writeString(getDays());
        dest.writeString(getTime());
    }

    @Override
    public String toString() {
        return "SFTP://" + getUser() + "@" + getHost() ;
    }
}
