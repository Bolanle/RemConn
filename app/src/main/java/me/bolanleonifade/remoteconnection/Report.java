package me.bolanleonifade.remoteconnection;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Ester on 29/06/2015.
 */
public class Report implements Parcelable {
    private int id;
    private String status;
    private String host;
    private String user;
    private int type;



    private String localDirectory;
    private String remoteDirectory;

    private String timeStamp;
    private String filesUploaded;
    private String filesDownloaded;
    private String error;

    public static final Parcelable.Creator<Report> CREATOR = new Parcelable.Creator<Report>() {

        @Override
        public Report[] newArray(int size) {

            return new Report[size];
        }

        @Override
        public Report createFromParcel(Parcel source) {

            Report report = new Report();
            report.setId(source.readInt());
            report.setHost(source.readString());
            report.setUser(source.readString());
            report.setType(source.readInt());
            report.setLocalDirectory(source.readString());
            report.setRemoteDirectory(source.readString());
            report.setStatus(source.readString());
            report.setTimeStamp(source.readString());
            report.setFilesUploaded(source.readString());
            report.setFilesDownloaded(source.readString());
            report.setError(source.readString());

            return report;
        }

    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getLocalDirectory() {
        return localDirectory;
    }

    public void setLocalDirectory(String localDirectory) {
        this.localDirectory = localDirectory;
    }

    public String getRemoteDirectory() {
        return remoteDirectory;
    }

    public void setRemoteDirectory(String remoteDirectory) {
        this.remoteDirectory = remoteDirectory;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getFilesUploaded() {
        return filesUploaded;
    }

    public void setFilesUploaded(String filesUploaded) {
        this.filesUploaded = filesUploaded;
    }

    public String getFilesDownloaded() {
        return filesDownloaded;
    }

    public void setFilesDownloaded(String filesDownloaded) {
        this.filesDownloaded = filesDownloaded;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(getId());
        dest.writeString(getHost());
        dest.writeString(getUser());
        dest.writeInt(getType());
        dest.writeString(getLocalDirectory());
        dest.writeString(getRemoteDirectory());
        dest.writeString(getStatus());
        dest.writeString(getTimeStamp());
        dest.writeString(getFilesUploaded());
        dest.writeString(getFilesDownloaded());
        dest.writeString(getError());
    }
}
