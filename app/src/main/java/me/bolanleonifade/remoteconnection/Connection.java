package me.bolanleonifade.remoteconnection;

import android.os.Handler;
import android.os.Looper;

import com.jcraft.jsch.UserInfo;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by Ester on 21/06/2015.
 */
public class Connection {

    private SFTPSession sftpSession;
    private FTPSession ftpSession;
    private int type;
    private FileSystemActivity activity;

    private StandardFileSystemManager manager;
    private String uri;
    private FileObject rootDirectory;
    private FileObject remoteDirectory;
    private String prevDir;
    private FileSystemOptions opts;

    private Handler handler;
    private boolean cancelled = false;

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public Connection(SFTPSession session, int type) {
        this.sftpSession = session;
        this.type = type;
    }

    public Connection(FTPSession session, int type) {
        this.ftpSession = session;
        this.type = type;
    }


    public void connect() throws FileSystemException {
        //Looper.prepare();
        handler = new Handler(Looper.getMainLooper());
        manager = new StandardFileSystemManager();
        manager.init();
        opts = new FileSystemOptions();

        if (type == SessionContract.FTP) {
            FtpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, true);
            FtpFileSystemConfigBuilder.getInstance().setPassiveMode(opts, true);
            FtpFileSystemConfigBuilder.getInstance().setSoTimeout(opts, null);
            FtpFileSystemConfigBuilder.getInstance().setDataTimeout(opts, null);


            String ftpUri = "ftp://" + ftpSession.getUser();
            ftpUri += !ftpSession.getPassword().isEmpty() ? ":" + ftpSession.getPassword()
                    + "@" : "@";
            ftpUri += ftpSession.getHost();
            ftpUri += ":" + ftpSession.getPort();
            uri = ftpUri;
            rootDirectory = manager.resolveFile(ftpUri + "/", opts);
            remoteDirectory = manager.resolveFile(uri + ftpSession.getRemoteDirectory(), opts);

        } else if (type == SessionContract.SFTP) {
            SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(
                    opts, "no");
            SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, true);
            // SftpFileSystemConfigBuilder.getInstance().setTimeout(opts, sftpSession.getTimeOut());

            SftpFileSystemConfigBuilder.getInstance().setPreferredAuthentications(opts, "password");
            if (sftpSession.getSSHKey() != null && !sftpSession.getSSHKey().isEmpty())
                SftpFileSystemConfigBuilder.getInstance().setIdentities(opts, new File[]{new File(sftpSession.getSSHKey())});
            if (sftpSession.getSSHPass() != null && !sftpSession.getSSHPass().isEmpty())
                SftpFileSystemConfigBuilder.getInstance().setUserInfo(opts, new SftpPassphraseUserInfo(sftpSession.getSSHPass()));

            //Create the SFTP URI using the host name, userid, password,  remote path and file name
            String sftpUri = "sftp://" + sftpSession.getUser();
            sftpUri += !sftpSession.getPassword().isEmpty() ? ":" + sftpSession.getPassword()
                    + "@" : "@";
            sftpUri += sftpSession.getHost();
            sftpUri += ":" + sftpSession.getPort();
            uri = sftpUri;
            rootDirectory = manager.resolveFile(sftpUri + "/", opts);

