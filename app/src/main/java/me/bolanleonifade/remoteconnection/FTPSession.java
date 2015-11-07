package me.bolanleonifade.remoteconnection;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Ester on 18/06/2015.
 */
public class FTPSession extends Session implements Parcelable{

    public static final Parcelable.Creator<FTPSession> CREATOR = new Parcelable.Creator<FTPSession>() {

        @Override
        public FTPSession[] newArray(int size) {

            return new FTPSession[size];
        }

        @Override
        public FTPSession createFromParcel(Parcel source) {

            FTPSession ftpSession = new FTPSession();
            ftpSession.setId(source.readInt());
            ftpSession.setHost(source.readString());
            ftpSession.setUser(source.readString());
            ftpSession.setPort(source.readInt());
            ftpSession.setPassword(source.readString());

            ftpSession.setRemoteDirectory(source.readString());
            ftpSession.setLocalDirectory(source.readString());
            ftpSession.setSaveDeletedFiles((Boolean) source.readValue(null));
            ftpSession.setSaveOverwrittenFile((Boolean) source.readValue(null));
            ftpSession.setRecycleBin(source.readString());

            ftpSession.setTurnOnSynchronisation((Boolean) source.readValue(null));
            ftpSession.setMaster(source.readString());
            ftpSession.setSyncLocalDirectory(source.readString());
            ftpSession.setSyncRemoteDirectory(source.readString());
            ftpSession.setRecursive((Boolean) source.readValue(null));
            ftpSession.setMobileNetwork((Boolean) source.readValue(null));

            ftpSession.setDays(source.readString());
            ftpSession.setTime(source.readString());

            return ftpSession;
        }

    };

    @Override
    public int describeContents() {
        return 1;
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
        return "FTP://" + getUser() + "@" +getHost() ;
    }
}
