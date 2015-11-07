package me.bolanleonifade.remoteconnection;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

import java.util.Comparator;

public class LastModifiedComparator implements Comparator<FileObject> {
    @Override
    public int compare(FileObject o1, FileObject o2) {
        try {
            if (o1.getContent().getLastModifiedTime() > o2.getContent().getLastModifiedTime())
                return 1;
            else if (o1.getContent().getLastModifiedTime() < o2.getContent().getLastModifiedTime()) {
                return -1;
            }
        } catch (FileSystemException e) {
            e.printStackTrace();
        }
        return 0;
    }
}