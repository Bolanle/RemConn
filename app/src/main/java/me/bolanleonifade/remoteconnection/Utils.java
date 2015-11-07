package me.bolanleonifade.remoteconnection;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.provider.ftp.FtpFileObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ester on 18/06/2015.
 */
public class Utils {

    public static List<String> stringToList(String sourceString, String pattern) {
        List<String> destList = new ArrayList<String>();
        String[] _array = sourceString.split(pattern);


        for (int i = 0; i < _array.length; i++) {
            destList.add(_array[i]);
        }

        return destList;
    }

    public static String listToString(List<String> sourceList, String pattern) {
        String destString = "";

        for (int i = 0; i < sourceList.size(); i++) {
            destString += sourceList.get(i) + pattern;
            destString += i != sourceList.size() - 1 ? ", " : "";
        }

        return destString;
    }

    public static ArrayList<FileObject> convertToArrayList(FileObject[] source){
        ArrayList<FileObject> dest = new ArrayList<>();

        for (FileObject child: source) {

            dest.add(child);
        }
        return dest;
    }

    public static ArrayList<FtpFileObject> convertToFTPArrayList(FileObject[] source){
        ArrayList<FtpFileObject> dest = new ArrayList<>();

        for (FileObject child: source) {

            dest.add((FtpFileObject) child);
        }
        return dest;
    }
}