            remoteDirectory = manager.resolveFile(uri + sftpSession.getRemoteDirectory(), opts);

        }
    }

    public ArrayList<FileObject> getFilesInDir(String subdir, int sortType) throws FileSystemException, InterruptedException {
        String modifiedUri = uri.endsWith("/") && subdir.length() == 1 && subdir.contains("/") ? uri : uri + subdir;

        if (type == SessionContract.SFTP) {
            if (!subdir.equals("/"))
                remoteDirectory = manager.resolveFile(modifiedUri, opts);
            else {
                rootDirectory.refresh();
                remoteDirectory = rootDirectory;
            }
            if (remoteDirectory.getParent() != null)
                prevDir = remoteDirectory.getParent().getName().getPath();
            ArrayList<ArrayList<FileObject>> files = new ArrayList<ArrayList<FileObject>>();
            files.add(Utils.convertToArrayList(remoteDirectory.getChildren()));

            ArrayList<FileObject> toSort = files.get(0);
            this.sort(toSort, sortType);
            return toSort;
        } else {
            if (!subdir.equals("/"))
                remoteDirectory = manager.resolveFile(modifiedUri, opts);
            else {
                rootDirectory.refresh();
                remoteDirectory = rootDirectory;
            }
            if (remoteDirectory.getParent() != null)
                prevDir = remoteDirectory.getParent().getName().getPath();

            ArrayList<ArrayList<FileObject>> files = new ArrayList<ArrayList<FileObject>>();
            files.add(Utils.convertToArrayList(remoteDirectory.getChildren()));

            ArrayList<FileObject> toSort = files.get(0);
            this.sort(toSort, sortType);
            if (!toSort.isEmpty())
                toSort.get(0).getType();
            return toSort;
            //return null;
        }
    }

    public String getPrevDir() {
        return prevDir;
    }

    public int download(String localUri, String remoteUri) throws FileSystemException {
        int count = 0;

        if (type == SessionContract.SFTP) {
            File file = new File(localUri);
            FileObject localFile = manager.resolveFile(file.getAbsolutePath());
            FileObject remoteFile = manager.resolveFile(uri + remoteUri, opts);
            try {
                if (!sftpSession.getRecycleBin().isEmpty() && sftpSession.isSaveOverwrittenFile()) {
                    FileObject recycleBin = manager.resolveFile("/storage/emulated/0/"
                            + sftpSession.getRecycleBin() + "/" + "LOCAL-"
                            + remoteFile.getName().getBaseName() + "-" +
                            DateFormat.getInstance().format(new Date(System.currentTimeMillis())));
                    recycleBin.copyFrom(localFile, Selectors.SELECT_SELF);
                }

                localFile.copyFrom(remoteFile, Selectors.SELECT_SELF);
                count += 1;
                if (localFile.getType() == FileType.FOLDER)
                    for (FileObject remoteChild : remoteFile.getChildren()) {

                        String localRelChildPath = localFile.getName().getPath();
                        FileObject localChild = manager.resolveFile(localRelChildPath
                                + "/" + remoteChild.getName().getBaseName());
                        localChild.copyFrom(remoteChild, Selectors.SELECT_SELF);
                        count += 1;
                        if (remoteChild.getType() == FileType.FOLDER) {
                            count += download(localChild.getName().getPath(), remoteChild.getName().getPath());
                        }
                    }
            } catch (FileSystemException e) {
                if (!e.getCode().equals("vfs.provider/find-files.error")
                        && !e.getCode().equals("vfs.provider/invalid-descendent-name.error")) {
                    throw e;
                }
            }

        } else if (type == SessionContract.FTP) {
            File file = new File(localUri);
            FileObject localFile = manager.resolveFile(file.getAbsolutePath());
            FileObject remoteFile = manager.resolveFile(uri + remoteUri, opts);

            try {
                if (!ftpSession.getRecycleBin().isEmpty() && ftpSession.isSaveOverwrittenFile()) {
                    FileObject recycleBin = manager.resolveFile("/storage/emulated/0/"
                            + ftpSession.getRecycleBin() + "/" + "LOCAL-"
                            + remoteFile.getName().getBaseName() + "-" +
                            DateFormat.getInstance().format(new Date(System.currentTimeMillis())));
                    recycleBin.copyFrom(localFile, Selectors.SELECT_SELF);
                }
                localFile.copyFrom(remoteFile, Selectors.SELECT_SELF);
                count += 1;
                if (localFile.getType() == FileType.FOLDER)
                    for (FileObject remoteChild : remoteFile.getChildren()) {

                        String localRelChildPath = localFile.getName().getPath();
                        FileObject localChild = manager.resolveFile(localRelChildPath
                                + "/" + remoteChild.getName().getBaseName());
                        localChild.copyFrom(remoteChild, Selectors.SELECT_SELF);
                        count += 1;
                        if (remoteChild.getType() == FileType.FOLDER) {
                            count += download(localChild.getName().getPath(), remoteChild.getName().getPath());
                        }
                    }
            } catch (FileSystemException e) {
                if (!e.getCode().equals("vfs.provider/find-files.error")) {
                    throw e;
                }
            }

        }
        return count;
    }

    public int upload(String localUri, String remoteUri) throws FileSystemException {
        int count = 0;
        if (!localUri.equals("..") && !remoteUri.equals(".."))
            if (type == SessionContract.SFTP) {
                File file = new File(localUri);
                FileObject localFile = manager.resolveFile(file.getAbsolutePath());
                FileObject remoteFile = manager.resolveFile(uri + remoteUri, opts);
                try {
                    if (!sftpSession.getRecycleBin().isEmpty() && sftpSession.isSaveOverwrittenFile()) {
                        FileObject recycleBin = manager.resolveFile("/storage/emulated/0/"
                                + sftpSession.getRecycleBin() + "/" + "REMOTE-"
                                + remoteFile.getName().getBaseName() + "-" +
                                DateFormat.getInstance().format(new Date(System.currentTimeMillis())), opts);
                        recycleBin.copyFrom(remoteFile, Selectors.SELECT_SELF);
                    }

                    remoteFile.copyFrom(localFile, Selectors.SELECT_SELF);
                    count += 1;
                    if (localFile.getType() == FileType.FOLDER)
                        for (FileObject localChild : localFile.getChildren()) {

                            String remoteRelChildPath = remoteFile.getName().getPath();
                            FileObject remoteChild = manager.resolveFile(uri + remoteRelChildPath +
                                    "/" + localChild.getName().getBaseName(), opts);
                            remoteChild.copyFrom(localChild, Selectors.SELECT_SELF);
                            count += 1;
                            if (localChild.getType() == FileType.FOLDER) {
                                count += upload(localChild.getName().getPath(), remoteChild.getName().getPath());
                            }
                        }
                } catch (FileSystemException e) {
                    if (!e.getCode().equals("vfs.provider/find-files.error")
                            && !e.getCode().equals("vfs.provider/invalid-descendent-name.error")) {
                    throw e;
                }
            }
            } else if (type == SessionContract.FTP) {
                File file = new File(localUri);
                FileObject localFile = manager.resolveFile(file.getAbsolutePath());
                FileObject remoteFile = manager.resolveFile(uri + remoteUri, opts);
                try {
                    if (!ftpSession.getRecycleBin().isEmpty() && ftpSession.isSaveOverwrittenFile()) {
                        FileObject recycleBin = manager.resolveFile("/storage/emulated/0/"
                                + ftpSession.getRecycleBin() + "/" + "REMOTE-"
                                + remoteFile.getName().getBaseName() + "-" +
                                DateFormat.getInstance().format(new Date(System.currentTimeMillis())), opts);
                        recycleBin.copyFrom(remoteFile, Selectors.SELECT_SELF);
                    }

                    remoteFile.copyFrom(localFile, Selectors.SELECT_SELF);
                    count += 1;
                    if (localFile.getType() == FileType.FOLDER)
                        for (FileObject localChild : localFile.getChildren()) {

                            String remoteRelChildPath = remoteFile.getName().getPath();
                            FileObject remoteChild = manager.resolveFile(uri + remoteRelChildPath
                                    + "/" + localChild.getName().getBaseName(), opts);
                            remoteChild.copyFrom(localChild, Selectors.SELECT_SELF);
                            count += 1;
                            if (localChild.getType() == FileType.FOLDER) {
                                count += upload(localChild.getName().getPath(), remoteChild.getName().getPath());
                            }
                        }
                } catch (FileSystemException e) {
                    if (!e.getCode().equals("vfs.provider/find-files.error")
                            && !e.getCode().equals("vfs.provider/invalid-descendent-name.error")) {
                        throw e;
                    }
                }
            }
        return count;
    }

    public boolean delete(String remoteUri) throws FileSystemException {
        FileObject remoteFile;
        if (type == SessionContract.SFTP)
            remoteFile = manager.resolveFile(uri + remoteUri, opts);
        else
            remoteFile = manager.resolveFile(uri + remoteUri, opts);

        if (remoteFile.exists()) {
            remoteFile.delete(Selectors.SELECT_ALL);
            return true;
        }
        return false;
    }

    public void rename(String path, String newPath) throws FileSystemException {
        FileObject replacement = null, toReplace = null;
        if (type == SessionContract.SFTP) {
            toReplace = manager.resolveFile(uri + path, opts);
            replacement = manager.resolveFile(uri + newPath, opts);

        } else if (type == SessionContract.FTP) {
            toReplace = manager.resolveFile(uri + path, opts);
            replacement = manager.resolveFile(uri + newPath, opts);
        }
        toReplace.moveTo(replacement);
    }

    public void createNewFolder(String path, String name) throws FileSystemException {
        FileObject newFolder;
        if (type == SessionContract.SFTP) {
            newFolder = manager.resolveFile(uri + path + "/" + name + "/", opts);
        } else {
            newFolder = manager.resolveFile(uri + path + "/" + name + "/", opts);
        }
        newFolder.createFolder();
    }

    public void setActivity(FileSystemActivity activity) {
        this.activity = activity;
    }

    public ArrayList<FileObject> search(String term) throws FileSystemException {

        rootDirectory.refresh();
        return searchFolder(rootDirectory, term);
    }

    public List<String> sync() throws FileSystemException {

        List<FileObject> filesTransferred = new ArrayList<>();
        FileObject remoteDir;
        FileObject localDir;
        List<String> paths = new ArrayList<>();
        if (type == SessionContract.SFTP) {
            remoteDir = manager.resolveFile(uri + sftpSession.getSyncRemoteDirectory(), opts);
            localDir = manager.resolveFile(sftpSession.getSyncLocalDirectory());

            filesTransferred = getDifferences(remoteDir, localDir, sftpSession.isRecursive(), sftpSession.getMaster());
            if (sftpSession.getMaster().equals("Local")) {
                for (FileObject toTransfer : filesTransferred) {
                    try {
                        String relPath = toTransfer.getName().getPath().replace(localDir.getName().getPath(), "");
                        FileObject remoteObject = manager.resolveFile(uri + remoteDir.getName().getPath() + "/" + relPath, opts);
                        remoteObject.copyFrom(toTransfer, Selectors.SELECT_SELF);
                        paths.add(toTransfer.getName().getPath().replace("/storage/emulated/0/", ""));
                    } catch (FileSystemException e) {
                        if (!e.getCode().equals("vfs.provider/find-files.error")
                                && !e.getCode().equals("vfs.provider/invalid-descendent-name.error")) {
                            throw e;
                        }
                    }
                }
            } else {
                for (FileObject toTransfer : filesTransferred) {
                    try {
                        String relPath = toTransfer.getName().getPath().replace(remoteDir.getName().getPath(), "");
                        FileObject localObject = manager.resolveFile(localDir.getName().getPath() + "/" + relPath);
                        localObject.copyFrom(toTransfer, Selectors.SELECT_SELF);
                        paths.add(toTransfer.getName().getPath());
                    } catch (FileSystemException e) {
                        if (!e.getCode().equals("vfs.provider/find-files.error")
                                && !e.getCode().equals("vfs.provider/invalid-descendent-name.error")) {
                            throw e;
                        }
                    }
                }
            }
        } else if (type == SessionContract.FTP) {
            remoteDir = manager.resolveFile(uri + ftpSession.getSyncRemoteDirectory(), opts);
            localDir = manager.resolveFile(ftpSession.getSyncLocalDirectory());
            filesTransferred = getDifferences(remoteDir, localDir, ftpSession.isRecursive(), ftpSession.getMaster());
            if (ftpSession.getMaster().equals("Local")) {
                for (FileObject toTransfer : filesTransferred) {
                    try {
                        String relPath = toTransfer.getName().getPath().replace(localDir.getName().getPath(), "");
                        FileObject remoteObject = manager.resolveFile(uri + remoteDir.getName().getPath() + "/" + relPath, opts);
                        remoteObject.copyFrom(toTransfer, Selectors.SELECT_SELF);
                        paths.add(toTransfer.getName().getPath().replace("/storage/emulated/0/", ""));
                    } catch (FileSystemException e) {
                        if (!e.getCode().equals("vfs.provider/find-files.error")
                                && !e.getCode().equals("vfs.provider/invalid-descendent-name.error")) {
                            throw e;
                        }
                    }
                }
            } else {
                for (FileObject toTransfer : filesTransferred) {
                    try {
                        String relPath = toTransfer.getName().getPath().replace(remoteDir.getName().getPath(), "");
                        FileObject localObject = manager.resolveFile(localDir.getName().getPath() + "/" + relPath);
                        localObject.copyFrom(toTransfer, Selectors.SELECT_SELF);
                        paths.add(toTransfer.getName().getPath());
                    } catch (FileSystemException e) {
                        if (!e.getCode().equals("vfs.provider/find-files.error")
                                && !e.getCode().equals("vfs.provider/invalid-descendent-name.error")) {
                            throw e;
                        }
                    }
                }
            }
        }
        //Clean up branch


        return paths;
    }

    private ArrayList<FileObject> getDifferences(FileObject remote, FileObject local, boolean recursive, String master) throws FileSystemException {
        ArrayList<FileObject> getDifferences, localFiles, remoteFiles;
        FileObject[] remoteFileObjects = remote.getChildren();
        FileObject[] localFileObjects = local.getChildren();
        if (!recursive) {
            localFiles = Utils.convertToArrayList(localFileObjects);
            remoteFiles = Utils.convertToArrayList(remoteFileObjects);
            if (master.equals("Local")) {
                getDifferences = getChangedFiles(localFiles, remoteFiles);
            } else {
                getDifferences = getChangedFiles(remoteFiles, localFiles);
            }
        } else {
            getDifferences = getRecursiveDifferences(remote, local, master);
        }
        return getDifferences;
    }

    private ArrayList<FileObject> getRecursiveDifferences(FileObject remote, FileObject local, String master) throws FileSystemException {
        ArrayList<FileObject> getDifferences = new ArrayList<>();

        if (master.equals("Local")) {
            remote.createFolder();
            FileObject[] localFileObjects = local.getChildren();
            for (FileObject file : localFileObjects) {
                FileObject remoteSubItem, localSubItem = null;
                remoteSubItem = resolveFileRemote(uri +
                        remote.getName().getPath() + "/" + file.getName().getBaseName());
                localSubItem = resolveFileLocal(
                        local.getName().getPath() + "/" + file.getName().getBaseName());


                if (file.getType() == FileType.FILE) {
                    if (!remoteSubItem.exists()) {
                        getDifferences.add(file);
                    } else {
                        if (remoteSubItem.getContent().getLastModifiedTime() < file.getContent().getLastModifiedTime())
                            getDifferences.add(file);
                    }
                } else if (file.getType() == FileType.FOLDER) {
                    if (!remoteSubItem.exists()) {
                        remoteSubItem.createFolder();
                        getDifferences.add(file);
                    }
                    FileObject[] remoteSubFolderFileObjects = remoteSubItem.getChildren();
                    FileObject[] localSubFolderFileObjects = localSubItem.getChildren();

                    getDifferences.addAll(getChangedFiles(
                            Utils.convertToArrayList(localSubFolderFileObjects),
                            Utils.convertToArrayList(remoteSubFolderFileObjects)));
                    getDifferences.addAll(getRecursiveDifferences(remoteSubItem, localSubItem, master));
                }
            }
        } else {
            local.createFolder();
            FileObject[] remoteFileObjects = remote.getChildren();

            for (FileObject file : remoteFileObjects) {
                FileObject remoteSubItem, localSubItem = null;

                remoteSubItem = resolveFileRemote(uri +
                        remote.getName().getPath() + "/" + file.getName().getBaseName());
                localSubItem = resolveFileLocal(
                        local.getName().getPath() + "/" + file.getName().getBaseName());

                if (file.getType() == FileType.FILE) {
                    if (!localSubItem.exists()) {
                        getDifferences.add(file);
                    } else {
                        if (localSubItem.getContent().getLastModifiedTime() < file.getContent().getLastModifiedTime())
                            getDifferences.add(file);
                    }
                }
                if (file.getType() == FileType.FOLDER) {
                    if (!localSubItem.exists()) {
                        localSubItem.createFolder();
                        getDifferences.add(file);
                    }
                    FileObject[] remoteSubFolderFileObjects = remoteSubItem.getChildren();
                    FileObject[] localSubFolderFileObjects = localSubItem.getChildren();
                    getDifferences.addAll(getChangedFiles(
                            Utils.convertToArrayList(remoteSubFolderFileObjects),
                            Utils.convertToArrayList(localSubFolderFileObjects)));
                    getDifferences.addAll(getRecursiveDifferences(remoteSubItem, localSubItem, master));
                }
            }
        }

        return getDifferences;
    }

    private FileObject resolveFileRemote(String path) throws FileSystemException {

        return manager.resolveFile(path, opts);
    }

    private FileObject resolveFileLocal(String path) throws FileSystemException {

        return manager.resolveFile(path);
    }

    private ArrayList<FileObject> searchFolder(FileObject folder, String term) throws FileSystemException {


        ArrayList<FileObject> files = new ArrayList<>();
        try {
            FileObject[] children = folder.getChildren();

            for (FileObject child : children) {
                if (!cancelled) {
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

    private void sort(List<FileObject> files, int sortType) {
        if (sortType == SessionContract.SORT_ASC)
            Collections.sort(files, new NameComparator());
        else if (sortType == SessionContract.SORT_DESC) {
            Collections.sort(files, new NameComparator());
            Collections.reverse(files);
        } else {
            Collections.sort(files, new LastModifiedComparator());
        }
    }

    private ArrayList<FileObject> getChangedFiles(ArrayList<FileObject> master, ArrayList<FileObject> branch) throws FileSystemException {
        ArrayList<FileObject> changedFiles = new ArrayList<>();
        ArrayList<FileObject> branchCopy = new ArrayList<>();
        branchCopy.addAll(branch);
        ArrayList<FileObject> originalFiles = new ArrayList<>();
        for (FileObject child : master) {
            FileObject correspondingObject = getMatchingFile(branch, child);
            if (correspondingObject != null)
                if (correspondingObject.getContent().getLastModifiedTime() < child.getContent().getLastModifiedTime()) {
                    changedFiles.add(child);
                    originalFiles.add(correspondingObject);
                } else {
                }
            else
                changedFiles.add(child);
        }
        branchCopy.removeAll(originalFiles);
        if(!changedFiles.isEmpty() )
            for (FileObject toDelete : branchCopy) {
            if (type == SessionContract.SFTP && !sftpSession.getRecycleBin().isEmpty() && sftpSession.isSaveOverwrittenFile()) {
                FileObject recycleBin = manager.resolveFile("/storage/emulated/0/"
                        + sftpSession.getRecycleBin() + "/"
                        + toDelete.getName().getBaseName() +
                        DateFormat.getInstance().format(new Date(System.currentTimeMillis())));
                recycleBin.copyFrom(toDelete, Selectors.SELECT_SELF);
            } else if (type == SessionContract.FTP && !ftpSession.getRecycleBin().isEmpty() && ftpSession.isSaveOverwrittenFile()) {
                FileObject recycleBin = manager.resolveFile("/storage/emulated/0/"
                        + ftpSession.getRecycleBin() + "/"
                        + toDelete.getName().getBaseName() +
                        DateFormat.getInstance().format(new Date(System.currentTimeMillis())));
                recycleBin.copyFrom(toDelete, Selectors.SELECT_SELF);
            }
            toDelete.delete();
        }
        return changedFiles;
    }

    private FileObject getMatchingFile(ArrayList<FileObject> branch, FileObject parentFile) {
        for (FileObject child : branch) {
            if (child.getName().getBaseName().equals(parentFile.getName().getBaseName()))
                return child;
        }
        return null;
    }

    public void close() {

        manager.close();
    }

    public static class SftpPassphraseUserInfo implements UserInfo {

        private String passphrase = null;

        public SftpPassphraseUserInfo(final String pp) {
            passphrase = pp;
        }

        public String getPassphrase() {
            return passphrase;
        }

        public String getPassword() {
            return null;
        }

        public boolean promptPassphrase(String arg0) {
            return true;
        }

        public boolean promptPassword(String arg0) {
            return false;
        }

        public void showMessage(String message) {

        }

        public boolean promptYesNo(String str) {
            return true;
        }


    }

}
