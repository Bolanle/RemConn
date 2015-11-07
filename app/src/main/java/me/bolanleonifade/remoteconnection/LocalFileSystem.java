package me.bolanleonifade.remoteconnection;

import android.os.Handler;
import android.os.Looper;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Ester on 25/06/2015.
 */
public class LocalFileSystem {
    StandardFileSystemManager manager;
    String prevDir;
    Handler handler;
    FileSystemActivity activity;
    private boolean cancelled;

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public LocalFileSystem() throws FileSystemException {
        handler = new Handler(Looper.getMainLooper());
        manager = new StandardFileSystemManager();
        manager.init();
    }

    public LocalFileSystem(FileSystemActivity activity) throws FileSystemException {
        handler = new Handler(Looper.getMainLooper());
        manager = new StandardFileSystemManager();
        manager.init();
        this.activity = activity;
    }

    public List<FileObject> getFilesInDir(String path, int sortType) throws FileSystemException {
        if (path == "Device Storage")
            path = "/storage/emulated/0/";
        FileObject file = manager.resolveFile(path);

        prevDir = file.getParent().getName().getPath();
        List<List<FileObject>> files = new ArrayList<List<FileObject>>();
        files.add(Utils.convertToArrayList(file.getChildren()));

        List<FileObject> toSort = files.get(0);
        this.sort(toSort, sortType);
        return toSort;
    }

    public String getPrevDir() {
        return prevDir;
    }

    private void sort(List<FileObject> files, int sortType) {
        if (sortType == SessionContract.SORT_ASC)
            Collections.sort(files, new NameComparator());
    }

    public boolean delete(String path) throws FileSystemException {
        FileObject toDelete = manager.resolveFile(path);
        boolean deleted = toDelete.delete();
        return deleted;
    }

    public void rename(String path, String newPath) throws FileSystemException {
        FileObject toReplace = manager.resolveFile(path);
        FileObject replacement = manager.resolveFile(newPath);
        replacement.copyFrom(toReplace, Selectors.SELECT_ALL);
    }

    public void createNewFolder(String path, String name) throws FileSystemException {

        FileObject newFolder = manager.resolveFile(path + "/" + name);
        newFolder.createFolder();

    }

    public ArrayList<FileObject> search(String term) throws FileSystemException {
        FileObject rootDirectory = manager.resolveFile("/storage/emulated/0/");
        rootDirectory.refresh();
        return searchFolder(rootDirectory, term);
    }

    private ArrayList<FileObject> searchFolder(FileObject folder, String term) throws FileSystemException {
        ArrayList<FileObject> files = new ArrayList<>();
        try {
            FileObject[] children = folder.getChildren();

            for (FileObject child : children) {
                if(!cancelled) {
                    try {

                        if (child.getName().getBaseName().contains(term)) {

                            final FileObject toSend = child;
                            files.add(child);

                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //activity.animateTopBarPath = false;
                                    activity.updateSearch(toSend);
                                }
                            }, 200);
                        }


                        if (child.getType() == FileType.FOLDER) {
                            files.addAll(searchFolder(child, term));
                        }
                    } catch (FileSystemException e) {
                        //suppress
                    }
                }
            }
        } catch (FileSystemException e) {
            if (!(e.getCode().equals("vfs.provider/invalid-descendent-name.error")
                    || e.getCode().equals("vfs.provider/list-children-not-folder.error")))
                throw e;

        }
        return files;
    }
}
