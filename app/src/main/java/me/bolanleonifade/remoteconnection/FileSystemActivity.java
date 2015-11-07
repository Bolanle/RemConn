package me.bolanleonifade.remoteconnection;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TaskStackBuilder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileSystemActivity extends AppCompatActivity {


    public static String ACTION_DISPLAY_NOTIF = "me.bolanleonifade.remoteconnection.file_system.notification";

    private SFTPSession sftpSession;
    private FTPSession ftpSession;

    private ImageButton fileSystemGoBack;

    private int type;
    private Handler handler;
    private PerformLongOperation longOperation;

    public static String CURRENT_REMOTE_DIRECTORY_STRING = "CUR_REM_DIR";
    public static String CURRENT_LOCAL_DIRECTORY_STRING = "CUR_LOC_DIR";
    public static String VIEW_REMOTE_STRING = "VIEW_REMOTE";
    public static String SESSION_STRING = "SESSION";
    public static String NOTIFICATION_ID_STRING = "NOTIFICATION_ID";
    public static String REMOTE_SORT_TYPE_STRING = "REMOTE_SORT_TYPE";
    public static String LOCAL_SORT_TYPE_STRING = "LOCAL_SORT_TYPE";
    public static String TYPE_STRING = "TYPE";

    private boolean VIEW_REMOTE;
    private Connection connection;
    private LocalFileSystem localFileSystem;

    private int OPERATION_CONNECT = 1;
    private int OPERATION_UPLOAD = 2;
    private int OPERATION_DOWNLOAD = 3;//Operation IDs for long performing tasks
    private int OPERATION_GET_FILES = 4;
    private int OPERATION_OPEN = 5;
    private int OPERATION_DELETE = 6;
    private int OPERATION_RENAME = 7;
    private int OPERATION_CREATE_FOLDER = 8;
    private int OPERATION_SEARCH = 9;
    private int OPERATION_CLOSE = 10;

    private String currentRemoteDirectory;
    private String previousRemoteDirectory;
    private String currentLocalDirectory;
    private String previousLocalDirectory;
    private ArrayList<FileObject> currentDirectory;

    private ProgressBar progressBar;
    private LinearLayout fileDisplay;
    private LinearLayout pathView;
    private Menu menu;
    private HorizontalScrollView topBarScrollView;

    private TextView rootView;

    private String tempOpenFileLocation;
    private String tempOpenFile;
    private File tempDownloadedFile;
    private String downloadDirectory;
    private boolean showProgressDialog;
    private boolean resetSearch;
    boolean finishedConnecting = false;
    String operateOnFile;
    int filesTransferred = 0;
    int operation_id;
    String operatedPath;
    private int remoteSortType;
    private ArrayList<FileObject> toOperateOn;
    boolean searching = false;
    boolean animateTopBarPath;
    private int localSortType;
    ProgressDialog prog;

    int notificationID;

    NotificationManager mNotificationManager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_system);

        Toolbar toolbar = (Toolbar) findViewById(R.id.file_system_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        resetSearch = true;
        notificationID = 1;
        animateTopBarPath = true;
        handler = new Handler();


        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        try {
            localFileSystem = new LocalFileSystem(this);
        } catch (FileSystemException e) {
            displayMessage(R.string.error_getting_file_system, R.string.error_getting_file_system_mssg, true);
        }
        tempOpenFileLocation = "/storage/emulated/0/Android/data/me.bolanleonifade/temp";
        showProgressDialog = false;
        SharedPreferences settings = getSharedPreferences(SessionContract.PREFERENCES, Context.MODE_PRIVATE);
        downloadDirectory = settings.getString(SessionContract.PREFERRED_LOCAL_DIR,
                "/storage/emulated/0/Download/"); //If not set, then use default downloads folder

        if (savedInstanceState != null) {
            VIEW_REMOTE = savedInstanceState.getBoolean(VIEW_REMOTE_STRING);
            currentRemoteDirectory = savedInstanceState.getString(CURRENT_REMOTE_DIRECTORY_STRING);
            currentLocalDirectory = savedInstanceState.getString(CURRENT_LOCAL_DIRECTORY_STRING);
            remoteSortType = savedInstanceState.getInt(REMOTE_SORT_TYPE_STRING);
            localSortType = savedInstanceState.getInt(LOCAL_SORT_TYPE_STRING);

            type = savedInstanceState.getInt(TYPE_STRING, SessionContract.NONE);
            if (type == SessionContract.FTP) {
                ftpSession = savedInstanceState.getParcelable(SESSION_STRING);
                ((TextView) findViewById(R.id.file_system_toolbar_title)).setText(ftpSession.getHost());
                connection = new Connection(ftpSession, type);
            } else if (type == SessionContract.SFTP) {
                sftpSession = savedInstanceState.getParcelable(SESSION_STRING);
                ((TextView) findViewById(R.id.file_system_toolbar_title)).setText(sftpSession.getHost());
                connection = new Connection(sftpSession, type);
            }
        } else {
            //Initialise variables
            Intent promptIntent = getIntent();
            Bundle b = promptIntent.getBundleExtra(SessionContract.CURRENT_SESSION);

            type = b.getInt(TYPE_STRING, SessionContract.NONE);
            VIEW_REMOTE = b.getBoolean(VIEW_REMOTE_STRING, true);
            remoteSortType = b.getInt(REMOTE_SORT_TYPE_STRING, SessionContract.SORT_ASC);
            localSortType = b.getInt(LOCAL_SORT_TYPE_STRING, SessionContract.SORT_ASC);
            mNotificationManager.cancel(b.getInt(NOTIFICATION_ID_STRING, notificationID));
            if (type == SessionContract.FTP) {
                b.setClassLoader(FTPSession.class.getClassLoader());
                ftpSession = b.getParcelable(SESSION_STRING);
                ((TextView) findViewById(R.id.file_system_toolbar_title)).setText(ftpSession.getHost());
                connection = new Connection(ftpSession, type);
                currentRemoteDirectory = b.getString(CURRENT_REMOTE_DIRECTORY_STRING, ftpSession.getRemoteDirectory());
                currentLocalDirectory = b.getString(CURRENT_LOCAL_DIRECTORY_STRING, ftpSession.getLocalDirectory());
                //Ignore preferences if download folder is provided
                if (ftpSession.getLocalDirectory() != null && !ftpSession.getLocalDirectory().isEmpty())
                    downloadDirectory = ftpSession.getLocalDirectory();
            } else if (type == SessionContract.SFTP) {
                b.setClassLoader(SFTPSession.class.getClassLoader());
                sftpSession = b.getParcelable(SESSION_STRING);
                ((TextView) findViewById(R.id.file_system_toolbar_title)).setText(sftpSession.getHost());
                connection = new Connection(sftpSession, type);
                currentRemoteDirectory = b.getString(CURRENT_REMOTE_DIRECTORY_STRING, sftpSession.getRemoteDirectory());
                currentLocalDirectory = b.getString(CURRENT_LOCAL_DIRECTORY_STRING, sftpSession.getLocalDirectory());
                if (sftpSession.getLocalDirectory() != null && !sftpSession.getLocalDirectory().isEmpty())
                    downloadDirectory = sftpSession.getLocalDirectory();
            }
        }
        connection.setActivity(this);
        //Assign directories finally if general preferences have been set

        if (currentRemoteDirectory.equals("/"))
            currentRemoteDirectory = settings.getString(SessionContract.PREFERRED_REMOTE_DIR,
                    currentRemoteDirectory);
        if (currentLocalDirectory.equals("/storage/emulated/0") || currentLocalDirectory.equals("/storage/emulated/0/"))
            currentLocalDirectory = settings.getString(SessionContract.PREFERRED_LOCAL_DIR,
                    currentLocalDirectory);


        progressBar = (ProgressBar) findViewById(R.id.file_system_progress_bar);
        fileDisplay = (LinearLayout) findViewById(R.id.file_system_files_list);
        pathView = (LinearLayout) findViewById(R.id.file_system_path_view);
        topBarScrollView = (HorizontalScrollView) findViewById(R.id.file_system_top_bar_scroll);

        fileSystemGoBack = (ImageButton) findViewById(R.id.file_system_go_back);
        fileSystemGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitFileSystem(v);
            }
        });
        longOperation = new PerformLongOperation();
        longOperation.execute(OPERATION_CONNECT);
        if (!VIEW_REMOTE) {
            animateTopBarPath = true;
            displayLocalFiles();
        }
    }

    private void displayFiles() {
        //Stop any operation in the previous view;
        progressBar.setVisibility(View.GONE);
        fileDisplay.setVisibility(View.VISIBLE);
        pathView.setVisibility(View.VISIBLE);


        fileDisplay.removeAllViews();
        {
            setTopBarPath();
            ImageItemView fileItemDisplay = new ImageItemView(this, false);
            fileItemDisplay.getSideImage().setImageResource(R.drawable.hidden_folder_icon);
            fileItemDisplay.getMainTextView().setText("..");
            fileItemDisplay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    moveUpDirectories(false);
                }
            });
            fileDisplay.addView(fileItemDisplay);
        }
        try {
            for (final FileObject file : currentDirectory) {
                {
                    SharedPreferences settings = getSharedPreferences(SessionContract.PREFERENCES, Context.MODE_PRIVATE);
                    boolean showHiddenFiles = settings.getBoolean(SessionContract.PREFERRED_HIDDEN_FILE_DISPLAY,
                            true);
                    if (showHiddenFiles || (!file.getName().getBaseName().startsWith(".")) && !showHiddenFiles ) {
                        final ImageItemView fileItemDisplay = new ImageItemView(this, false);

                        if (file.getType() == FileType.FILE && !file.getName().getBaseName().startsWith(".")) {
                            fileItemDisplay.getSideImage().setImageResource(R.drawable.file_icon);
                        } else if (file.getType() == FileType.IMAGINARY || file.getType() == FileType.FILE && file.getName().getBaseName().startsWith(".")) {
                            fileItemDisplay.getSideImage().setImageResource(R.drawable.hidden_file_icon);
                        } else if (file.getType() == FileType.FOLDER && !file.getName().getBaseName().startsWith(".")) {
                            fileItemDisplay.getSideImage().setImageResource(R.drawable.folder_icon);
                        } else {
                            fileItemDisplay.getSideImage().setImageResource(R.drawable.hidden_folder_icon);
                        }
                        fileItemDisplay.getMainTextView().setText(file.getName().getBaseName());
                        fileItemDisplay.setOnClickListener(new ImageItemViewOnClickListener());
                        fileItemDisplay.setOnLongClickListener(new ImageItemViewOnLongClickListener());
                        fileItemDisplay.setFileObject(file);
                        fileDisplay.addView(fileItemDisplay);
                    }
                }
            }
        } catch (FileSystemException e) {
            displayMessage(R.string.error_getting_file_system, R.string.error_getting_file_system_mssg, true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        MenuItem switchToRemote = menu.getItem(1);
        MenuItem switchToHome = menu.getItem(0);
        MenuItem upload = menu.getItem(2);
        MenuItem download = menu.getItem(3);

        if (operation_id == OPERATION_SEARCH) {
            progressBar.setVisibility(View.VISIBLE);
            fileDisplay.setVisibility(View.GONE);
            pathView.setVisibility(View.GONE);
            if (VIEW_REMOTE) {
                connection.setCancelled(true);
            } else {
                localFileSystem.setCancelled(true);
            }
            longOperation.cancel(true);
            resetSearch = true;
        } else {
            connection.setCancelled(false);
            connection.setCancelled(false);
        }
        if (item.getItemId() == R.id.switch_to_home) {
            VIEW_REMOTE = false;
            item.setVisible(false);
            download.setVisible(false);
            switchToRemote.setVisible(true);
            upload.setVisible(true);
            if (operation_id == OPERATION_CONNECT || operation_id == OPERATION_GET_FILES)
                longOperation.cancel(true);
            animateTopBarPath = false;
            displayLocalFiles();
        } else if (item.getItemId() == R.id.switch_to_remote) {
            VIEW_REMOTE = true;
            progressBar.setVisibility(View.VISIBLE);
            fileDisplay.setVisibility(View.GONE);
            pathView.setVisibility(View.GONE);

            item.setVisible(false);
            upload.setVisible(false);
            switchToHome.setVisible(true);
            download.setVisible(true);
            if (operation_id == OPERATION_CONNECT || operation_id == OPERATION_GET_FILES)
                longOperation.cancel(true);
            animateTopBarPath = false;
            longOperation = new PerformLongOperation();
            longOperation.execute(OPERATION_GET_FILES);

        } else if (item.getItemId() == R.id.upload) {
            toOperateOn = getUserSelectedItems();
            if (toOperateOn.isEmpty()) {
                displayMessage(R.string.error_selected, R.string.error_selected_mssg, false);
            }else {
                NotificationCompat.Builder notificationBuilder =
                        new NotificationCompat.Builder(FileSystemActivity.this)
                                .setSmallIcon(R.drawable.ic_stat)
                                .setColor(getResources().getColor(R.color.icon_color))
                                .setContentTitle("RemConn Upload")
                                .setTicker("Upload started");
                notificationID++;
                mNotificationManager.notify(notificationID, notificationBuilder.build());

                animateTopBarPath = false;
                longOperation = new PerformLongOperation();
                longOperation.execute(OPERATION_UPLOAD);
            }

        } else if (item.getItemId() == R.id.download) {
            toOperateOn = getUserSelectedItems();
            if (toOperateOn.isEmpty()) {
                displayMessage(R.string.error_selected, R.string.error_selected_mssg, false);
            }else {
                NotificationCompat.Builder notificationBuilder =
                        new NotificationCompat.Builder(FileSystemActivity.this)
                                .setSmallIcon(R.drawable.ic_stat)
                                .setColor(getResources().getColor(R.color.icon_color))
                                .setContentTitle("RemConn Download")
                                .setTicker("Download started");
                notificationID++;
                mNotificationManager.notify(notificationID, notificationBuilder.build());
                animateTopBarPath = false;
                longOperation = new PerformLongOperation();
                longOperation.execute(OPERATION_DOWNLOAD);
            }

        } else if (item.getItemId() == R.id.refresh) {
            if (VIEW_REMOTE) {
                animateTopBarPath = false;
                longOperation = new PerformLongOperation();
                longOperation.execute(OPERATION_GET_FILES);
            } else {
                animateTopBarPath = false;
                displayLocalFiles();
            }
        } else if (item.getItemId() == R.id.delete) {


            toOperateOn = getUserSelectedItems();
            if (toOperateOn.isEmpty()) {
                displayMessage(R.string.error_selected, R.string.error_selected_mssg, false);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(FileSystemActivity.this);
                builder.setTitle(R.string.warning_delete_files)
                        .setMessage(R.string.warning_delete_files_mssg)
                        .setCancelable(true)
                        .setNegativeButton("CANCEL", null)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        fileDisplay.setVisibility(View.GONE);
                                        pathView.setVisibility(View.GONE);
                                        progressBar.setVisibility(View.VISIBLE);

                                        longOperation = new PerformLongOperation();
                                        longOperation.execute(OPERATION_DELETE);
                                    }
                                }).show();
            }

        } else if (item.getItemId() == R.id.rename) {
            toOperateOn = getUserSelectedItems();
            if (toOperateOn.isEmpty()) {
                displayMessage(R.string.error_selected, R.string.error_selected_mssg, false);
            } else if (toOperateOn.size() > 1) {
                displayMessage(R.string.error_multiple_selected_files, R.string.error_multiple_selected_files_mssg, false);
            } else {
                final EditText input = new EditText(this);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                if (VIEW_REMOTE) {
                    builder.setTitle("Input Required")
                            .setMessage("Enter New Filename:")
                            .setView(input)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    operateOnFile = input.getText().toString().trim();
                                    longOperation = new PerformLongOperation();
                                    longOperation.execute(OPERATION_RENAME);

                                }
                            }).setNegativeButton("Cancel", null).show();
                } else {
                    builder.setTitle("Input Required")
                            .setMessage("Enter New File Name:")
                            .setView(input)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    operateOnFile = input.getText().toString();
                                    for (FileObject toRename : toOperateOn) {
                                        try {
                                            connection.rename(toRename.getName().getPath(), toRename.getName().getParent().getPath() + operateOnFile);
                                        } catch (FileSystemException e) {
                                            displayMessage(R.string.error_cannot_rename, R.string.error_cannot_rename_mssg, false);
                                        }
                                    }
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    }).show();
                }
            }
        } else if (item.getItemId() == R.id.create_folder) {
            final EditText input = new EditText(this);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            if (VIEW_REMOTE) {
                builder.setTitle("Input Required")
                        .setMessage("Enter Folder Name:")
                        .setView(input)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                operateOnFile = input.getText().toString().trim();
                                longOperation = new PerformLongOperation();
                                longOperation.execute(OPERATION_CREATE_FOLDER);

                            }
                        }).setNegativeButton("Cancel", null).show();
            } else {
                builder.setTitle("Input Required")
                        .setMessage("Enter Folder Name:")
                        .setView(input)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                operateOnFile = input.getText().toString();
                                try {
                                    localFileSystem.createNewFolder(currentLocalDirectory, operateOnFile);
                                } catch (FileSystemException e) {
                                    displayMessage(R.string.error_cannot_rename, R.string.error_cannot_rename_mssg, false);
                                }
                                displayLocalFiles();
                            }
                        }).setNegativeButton("Cancel", null).show();
            }
        } else if (item.getItemId() == R.id.search) {
            final EditText input = new EditText(this);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            resetSearch = false;
            if (VIEW_REMOTE) {
                builder.setTitle("Input Required")
                        .setMessage("Enter Search Term:")
                        .setView(input)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                operateOnFile = input.getText().toString().trim();
                                showProgressDialog = true;
                                longOperation = new PerformLongOperation();
                                longOperation.execute(OPERATION_SEARCH);

                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).show();
            } else {
                builder.setTitle("Input Required")
                        .setMessage("Enter Folder Name:")
                        .setView(input)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                operateOnFile = input.getText().toString().trim();
                                showProgressDialog = true;
                                longOperation = new PerformLongOperation();
                                longOperation.execute(OPERATION_SEARCH);

                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).show();
            }

        } else if (item.getItemId() == R.id.details) {
            toOperateOn = getUserSelectedItems();
            if (toOperateOn.size() >= 1) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                LinearLayout view = new LinearLayout(this);
                view.setOrientation(LinearLayout.VERTICAL);
                view.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);

                LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                final float scale = getResources().getDisplayMetrics().density;

                headerParams.setMargins((int) (10 * scale + 0.5f), (int) (10 * scale + 0.5f), 0, (int) (5 * scale + 0.5f));
                contentParams.setMargins((int) (5 * scale + 0.5f), (int) (10 * scale + 0.5f), 0, (int) (5 * scale + 0.5f));
                if (toOperateOn.size() == 1) {


                    LinearLayout nameLinearLayout = new LinearLayout(this);
                    nameLinearLayout.setLayoutParams(containerParams);
                    nameLinearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_END);
                    nameLinearLayout.setOrientation(LinearLayout.VERTICAL);

                    TextView nameTextView = new TextView(this);
                    nameTextView.setText("Name");
                    nameTextView.setTextColor(getResources().getColor(R.color.primary_dark_material_light));
                    nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                    nameTextView.setLayoutParams(headerParams);

                    TextView nameValueTextView = new TextView(this);
                    nameValueTextView.setText(toOperateOn.get(0).getName().getBaseName());
                    nameValueTextView.setTextColor(getResources().getColor(R.color.primary_dark_material_light));
                    nameValueTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                    nameValueTextView.setLayoutParams(contentParams);

                    nameLinearLayout.addView(nameTextView);
                    nameLinearLayout.addView(nameValueTextView);

                    view.addView(nameLinearLayout);

                    LinearLayout sizeLinearLayout = new LinearLayout(this);
                    sizeLinearLayout.setOrientation(LinearLayout.VERTICAL);
                    sizeLinearLayout.setLayoutParams(containerParams);
                    sizeLinearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_END);

                    TextView sizeTextView = new TextView(this);
                    sizeTextView.setText("File Size");
                    sizeTextView.setTextColor(getResources().getColor(R.color.primary_dark_material_light));
                    sizeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                    sizeTextView.setLayoutParams(headerParams);

                    String byteSize = "";
                    try {
                        long bytes = 0;
                        if (toOperateOn.get(0).getType() == FileType.FILE) {
                            bytes = getSize(toOperateOn.get(0));
                        }
                        DecimalFormat formatter = new DecimalFormat("#,###");
                        byteSize = String.valueOf((int) bytes / 1000) + "KB" + " (" + formatter.format(bytes) + " bytes)";
                    } catch (FileSystemException e) {
                        e.printStackTrace();

                    }

                    TextView sizeValueTextView = new TextView(this);
                    sizeValueTextView.setText(byteSize);
                    sizeValueTextView.setTextColor(getResources().getColor(R.color.primary_dark_material_light));
                    sizeValueTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                    sizeValueTextView.setLayoutParams(contentParams);

                    sizeLinearLayout.addView(sizeTextView);
                    sizeLinearLayout.addView(sizeValueTextView);

                    view.addView(sizeLinearLayout);

                    LinearLayout lastModifiedLinearLayout = new LinearLayout(this);
                    lastModifiedLinearLayout.setOrientation(LinearLayout.VERTICAL);
                    lastModifiedLinearLayout.setLayoutParams(containerParams);
                    lastModifiedLinearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_END);


                    TextView lastModifiedTextView = new TextView(this);
                    lastModifiedTextView.setText("Last Modified");
                    lastModifiedTextView.setTextColor(getResources().getColor(R.color.primary_dark_material_light));
                    lastModifiedTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                    lastModifiedTextView.setLayoutParams(headerParams);

                    String lastModified = "";
                    try {
                        long time = toOperateOn.get(0).getContent().getLastModifiedTime();
                        lastModified = DateFormat.getInstance().format(new Date(time));
                    } catch (FileSystemException e) {
                        lastModified = "unknown";
                    }
                    TextView lastModifiedValueTextView = new TextView(this);
                    lastModifiedValueTextView.setText(lastModified);
                    lastModifiedValueTextView.setTextColor(getResources().getColor(R.color.primary_dark_material_light));
                    lastModifiedValueTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                    lastModifiedValueTextView.setLayoutParams(contentParams);

                    lastModifiedLinearLayout.addView(lastModifiedTextView);
                    lastModifiedLinearLayout.addView(lastModifiedValueTextView);

                    view.addView(lastModifiedLinearLayout);

                    LinearLayout pathLinearLayout = new LinearLayout(this);
                    pathLinearLayout.setOrientation(LinearLayout.VERTICAL);
                    pathLinearLayout.setLayoutParams(containerParams);
                    pathLinearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_END);

                    TextView pathTextView = new TextView(this);
                    pathTextView.setText("Path");
                    pathTextView.setTextColor(getResources().getColor(R.color.primary_dark_material_light));
                    pathTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                    pathTextView.setLayoutParams(headerParams);

                    TextView pathValueTextView = new TextView(this);
                    pathValueTextView.setText(toOperateOn.get(0).getName().getPath());
                    pathValueTextView.setTextColor(getResources().getColor(R.color.primary_dark_material_light));
                    pathValueTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                    pathValueTextView.setLayoutParams(contentParams);

                    pathLinearLayout.addView(pathTextView);
                    pathLinearLayout.addView(pathValueTextView);

                    view.addView(pathLinearLayout);
                } else {
                    LinearLayout sizeLinearLayout = new LinearLayout(this);
                    sizeLinearLayout.setOrientation(LinearLayout.VERTICAL);
                    sizeLinearLayout.setLayoutParams(containerParams);
                    sizeLinearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_END);

                    TextView sizeTextView = new TextView(this);
                    sizeTextView.setText("Total File Size");
                    sizeTextView.setTextColor(getResources().getColor(R.color.primary_dark_material_light));
                    sizeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                    sizeTextView.setLayoutParams(headerParams);

                    String byteSize = "";
                    try {
                        long bytes = 0;
                        for (FileObject file : toOperateOn) {
                            if (file.getType() == FileType.FILE) {
                                bytes += getSize(file);
                            } else {

                            }

                        }
                        DecimalFormat formatter = new DecimalFormat("#,###");
                        byteSize = String.valueOf((int) bytes / 1000) + "KB" + " (" + formatter.format(bytes) + " bytes)";
                    } catch (FileSystemException e) {
                        e.printStackTrace();
                    }

                    TextView sizeValueTextView = new TextView(this);
                    sizeValueTextView.setText(byteSize);
                    sizeValueTextView.setTextColor(getResources().getColor(R.color.primary_dark_material_light));
                    sizeValueTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                    sizeValueTextView.setLayoutParams(contentParams);

                    sizeLinearLayout.addView(sizeTextView);
                    sizeLinearLayout.addView(sizeValueTextView);

                    view.addView(sizeLinearLayout);

                    LinearLayout itemsLinearLayout = new LinearLayout(this);
                    itemsLinearLayout.setOrientation(LinearLayout.VERTICAL);
                    itemsLinearLayout.setLayoutParams(containerParams);
                    itemsLinearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_END);

                    TextView itemsTextView = new TextView(this);
                    itemsTextView.setText("Contains");
                    itemsTextView.setTextColor(getResources().getColor(R.color.primary_dark_material_light));
                    itemsTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                    itemsTextView.setLayoutParams(headerParams);

                    TextView itemsValueTextView = new TextView(this);
                    itemsValueTextView.setText(String.valueOf(toOperateOn.size()));
                    itemsValueTextView.setTextColor(getResources().getColor(R.color.primary_dark_material_light));
                    itemsValueTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                    itemsValueTextView.setLayoutParams(contentParams);

                    itemsLinearLayout.addView(itemsTextView);
                    itemsLinearLayout.addView(itemsValueTextView);

                    view.addView(itemsLinearLayout);
                }

                builder.setTitle("Details")
                        .setView(view)
                        .setPositiveButton("Ok", null)
                        .show();
            }
        } else if (item.getItemId() == R.id.sort) {
            final float scale = getResources().getDisplayMetrics().density;
            final RadioGroup group = new RadioGroup(this);


            RadioButton ascendingRadioButton = new RadioButton(this);
            ascendingRadioButton.setText("Ascending");


            group.addView(ascendingRadioButton);


            RadioButton descendingRadioButton = new RadioButton(this);
            descendingRadioButton.setText("Descending");


            group.addView(descendingRadioButton);


            RadioButton lastModifiedRadioButton = new RadioButton(this);
            lastModifiedRadioButton.setText("Last Modified");

            group.addView(lastModifiedRadioButton);

            if ((remoteSortType == SessionContract.SORT_ASC && VIEW_REMOTE)
                    || (localSortType == SessionContract.SORT_ASC && !VIEW_REMOTE))
                group.check(ascendingRadioButton.getId());
            if ((remoteSortType == SessionContract.SORT_DESC && VIEW_REMOTE)
                    || (localSortType == SessionContract.SORT_DESC && !VIEW_REMOTE))
                group.check(descendingRadioButton.getId());
            if ((remoteSortType == SessionContract.SORT_MODIFIED && VIEW_REMOTE) ||
                    (localSortType == SessionContract.SORT_MODIFIED && !VIEW_REMOTE))
                group.check(lastModifiedRadioButton.getId());

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            if (VIEW_REMOTE) {
                builder.setTitle("Sort Method")
                        .setView(group)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                int radioButtonID = group.getCheckedRadioButtonId();
                                View radioButton = group.findViewById(radioButtonID);
                                remoteSortType = group.indexOfChild(radioButton) + 1;
                                animateTopBarPath = false;
                                longOperation = new PerformLongOperation();
                                longOperation.execute(OPERATION_GET_FILES);

                            }
                        }).setNegativeButton("Cancel", null).show();
            } else {
                builder.setTitle("Sort Method")
                        .setView(group)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                int radioButtonID = group.getCheckedRadioButtonId();
                                View radioButton = group.findViewById(radioButtonID);
                                localSortType = group.indexOfChild(radioButton) + 1;
                                animateTopBarPath = false;
                                displayLocalFiles();

                            }
                        }).setNegativeButton("Cancel", null).show();
            }
        }
        //
        int childCount = fileDisplay.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (i != 0) {
                ImageItemView fileItem = (ImageItemView) fileDisplay.getChildAt(i);
                fileItem.getCheckItem().setChecked(false);
                fileItem.getCheckItem().setVisibility(View.GONE);
                fileItem.setOnClickListener(new ImageItemViewOnClickListener());
                fileItem.setOnLongClickListener(new ImageItemViewOnLongClickListener());
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_file_system, menu);
        this.menu = menu;

        MenuItem switchToRemote = menu.getItem(1);
        MenuItem switchToHome = menu.getItem(0);
        MenuItem upload = menu.getItem(2);
        MenuItem download = menu.getItem(3);


        if (VIEW_REMOTE) {
            switchToHome.setVisible(false);
            upload.setVisible(false);
            switchToHome.setVisible(true);
            download.setVisible(true);
        } else {
            switchToHome.setVisible(false);
            download.setVisible(false);
            switchToRemote.setVisible(true);
            upload.setVisible(true);
        }

        return super.onCreateOptionsMenu(menu);
    }

    public void exitFileSystem(View view) {
        longOperation = new PerformLongOperation();
        longOperation.execute(OPERATION_CLOSE);
        longOperation.cancel(true);

        finish();
    }


    private class PerformLongOperation extends AsyncTask<Integer, Integer, Boolean> {


        @Override
        protected Boolean doInBackground(Integer... params) {
            operation_id = params[0];

            if (operation_id == OPERATION_CONNECT) {
                try {
                    connection.connect();
                    finishedConnecting = true;
                } catch (FileSystemException e) {
                    e.printStackTrace();
                    return false;
                }
            } else if (operation_id == OPERATION_GET_FILES) {
                try {
                    currentDirectory = connection.getFilesInDir(currentRemoteDirectory, remoteSortType);
                    previousRemoteDirectory = connection.getPrevDir();
                } catch (FileSystemException | InterruptedException e) {
                    e.printStackTrace();
                    return false;
                }
            } else if (operation_id == OPERATION_UPLOAD) {
                filesTransferred = 0;
                mNotificationManager.cancel(notificationID);
                for (FileObject toUploadFile : toOperateOn) {
                    try {
                        filesTransferred += connection.upload(toUploadFile.getName().getPath().toString(), currentRemoteDirectory + "/" + toUploadFile.getName().getBaseName()); // Refresh when done
                    } catch (FileSystemException e) {
                        return false;
                    }
                }

            } else if (operation_id == OPERATION_DOWNLOAD) {
                filesTransferred = 0;
                mNotificationManager.cancel(notificationID);
                if (VIEW_REMOTE) {
                    for (FileObject toDownloadFile : toOperateOn) {
                        try {

                            operatedPath = toDownloadFile.getName().getPath();
                            filesTransferred += connection.download(downloadDirectory +
                                            toDownloadFile.getName().getBaseName(),
                                    toDownloadFile.getName().getPath());

                        } catch (FileSystemException e) {
                            return false;
                        }
                    }
                } else {

                }

            } else if (operation_id == OPERATION_OPEN) {
                try {
                    showProgressDialog = false;
                    connection.download(tempOpenFileLocation + "/" + tempOpenFile, currentRemoteDirectory + "/" + tempOpenFile);
                    tempDownloadedFile = new File(tempOpenFileLocation + "/" + tempOpenFile);

                } catch (FileSystemException e) {
                    return false;
                }

            } else if (operation_id == OPERATION_DELETE) {
                try {
                    if (VIEW_REMOTE)
                        for (FileObject toDelete : toOperateOn) {
                            connection.delete(toDelete.getName().getPath());
                        }
                    else {
                        for (FileObject toDeleteFile : toOperateOn) {
                            localFileSystem.delete(toDeleteFile.getName().getPath());
                        }
                    }
                } catch (FileSystemException e) {
                    return false;
                }

            } else if (operation_id == OPERATION_RENAME) {
                try {
                    for (FileObject toRename : toOperateOn) {
                        connection.rename(toRename.getName().getPath(), toRename.getName().getParent().getPath() + "/" + operateOnFile);
                    }
                } catch (FileSystemException e) {
                    return false;
                }

            } else if (operation_id == OPERATION_CREATE_FOLDER) {
                try {
                    connection.createNewFolder(currentRemoteDirectory, operateOnFile);
                } catch (FileSystemException e) {
                    return false;
                }

            } else if (operation_id == OPERATION_SEARCH) {
                showProgressDialog = false;
                searching = true;
                currentDirectory.clear();
                if (VIEW_REMOTE) {
                    try {

                        currentDirectory = connection.search(operateOnFile);
                    } catch (FileSystemException e) {
                        return false;
                    }
                } else {
                    try {
                        currentDirectory = localFileSystem.search(operateOnFile);
                    } catch (FileSystemException e) {
                        return false;
                    }
                }
            } else if (operation_id == OPERATION_CLOSE) {
                connection.close();
            }

            return true;
        }

        protected void onPreExecute() {
            if (showProgressDialog) {
                prog = new ProgressDialog(FileSystemActivity.this);
                //prog.setTitle("Processing");
                prog.setMessage("Please wait");
                prog.setIndeterminate(true);
                prog.setCancelable(true);
                prog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        longOperation.cancel(true);
                    }
                });
                prog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                prog.show();
            }
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (operation_id == OPERATION_CONNECT) {
                        if (VIEW_REMOTE)
                            if (result) {
                                longOperation = new PerformLongOperation();
                                longOperation.execute(OPERATION_GET_FILES);
                            } else {
                                displayMessage(R.string.error_connecting, R.string.error_getting_file_system_mssg, true);
                            }
                    } else if (operation_id == OPERATION_GET_FILES) {
                        if (!isCancelled())
                            if (result) {
                                displayFiles();
                            } else {
                                displayMessage(R.string.error_getting_file_system, R.string.error_getting_file_system_mssg, true);
                            }
                    } else if (operation_id == OPERATION_OPEN) {
                        if (result) {
                            try {
                                prog.dismiss();
                                openFile(FileSystemActivity.this, tempDownloadedFile);
                            } catch (IOException e) {
                                displayMessage(R.string.error_opening_files, R.string.error_opening_files_mssg, true);
                            }
                        } else {
                            displayMessage(R.string.error_opening_files, R.string.error_opening_files_mssg, true);
                        }
                    } else if (operation_id == OPERATION_DOWNLOAD) {

                        if (result) {

                            Intent intent = new Intent(FileSystemActivity.this.getApplicationContext(), FileSystemActivity.class);
                            Bundle b = new Bundle();
                            notificationID++;
                            b.putString(CURRENT_REMOTE_DIRECTORY_STRING, currentRemoteDirectory);
                            b.putString(CURRENT_LOCAL_DIRECTORY_STRING, downloadDirectory);
                            b.putBoolean(VIEW_REMOTE_STRING, !VIEW_REMOTE);
                            b.putInt(REMOTE_SORT_TYPE_STRING, remoteSortType);
                            b.putInt(LOCAL_SORT_TYPE_STRING, localSortType);
                            b.putInt(NOTIFICATION_ID_STRING, notificationID);
                            if (type == SessionContract.FTP) {
                                ftpSession.setLocalDirectory(operatedPath);
                                ftpSession.setRemoteDirectory(currentRemoteDirectory);
                                b.putParcelable(SESSION_STRING, ftpSession);
                                b.putInt(SessionContract.TYPE, SessionContract.FTP);
                            } else if (type == SessionContract.SFTP) {
                                sftpSession.setLocalDirectory(operatedPath);
                                sftpSession.setRemoteDirectory(currentRemoteDirectory);
                                b.putParcelable(SESSION_STRING, sftpSession);
                                b.putInt(SessionContract.TYPE, SessionContract.SFTP);
                            }
                            intent.putExtra(SessionContract.CURRENT_SESSION, b);
                            String contentInfo;
                            if (filesTransferred != 1)
                                contentInfo = filesTransferred + " files downloaded to local Downloads folder";
                            else
                                contentInfo = filesTransferred + " file downloaded to local Downloads folder";
                            TaskStackBuilder stackBuilder = TaskStackBuilder.create(FileSystemActivity.this);
                            stackBuilder.addParentStack(SessionsActivity.class);

                            stackBuilder.addNextIntent(intent);
                            PendingIntent fileSysIntent =
                                    stackBuilder.getPendingIntent(
                                            notificationID,
                                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT
                                    );

                            NotificationCompat.Builder notificationBuilder =
                                    new NotificationCompat.Builder(FileSystemActivity.this)
                                            .setSmallIcon(R.drawable.ic_launcher)
                                            .setColor(getResources().getColor(R.color.icon_color))
                                            .setContentTitle("RemConn Download")
                                            .setContentInfo(contentInfo)
                                            .setContentIntent(fileSysIntent);
                            notificationBuilder.setVibrate(new long[]{1000, 1000});


                            mNotificationManager.notify(notificationID, notificationBuilder.build());
                            //notificationID++;
                        } else {
                            displayMessage(R.string.error_transfer, R.string.error_transfer_mssg, false);
                        }
                        //Toast.makeText(FileSystemActivity.this, "titleBuild", Toast.LENGTH_SHORT);
                    } else if (operation_id == OPERATION_UPLOAD) {
                        if (result) {
                            Intent intent = new Intent(FileSystemActivity.this.getApplicationContext(), FileSystemActivity.class);
                            Bundle b = new Bundle();
                            notificationID++;
                            b.putString(CURRENT_REMOTE_DIRECTORY_STRING, currentRemoteDirectory);
                            b.putString(CURRENT_LOCAL_DIRECTORY_STRING, currentLocalDirectory);
                            b.putBoolean(VIEW_REMOTE_STRING, !VIEW_REMOTE);
                            b.putInt(REMOTE_SORT_TYPE_STRING, remoteSortType);
                            b.putInt(LOCAL_SORT_TYPE_STRING, localSortType);
                            b.putInt(NOTIFICATION_ID_STRING, notificationID);
                            if (type == SessionContract.FTP) {
                                ftpSession.setLocalDirectory(operatedPath);
                                ftpSession.setRemoteDirectory(currentRemoteDirectory);
                                b.putParcelable(SESSION_STRING, ftpSession);
                                b.putInt(TYPE_STRING, SessionContract.FTP);
                            } else if (type == SessionContract.SFTP) {
                                sftpSession.setLocalDirectory(operatedPath);
                                sftpSession.setRemoteDirectory(currentRemoteDirectory);
                                b.putParcelable(SESSION_STRING, sftpSession);
                                b.putInt(TYPE_STRING, SessionContract.SFTP);
                            }
                            intent.putExtra(SessionContract.CURRENT_SESSION, b);

                            String contentInfo;
                            if (filesTransferred != 1)
                                contentInfo = filesTransferred + " files uploaded to remote folder " + currentRemoteDirectory;
                            else
                                contentInfo = filesTransferred + " file uploaded to remote folder " + currentRemoteDirectory;
                            TaskStackBuilder stackBuilder = TaskStackBuilder.create(FileSystemActivity.this);
                            stackBuilder.addParentStack(SessionsActivity.class);

                            stackBuilder.addNextIntent(intent);
                            PendingIntent fileSysIntent =
                                    stackBuilder.getPendingIntent(
                                            notificationID,
                                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT
                                    );

                            NotificationCompat.Builder notificationBuilder =
                                    new NotificationCompat.Builder(FileSystemActivity.this)
                                            .setSmallIcon(R.drawable.ic_launcher)
                                            .setColor(getResources().getColor(R.color.icon_color))
                                            .setContentIntent(fileSysIntent)
                                            .setContentTitle("RemConn Upload")
                                            .setContentInfo(contentInfo);
                            notificationBuilder.setVibrate(new long[]{1000, 1000});

                            mNotificationManager.notify(notificationID, notificationBuilder.build());
                            //notificationID++;
                        } else {

                            displayMessage(R.string.error_transfer, R.string.error_transfer_mssg, false);

                        }
                        //Toast.makeText(FileSystemActivity.this, "titleBuild", Toast.LENGTH_SHORT);
                    } else if (operation_id == OPERATION_DELETE) {
                        if (result) {
                            animateTopBarPath = false;
                            if (VIEW_REMOTE) {

                                longOperation = new PerformLongOperation();
                                longOperation.execute(OPERATION_GET_FILES);
                            } else {
                                displayLocalFiles();
                            }
                        } else {
                            displayMessage(R.string.error_deleting_files, R.string.error_deleting_files_mssg, false);
                        }
                    } else if (operation_id == OPERATION_RENAME) {
                        if (result) {
                            animateTopBarPath = false;
                            longOperation = new PerformLongOperation();
                            longOperation.execute(OPERATION_GET_FILES);
                        } else {
                            displayMessage(R.string.error_multiple_selected_files, R.string.error_multiple_selected_files_mssg, false);
                        }
                    } else if (operation_id == OPERATION_CREATE_FOLDER) {
                        if (result) {
                            animateTopBarPath = false;
                            longOperation = new PerformLongOperation();
                            longOperation.execute(OPERATION_GET_FILES);
                        } else {
                            displayMessage(R.string.error_create_folder, R.string.error_create_folder_mssg, false);
                        }
                    } else if (operation_id == OPERATION_SEARCH) {
                        prog.dismiss();
                        if (result) {
                            displaySearchResults(false);
                        } else {
                            displayMessage(R.string.error_getting_file_system, R.string.error_getting_file_system_mssg, false);
                        }
                    }
                }
            });
        }
    }

    private class ImageItemViewOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            try {
                ImageItemView display = (ImageItemView) v;
                if (VIEW_REMOTE) {
                    if (display.getFileObject().getType() == FileType.FOLDER) {
                        fileDisplay.setVisibility(View.GONE);
                        pathView.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);

                        currentRemoteDirectory = display.getFileObject().getName().getPath();

                        animateTopBarPath = true;
                        longOperation = new PerformLongOperation();
                        longOperation.execute(OPERATION_GET_FILES);
                    } else {
                        showProgressDialog = true;
                        tempOpenFile = display.getFileObject().getName().getBaseName();
                        longOperation = new PerformLongOperation();
                        longOperation.execute(OPERATION_OPEN);

                    }
                } else {
                    if (display.getFileObject().getType() == FileType.FOLDER) {
                        fileDisplay.setVisibility(View.GONE);
                        pathView.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);

                        currentLocalDirectory = display.getFileObject().getName().getPath();
                        animateTopBarPath = true;
                        displayLocalFiles();
                    } else {
                        openFile(FileSystemActivity.this, new File(display.getFileObject().getName().getPath()));
                    }
                }
            } catch (IOException e) {
                displayMessage(R.string.error_opening_files, R.string.error_opening_files_mssg, true);
            }
        }
    }

    private class ImageItemViewOnLongClickListener implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(View v) {
            ImageItemView view = (ImageItemView) v;
            int childCount = fileDisplay.getChildCount();
            for (int i = 0; i < childCount; i++) {
                if (i != 0) {
                    ImageItemView item = (ImageItemView) fileDisplay.getChildAt(i);
                    item.getCheckItem().setVisibility(View.VISIBLE);
                    item.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ImageItemView fileItem = (ImageItemView) v;
                            fileItem.getCheckItem().setChecked(true);
                        }
                    });
                    item.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            int childCount = fileDisplay.getChildCount();
                            for (int i = 0; i < childCount; i++) {
                                if (i != 0) {
                                    ImageItemView item = (ImageItemView) fileDisplay.getChildAt(i);
                                    item.getCheckItem().setChecked(false);
                                    item.getCheckItem().setVisibility(View.GONE);
                                    item.setOnClickListener(new ImageItemViewOnClickListener());
                                    item.setOnLongClickListener(new ImageItemViewOnLongClickListener());
                                }
                            }
                            return true;
                        }
                    });
                }
            }
            view.getCheckItem().setChecked(true);
            return true;
        }
    }

    private void displayMessage(int error_header, int error_message, final boolean finish_activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(FileSystemActivity.this);
        builder.setTitle(error_header)
                .setMessage(error_message)
                .setCancelable(true)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (finish_activity) {
                                    longOperation = new PerformLongOperation();
                                    longOperation.execute(OPERATION_CLOSE);
                                    longOperation.cancel(true);
                                    FileSystemActivity.this.finish();
                                }
                            }
                        }).show();
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(CURRENT_REMOTE_DIRECTORY_STRING, currentRemoteDirectory);
        savedInstanceState.putString(CURRENT_LOCAL_DIRECTORY_STRING, currentLocalDirectory);
        savedInstanceState.putInt(TYPE_STRING, type);
        savedInstanceState.putBoolean(VIEW_REMOTE_STRING, VIEW_REMOTE);
        savedInstanceState.putInt(REMOTE_SORT_TYPE_STRING, remoteSortType);
        savedInstanceState.putInt(LOCAL_SORT_TYPE_STRING, localSortType);
        savedInstanceState.putInt(NOTIFICATION_ID_STRING, notificationID);
        if (type == SessionContract.FTP)
            savedInstanceState.putParcelable(SESSION_STRING, ftpSession);
        else
            savedInstanceState.putParcelable(SESSION_STRING, sftpSession);

    }

    public static void openFile(Context context, File url) throws IOException, ActivityNotFoundException {
        // Create URI
        File file = url;
        Uri uri = Uri.fromFile(file);

        Intent intent = new Intent(Intent.ACTION_VIEW);

        if (url.toString().contains(".doc") || url.toString().contains(".docx")) {
            intent.setDataAndType(uri, "application/msword");
        }else if (url.toString().contains(".au")) {
            intent.setDataAndType(uri, "audio/basic");
        } else if (url.toString().contains(".bmp")) {
            intent.setDataAndType(uri, "image/bmp");
        }else if (url.toString().contains(".bz2")) {
            intent.setDataAndType(uri, "application/bzip2");
        }else if (url.toString().contains(".pdf")) {
            intent.setDataAndType(uri, "application/pdf");
        } else if (url.toString().contains(".ppt") || url.toString().contains(".pptx")) {
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        } else if (url.toString().contains(".xls") || url.toString().contains(".xlsx")) {
            intent.setDataAndType(uri, "application/vnd.ms-excel");
        } else if (url.toString().contains(".zip") || url.toString().contains(".rar")) {
            intent.setDataAndType(uri, "application/zip");
        } else if (url.toString().contains(".rtf")) {
            intent.setDataAndType(uri, "application/rtf");
        } else if (url.toString().contains(".wav") || url.toString().contains(".mp3") || url.toString().contains(".m4a") || url.toString().contains(".ogg")) {
            intent.setDataAndType(uri, "audio/x-wav");
        } else if (url.toString().contains(".gif")) {
            intent.setDataAndType(uri, "image/gif");
        } else if (url.toString().contains(".jpg") || url.toString().contains(".jpeg") || url.toString().contains(".png")) {
            intent.setDataAndType(uri, "image/jpeg");
        } else if (url.toString().contains(".txt")) {

            intent.setDataAndType(uri, "text/plain");
        } else if (url.toString().contains(".3gp") || url.toString().contains(".mpg")  || url.toString().contains(".mpeg") || url.toString().contains(".mpe") || url.toString().contains(".mp4") || url.toString().contains(".avi") ) {
            intent.setDataAndType(uri, "video/*");
        } else {
            intent.setDataAndType(uri, "text/plain");
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

    }

    private void setTopBarPath() {

        final float scale = getResources().getDisplayMetrics().density;
        //int pixels = (int) (55 * scale + 0.5f);
        List<String> directoriesInPath;
        rootView = (TextView) findViewById(R.id.file_system_root_text_view);

        if (VIEW_REMOTE) {
            directoriesInPath = Utils.stringToList(currentRemoteDirectory, "/");
            if (directoriesInPath.size() > 0)
                directoriesInPath.remove(0);

            rootView.setText("Remote Server");
            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressBar.setVisibility(View.VISIBLE);
                    fileDisplay.setVisibility(View.GONE);
                    pathView.setVisibility(View.GONE);

                    currentRemoteDirectory = "/";
                    animateTopBarPath = true;
                    longOperation = new PerformLongOperation();
                    longOperation.execute(OPERATION_GET_FILES);
                }
            });
        } else {
            directoriesInPath = Utils.stringToList(currentLocalDirectory, "/");
            for (int i = 0; i < 4; i++)
                directoriesInPath.remove(0);

            rootView.setText("Device Storage");
            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentLocalDirectory = "/storage/emulated/0/";
                    animateTopBarPath = true;
                    displayLocalFiles();
                }
            });


        }
        rootView.setPadding((int) (10 * scale + 0.5f), (int) (15 * scale + 0.5f), 0, 0);
        rootView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        int childCount = pathView.getChildCount();
        for (int i = 1; i < childCount; i++) {
            pathView.removeViewAt(1);
        }
        rootView.setTextColor(getResources().getColor(R.color.primary_dark_material_light));
        if ((!currentRemoteDirectory.equals("/") && VIEW_REMOTE) ||
                (!currentLocalDirectory.equals("/storage/emulated/0/") && !VIEW_REMOTE))
            for (int i = 0; i < directoriesInPath.size(); i++) {

                ImageView chevron = new ImageView(this);

                chevron.setImageResource(R.drawable.ic_chevron_right_black);
                chevron.setColorFilter(getResources().getColor(R.color.primary_dark_material_light));
                chevron.setPadding(0, (int) (10 * scale + 0.5f), 0, 0);
                LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams((int) (35 * scale + 0.5f), (int) (35 * scale + 0.5f));
                chevron.setLayoutParams(params);

                TextView textView = new TextView(this);
                textView.setText(directoriesInPath.get(i));
                textView.setTextColor(getResources().getColor(R.color.primary_dark_material_light));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                textView.setOnClickListener(new MoveToDirectoryListener());
                textView.setPadding(0, (int) (15 * scale + 0.5f), 0, 0);
                //textView.setTextSize();
                params =
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                textView.setLayoutParams(params);


                if (i == directoriesInPath.size() - 1) {
                    if (animateTopBarPath) {
                        Animation slideLeftToRight = AnimationUtils.loadAnimation(this, R.anim.path_views_anim);
                        textView.setAnimation(slideLeftToRight);
                    }

                    textView.setTextColor(getResources().getColor(R.color.white));

                }
                pathView.addView(chevron);
                pathView.addView(textView);
            }
        else {
            rootView.setTextColor(getResources().getColor(R.color.white));
        }

        topBarScrollView.postDelayed(new Runnable() {
            public void run() {
                topBarScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
        }, 100L);

    }

    private ArrayList<FileObject> getUserSelectedItems() {
        ArrayList<FileObject> userSelectedItems = new ArrayList<>();
        int childCount = fileDisplay.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageItemView item = (ImageItemView) fileDisplay.getChildAt(i);
            if (item.getCheckItem().isChecked()) {
                userSelectedItems.add(item.getFileObject());
            }
        }
        return userSelectedItems;
    }

    @Override
    public void onBackPressed() {
        int selectedCount = getUserSelectedItems().size();
        if (selectedCount > 0) {
            for (int i = 0; i < fileDisplay.getChildCount(); i++) {
                if (i != 0) {
                    ImageItemView item = (ImageItemView) fileDisplay.getChildAt(i);
                    item.getCheckItem().setChecked(false);
                    item.getCheckItem().setVisibility(View.GONE);
                    item.setOnClickListener(new ImageItemViewOnClickListener());
                    item.setOnLongClickListener(new ImageItemViewOnLongClickListener());
                }
            }
        } else {
            if (operation_id == OPERATION_OPEN) {
                showProgressDialog = false;
                longOperation.cancel(true);
            } else if (operation_id == OPERATION_SEARCH) {
                progressBar.setVisibility(View.VISIBLE);
                fileDisplay.setVisibility(View.GONE);
                pathView.setVisibility(View.GONE);

                showProgressDialog = false;
                resetSearch = true;
                longOperation.cancel(true);
                if (VIEW_REMOTE) {
                    animateTopBarPath = false;
                    connection.setCancelled(true);
                    longOperation = new PerformLongOperation();
                    longOperation.execute(OPERATION_GET_FILES);
                } else {
                    animateTopBarPath = false;
                    localFileSystem.setCancelled(true);
                    displayLocalFiles();
                }
            } else {
                moveUpDirectories(true);
            }
        }
    }

    private void moveUpDirectories(boolean canFinishActivity) {
        if (VIEW_REMOTE) {
            if (!currentRemoteDirectory.equals("/")) {
                if (!searching) {
                    currentRemoteDirectory = previousRemoteDirectory;
                    searching = false;
                }
                progressBar.setVisibility(View.VISIBLE);
                fileDisplay.setVisibility(View.GONE);
                pathView.setVisibility(View.GONE);

                animateTopBarPath = true;
                longOperation = new PerformLongOperation();
                longOperation.execute(OPERATION_GET_FILES);
            } else {
                if (canFinishActivity) {
                    longOperation = new PerformLongOperation();
                    longOperation.execute(OPERATION_CLOSE);
                    longOperation.cancel(true);

                    finish();
                } else
                    displayMessage(R.string.error_changing_directory, R.string.error_changing_directory_mssg, false);
            }
        } else {
            if (!currentLocalDirectory.equals("/storage/emulated/0/") && !currentLocalDirectory.equals("/storage/emulated/0")) {
                if (!searching) {
                    currentLocalDirectory = previousLocalDirectory;
                    searching = false;
                }
                animateTopBarPath = true;
                displayLocalFiles();
            } else {
                if (canFinishActivity) {
                    longOperation = new PerformLongOperation();
                    longOperation.execute(OPERATION_CLOSE);
                    longOperation.cancel(true);
                    finish();
                } else
                    displayMessage(R.string.error_changing_directory, R.string.error_changing_directory_mssg, false);
            }
        }
    }

    private void displayLocalFiles() {


        progressBar.setVisibility(View.GONE);
        fileDisplay.setVisibility(View.VISIBLE);
        pathView.setVisibility(View.VISIBLE);

        fileDisplay.removeAllViews();
        {
            setTopBarPath();
            ImageItemView fileItemDisplay = new ImageItemView(this, false);
            fileItemDisplay.getSideImage().setImageResource(R.drawable.hidden_folder_icon);
            fileItemDisplay.getMainTextView().setText("..");
            fileItemDisplay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    moveUpDirectories(false);
                }
            });
            fileDisplay.addView(fileItemDisplay);
        }
        //File For Local Main Storage
        try {
            List<FileObject> files = localFileSystem.getFilesInDir(currentLocalDirectory, localSortType);
            previousLocalDirectory = localFileSystem.getPrevDir();
            for (final FileObject file : files) {
                {
                    SharedPreferences settings = getSharedPreferences(SessionContract.PREFERENCES, Context.MODE_PRIVATE);
                    boolean showHiddenFiles = settings.getBoolean(SessionContract.PREFERRED_HIDDEN_FILE_DISPLAY,
                            true);
                    if (showHiddenFiles || (!file.getName().getBaseName().startsWith(".")) && !showHiddenFiles ) {
                        final ImageItemView fileItemDisplay = new ImageItemView(this, false);
                        if (file.getType() == FileType.FILE && !file.getName().getBaseName().startsWith(".")) {
                            fileItemDisplay.getSideImage().setImageResource(R.drawable.file_icon);
                        } else if (file.getType() == FileType.FILE && file.getName().getBaseName().startsWith(".")) {
                            fileItemDisplay.getSideImage().setImageResource(R.drawable.hidden_file_icon);
                        } else if (file.getType() == FileType.FOLDER && !file.getName().getBaseName().startsWith(".")) {
                            fileItemDisplay.getSideImage().setImageResource(R.drawable.folder_icon);
                        } else {
                            fileItemDisplay.getSideImage().setImageResource(R.drawable.hidden_folder_icon);
                        }
                        fileItemDisplay.getMainTextView().setText(file.getName().getBaseName());
                        fileItemDisplay.setOnClickListener(new ImageItemViewOnClickListener());
                        fileItemDisplay.setOnLongClickListener(new ImageItemViewOnLongClickListener());
                        fileItemDisplay.setFileObject(file);
                        fileDisplay.addView(fileItemDisplay);
                    }
                }
            }
        } catch (FileSystemException e) {
            displayMessage(R.string.error_getting_file_system, R.string.error_getting_file_system_mssg, true);
        }
    }

    public class MoveToDirectoryListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (VIEW_REMOTE) {
                TextView textView = (TextView) v;
                String text = textView.getText().toString();
                currentRemoteDirectory =
                        currentRemoteDirectory.substring(0,
                                currentRemoteDirectory.indexOf(text) + text.length());
                longOperation = new PerformLongOperation();
                longOperation.execute(OPERATION_GET_FILES);

            } else {
                TextView textView = (TextView) v;
                String text = textView.getText().toString();
                currentLocalDirectory =
                        currentLocalDirectory.substring(0,
                                currentLocalDirectory.indexOf(text) + text.length());
                animateTopBarPath = true;
                displayLocalFiles();
            }
        }
    }

    public long getSize(FileObject file) throws FileSystemException {
        long size = 0;
        List<FileObject> object = new ArrayList<>();

        if (file.getType() == FileType.FILE) {
            size += file.getContent().getSize();
        } else {
            FileObject[] children = file.getChildren();

            for (FileObject child : children) {
                getSize(file);
            }
        }
        return size;
    }

    public void displaySearchResults(boolean still_updating) {
        if (!currentDirectory.isEmpty()) {
            if (still_updating)
                rootView.setText("Search Results (Updating)");
            else
                rootView.setText("Search Results");
            rootView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            int childCount = pathView.getChildCount();
            for (int i = 1; i < childCount; i++) {
                pathView.removeViewAt(1);
            }

            fileDisplay.removeAllViews();
            try {
                for (final FileObject file : currentDirectory) {
                    {
                        final ImageItemView fileItemDisplay = new ImageItemView(this, false);
                        if (file.getType() == FileType.FILE && !file.getName().getBaseName().startsWith(".")) {
                            fileItemDisplay.getSideImage().setImageResource(R.drawable.file_icon);
                        } else if (file.getType() == FileType.FILE && file.getName().getBaseName().startsWith(".")) {
                            fileItemDisplay.getSideImage().setImageResource(R.drawable.hidden_file_icon);
                        } else if (file.getType() == FileType.FOLDER && !file.getName().getBaseName().startsWith(".")) {
                            fileItemDisplay.getSideImage().setImageResource(R.drawable.folder_icon);
                        } else {
                            fileItemDisplay.getSideImage().setImageResource(R.drawable.hidden_folder_icon);
                        }
                        fileItemDisplay.getMainTextView().setText(file.getName().getBaseName());
                        fileItemDisplay.setOnClickListener(new ImageItemViewOnClickListener());
                        fileItemDisplay.setOnLongClickListener(new ImageItemViewOnLongClickListener());
                        fileItemDisplay.setFileObject(file);
                        fileDisplay.addView(fileItemDisplay);

                    }

                }
            } catch (FileSystemException e) {
                displayMessage(R.string.error_getting_file_system, R.string.error_getting_file_system_mssg, true);
            }
        } else {
            displayMessage(R.string.error_getting_file_system, R.string.error_getting_file_system_mssg, true);
        }
    }

    public void updateSearch(FileObject file) {
        int start = currentDirectory.size() - 1;


        if (!resetSearch) {
            prog.dismiss();
            currentDirectory.add(file);
            displaySearchResults(true);
            resetSearch = true;
        } else {
            currentDirectory.add(file);
            try {
                final ImageItemView fileItemDisplay = new ImageItemView(this, false);
                if (file.getType() == FileType.FILE && !file.getName().getBaseName().startsWith(".")) {
                    fileItemDisplay.getSideImage().setImageResource(R.drawable.file_icon);
                } else if (file.getType() == FileType.FILE && file.getName().getBaseName().startsWith(".")) {
                    fileItemDisplay.getSideImage().setImageResource(R.drawable.hidden_file_icon);
                } else if (file.getType() == FileType.FOLDER && !file.getName().getBaseName().startsWith(".")) {
                    fileItemDisplay.getSideImage().setImageResource(R.drawable.folder_icon);
                } else {
                    fileItemDisplay.getSideImage().setImageResource(R.drawable.hidden_folder_icon);
                }
                fileItemDisplay.getMainTextView().setText(file.getName().getBaseName());
                fileItemDisplay.setOnClickListener(new ImageItemViewOnClickListener());
                fileItemDisplay.setOnLongClickListener(new ImageItemViewOnLongClickListener());
                fileItemDisplay.setFileObject(file);
                fileDisplay.addView(fileItemDisplay);

            } catch (FileSystemException e) {
                displayMessage(R.string.error_getting_file_system, R.string.error_getting_file_system_mssg, true);
            }
        }
    }
}
