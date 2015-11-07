package me.bolanleonifade.remoteconnection;

import org.apache.commons.vfs2.FileObject;

import java.util.Comparator;

public class NameComparator implements Comparator<FileObject> {
    @Override
    public int compare(FileObject o1, FileObject o2) {
        return o1.getName().getBaseName().toLowerCase().compareTo(o2.getName().getBaseName().toLowerCase());
    }
}