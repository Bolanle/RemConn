package me.bolanleonifade.remoteconnection;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class SessionSettingsActivity extends AppCompatActivity {

    private SFTPSession sftpSession = null;
    private FTPSession ftpSession = null;

    private ImageButton sessionSettingsGoBack;


    private EditText hostEditText;
    private EditText portEditText;
    private Spinner typeSpinner;
    private EditText userEditText;
    private EditText passwordEditText;

    private EditText remoteDirectoryEditText;
    private EditText localDirectoryEditText;
    private Switch saveDeletedFilesToggleButton;
    private Switch saveOverWrittenFilesToggleButton;
    private EditText recycleBinEditText;


    private EditText sshKeyEditText;
    private EditText sshPassEditText;

    private Switch turnOnToggleButton;
    private Spinner masterSpinner;
    private EditText syncLocalDirectoryEditText;
    private EditText syncRemoteDirectoryEditText;
    private Switch recursiveToggleButton;
    private Switch mobileNetworkToggleButton;

    private Button scheduleMondayButton;
    private Button scheduleTuesdayButton;
    private Button scheduleWednesdayButton;
    private Button scheduleThursdayButton;
    private Button scheduleFridayButton;
    private Button scheduleSaturdayButton;
    private Button scheduleSundayButton;

    private static Spinner dailyScheduleSpinner;

    private static String time;
    private static boolean userSetTimeSelection;
    // private static boolean userSetKeepAlive;
    private int TYPE;
    private SessionDataSource dbSource;
    private static boolean userSetTimeOut;
    private boolean saved;
    private boolean newlyCreated;
    private FTPSession oldFtpSession;
    private SFTPSession oldSftpSession;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_settings);
        saved = false;
        Toolbar toolbar = (Toolbar) findViewById(R.id.session_settings_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);

        Intent startPrompterIntent = getIntent();

        TextView toolBarTextView = (TextView) findViewById(R.id.session_settings_toolbar_title);
        Bundle b = null;
        if (startPrompterIntent.getBundleExtra(SessionContract.CURRENT_SESSION) == null)
            TYPE = SessionContract.NONE;
        else {
            b = startPrompterIntent.getBundleExtra(SessionContract.CURRENT_SESSION);
            TYPE = b.getInt(SessionContract.TYPE, SessionContract.NONE);
        }


        //Initialise Objects
        time = "";
        dbSource = new SessionDataSource(this);
        userSetTimeSelection = false;

        hostEditText = (EditText) findViewById(R.id.general_host_edit_text);
        portEditText = (EditText) findViewById(R.id.general_port_edit_text);
        typeSpinner = (Spinner) findViewById(R.id.general_type_spinner);

        {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.connection_type,
                    android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            typeSpinner.setAdapter(adapter);
            typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedItem = (String) parent.getItemAtPosition(position);
                    if (selectedItem.equals(getResources().getString(R.string.ftp))) {
                        sshKeyEditText.setEnabled(false);
                    } else {
                        sshKeyEditText.setEnabled(true);
                    }

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

        userEditText = (EditText) findViewById(R.id.general_user_edit_text);
        passwordEditText = (EditText) findViewById(R.id.general_password_edit_text);

        remoteDirectoryEditText = (EditText) findViewById(R.id.environment_remote_dir_edit_text);
        localDirectoryEditText = (EditText) findViewById(R.id.environment_local_dir_edit_text);
        saveDeletedFilesToggleButton = (Switch) findViewById(R.id.environment_save_deleted_files_toggle_button);
        saveOverWrittenFilesToggleButton = (Switch) findViewById(R.id.environment_save_overwritten_files_toggle_button);
        recycleBinEditText = (EditText) findViewById(R.id.environment_recycle_bin_edit_text);

        sshKeyEditText = (EditText) findViewById(R.id.ssh_ssh_key_edit_text);
        sshPassEditText = (EditText) findViewById(R.id.ssh_ssh_pass_edit_text);

        turnOnToggleButton = (Switch) findViewById(R.id.synchronisation_turn_on_toggle_button);
        turnOnToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    masterSpinner.setClickable(true);

                    syncLocalDirectoryEditText.setEnabled(true);
                    syncRemoteDirectoryEditText.setEnabled(true);

                    recursiveToggleButton.setEnabled(true);
                    mobileNetworkToggleButton.setEnabled(true);

                    scheduleMondayButton.setEnabled(true);
                    scheduleTuesdayButton.setEnabled(true);
                    scheduleWednesdayButton.setEnabled(true);
                    scheduleThursdayButton.setEnabled(true);
                    scheduleFridayButton.setEnabled(true);
                    scheduleSaturdayButton.setEnabled(true);
                    scheduleSundayButton.setEnabled(true);

                    dailyScheduleSpinner.setClickable(true);
                } else {
                    masterSpinner.setClickable(false);

                    syncLocalDirectoryEditText.setEnabled(false);
                    syncLocalDirectoryEditText.setText("");
                    syncRemoteDirectoryEditText.setEnabled(false);
                    syncRemoteDirectoryEditText.setText("");

                    recursiveToggleButton.setEnabled(false);
                    recursiveToggleButton.setChecked(false);
                    mobileNetworkToggleButton.setEnabled(false);
                    mobileNetworkToggleButton.setChecked(false);

                    scheduleMondayButton.setEnabled(false);
                    scheduleMondayButton.setActivated(false);
                    scheduleTuesdayButton.setEnabled(false);
                    scheduleTuesdayButton.setActivated(false);
                    scheduleWednesdayButton.setEnabled(false);
                    scheduleWednesdayButton.setActivated(false);
                    scheduleThursdayButton.setEnabled(false);
                    scheduleThursdayButton.setActivated(false);
                    scheduleFridayButton.setEnabled(false);
                    scheduleFridayButton.setActivated(false);
                    scheduleSaturdayButton.setEnabled(false);
                    scheduleSaturdayButton.setActivated(false);
                    scheduleSundayButton.setEnabled(false);
                    scheduleSundayButton.setActivated(false);

                    dailyScheduleSpinner.setClickable(false);
                }
            }
        });
        masterSpinner = (Spinner) findViewById(R.id.synchronisation_master_spinner);
        {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.master_machine,
                    android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            masterSpinner.setAdapter(adapter);
        }
        syncLocalDirectoryEditText = (EditText) findViewById(R.id.synchronisation_local_dir_edit_text);
        syncRemoteDirectoryEditText = (EditText) findViewById(R.id.synchronisation_remote_dir_edit_text);
        recursiveToggleButton = (Switch) findViewById(R.id.synchronisation_recursive_toggle_button);
        mobileNetworkToggleButton = (Switch) findViewById(R.id.synchronisation_mobile_network_toggle_button);


        scheduleMondayButton = (Button) findViewById(R.id.synchronisation_schedule_monday_button);
        scheduleTuesdayButton = (Button) findViewById(R.id.synchronisation_schedule_tuesday_button);
        scheduleWednesdayButton = (Button) findViewById(R.id.synchronisation_schedule_wednesday_button);
        scheduleThursdayButton = (Button) findViewById(R.id.synchronisation_schedule_thursday_button);
        scheduleFridayButton = (Button) findViewById(R.id.synchronisation_schedule_friday_button);
        scheduleSaturdayButton = (Button) findViewById(R.id.synchronisation_schedule_saturday_button);
        scheduleSundayButton = (Button) findViewById(R.id.synchronisation_schedule_sunday_button);

        dailyScheduleSpinner = (Spinner) findViewById(R.id.synchronisation_daily_schedule_spinner);
        {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.schedule_times,
                    android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            dailyScheduleSpinner.setAdapter(adapter);
            dailyScheduleSpinner.setSelection(0, false);
            dailyScheduleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int pos, long id) {
                    // An spinnerItem was selected. You can retrieve the selected item using
                    String selectedItem = (String) parent.getItemAtPosition(pos);
                    if (userSetTimeSelection) {
                        if (selectedItem.contains(":") || selectedItem.contains(getResources().getString(R.string.only_at))) {

                            TimePickerFragment newFragment = new TimePickerFragment();
                            newFragment.setActivity(getBaseContext());
                            newFragment.show(getFragmentManager(), "timePicker");

                        } else {
                            time = "";
                            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                                    SessionSettingsActivity.this, R.array.schedule_times,
                                    android.R.layout.simple_spinner_item);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            dailyScheduleSpinner.setAdapter(adapter);
                            dailyScheduleSpinner.setSelection(pos, false);
                        }
                        userSetTimeSelection = false;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            dailyScheduleSpinner.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    userSetTimeSelection = true;
                    return false;
                }
            });
        }

        if (TYPE == SessionContract.NONE) {
            toolBarTextView.setText("New Session");
            newlyCreated = true;
            ftpSession = new FTPSession();
            sftpSession = new SFTPSession();

        } else {
            newlyCreated = false;
            toolBarTextView.setText("Session Settings"); //Set Session Too
            if (TYPE == SessionContract.SFTP) {
                sftpSession = b.getParcelable(FileSystemActivity.SESSION_STRING);
                updateUI(sftpSession);
            } else {
                ftpSession = b.getParcelable(FileSystemActivity.SESSION_STRING);
                updateUI(ftpSession);
            }
        }
        oldFtpSession = ftpSession;
        oldSftpSession = sftpSession;

        sessionSettingsGoBack = (ImageButton) findViewById(R.id.session_settings_go_back);
        sessionSettingsGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitSessionSettings(v);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        //Check if any top buttons
        if (item.getItemId() == R.id.save_session) {
            int sessionType = save();
            if (sessionType != SessionContract.NONE) {
                saved = true;
                Intent resultIntent = new Intent();
                setResult(Activity.RESULT_OK, resultIntent);
                finish();

            }
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_session_settings, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    public void startSession(View view) {
        int sessionType = save();
        if (sessionType != SessionContract.NONE) {
            Intent intent = new Intent(this, FileSystemActivity.class);
            if (sessionType == SessionContract.FTP) {
                Bundle b = new Bundle();
                b.putParcelable(FileSystemActivity.SESSION_STRING, ftpSession);
                b.putInt(SessionContract.TYPE, SessionContract.FTP);
                intent.putExtra(SessionContract.CURRENT_SESSION, b);

            } else if (sessionType == SessionContract.SFTP) {
                Bundle b = new Bundle();
                b.putParcelable(FileSystemActivity.SESSION_STRING, sftpSession);
                b.putInt(SessionContract.TYPE, SessionContract.SFTP);
                intent.putExtra(SessionContract.CURRENT_SESSION, b);
            }
            newlyCreated = false;
            saved = true;
            startActivity(intent);
        }
    }

    public int save() {
        int multiplier = 1000; //multiplier for generating unique iitents
        int syncId = 0;
        AlarmManager alarmMgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        String host = hostEditText.getText().toString().trim();
        if (!URLUtil.isValidUrl("http://" + host)) { //Simple check to ensure URL is valid
            AlertDialog.Builder builder = new AlertDialog.Builder(SessionSettingsActivity.this);
            builder.setTitle(R.string.error_not_valid_host)
                    .setMessage(R.string.error_not_valid_host_mssg)
                    .setCancelable(true)
                    .setPositiveButton("OK", null).show();
            return SessionContract.NONE;
        }

        String user = userEditText.getText().toString().trim();
        user = user.isEmpty() ? "anonymous" : user;
        int type = typeSpinner.getSelectedItemPosition() + 1;
        TYPE = type;

        int port;
        try {
            port = Integer.parseInt(portEditText.getText().toString());
        } catch (NumberFormatException e) {  //port hasn't been set
            if (type == SessionContract.SFTP) {
                port = 22;
            } else { //type is FTP
                port = 21;
            }
        }
        String password = passwordEditText.getText().toString();
        password = password.isEmpty() ? "anonymous" : password;

        String remoteDirectory = remoteDirectoryEditText.getText().toString().trim();
        String localDirectory = localDirectoryEditText.getText().toString().trim();

        localDirectory = localDirectory.startsWith("/")
                ? localDirectory.replaceFirst("/", "") : localDirectory;
        localDirectory = "/storage/emulated/0/" + localDirectory;

        remoteDirectory = !remoteDirectory.startsWith("/")
                ? "/" + remoteDirectory : remoteDirectory;

        boolean saveDeletedFiles = saveDeletedFilesToggleButton.isChecked();
        boolean saveOverwrittenFiles = saveOverWrittenFilesToggleButton.isChecked();
        String recycleBin = recycleBinEditText.getText().toString().trim();

        if (saveDeletedFiles && recycleBin.isEmpty() || saveOverwrittenFiles && recycleBin.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SessionSettingsActivity.this);
            builder.setTitle(R.string.error_recycle_bin_save)
                    .setMessage(R.string.error_recycle_bin_save_mssg)
                    .setCancelable(true)
                    .setPositiveButton("OK", null).show();
            return SessionContract.NONE;
        }
        String sshKey = sshKeyEditText.getText().toString().trim();
        sshKey = sshKey.startsWith("/")
                ? sshKey.replaceFirst("/", "") : sshKey;
        sshKey = "/storage/emulated/0/" + sshKey;

        String sshPass = sshPassEditText.getText().toString();
        boolean turnOnSynchronisation = turnOnToggleButton.isChecked();

        String master = masterSpinner.getSelectedItem().toString().trim();
        String syncLocalDirectory = syncLocalDirectoryEditText.getText().toString().trim();
        String syncRemoteDirectory = syncRemoteDirectoryEditText.getText().toString().trim();

        boolean recursive = recursiveToggleButton.isChecked();
        boolean mobileNetwork = mobileNetworkToggleButton.isChecked();
        String days = Utils.listToString(getSelectedSyncDays(), ", ");
        String selectedTime = "";
        selectedTime += !time.equals("") ? time
                : (String) dailyScheduleSpinner.getItemAtPosition(dailyScheduleSpinner.getSelectedItemPosition());
        time = selectedTime.trim();
        if (turnOnSynchronisation && (master.isEmpty() || syncLocalDirectory.isEmpty() ||
                syncRemoteDirectory.isEmpty() || days.isEmpty() || selectedTime.isEmpty())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SessionSettingsActivity.this);
            builder.setTitle(R.string.error_sync_fields_missing)
                    .setMessage(R.string.error_sync_fields_missing_mssg)
                    .setCancelable(true)
                    .setPositiveButton("OK", null).show();
            return SessionContract.NONE;
        } else {
            syncLocalDirectory = syncLocalDirectory.startsWith("/")
                    ? syncLocalDirectory.replaceFirst("/", "") : syncLocalDirectory;
            syncLocalDirectory = "/storage/emulated/0/" + syncLocalDirectory;

            syncRemoteDirectory = !syncRemoteDirectory.startsWith("/")
                    ? "/" + syncRemoteDirectory : syncRemoteDirectory;
        }
        if (newlyCreated) {
            syncId = dbSource.createSession(host, user, type,
                    port, password, remoteDirectory, localDirectory, saveDeletedFiles,
                    saveOverwrittenFiles, recycleBin, sshKey, sshPass, turnOnSynchronisation,
                    master, syncLocalDirectory, syncRemoteDirectory, recursive, mobileNetwork,
                    days, time);
            Toast.makeText(this, "Saved New Session", Toast.LENGTH_SHORT).show();
        } else {
            if (type == SessionContract.FTP) {
                if (sftpSession != null) {
                    ftpSession = new FTPSession();
                    ftpSession.setId(sftpSession.getId());
                }
                dbSource.updateSession(ftpSession.getId(), host, user, type,
                        port, password, remoteDirectory, localDirectory, saveDeletedFiles,
                        saveOverwrittenFiles, recycleBin, sshKey, sshPass, turnOnSynchronisation,
                        master, syncLocalDirectory, syncRemoteDirectory, recursive, mobileNetwork,
                        days, time);
                syncId = ftpSession.getId();
            } else if (TYPE == SessionContract.SFTP) {
                if (ftpSession != null) {
                    sftpSession = new SFTPSession();
                    sftpSession.setId(ftpSession.getId());
                }
                dbSource.updateSession(sftpSession.getId(), host, user, type,
                        port, password, remoteDirectory, localDirectory, saveDeletedFiles,
                        saveOverwrittenFiles, recycleBin, sshKey, sshPass, turnOnSynchronisation,
                        master, syncLocalDirectory, syncRemoteDirectory, recursive, mobileNetwork,
                        days, time);
                syncId = sftpSession.getId();
            }
            Toast.makeText(this, "Updated existing session", Toast.LENGTH_SHORT).show();
        }
        PendingIntent alarmIntent;

        for (int i = 1; i < 8; i++) {
            int alarmID = (syncId * multiplier) + 1;
            Intent intent = new Intent(SynchronisationReceiver.ACTION_ALARM);
            Bundle b = new Bundle();
            if (oldFtpSession != null) {
                b.putParcelable("session", oldFtpSession);
                b.putInt(SessionContract.TYPE, SessionContract.FTP);
                intent.putExtra(SessionContract.CURRENT_SESSION, b);
                alarmIntent = PendingIntent.getBroadcast(this, alarmID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmMgr.cancel(alarmIntent);
            }
            if(oldSftpSession != null){
                b.putParcelable("session", oldSftpSession);
                b.putInt(SessionContract.TYPE, SessionContract.SFTP);
                intent.putExtra(SessionContract.CURRENT_SESSION, b);
                alarmIntent = PendingIntent.getBroadcast(this, alarmID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmMgr.cancel(alarmIntent);
            }



        }

        if (type == SessionContract.FTP) {
            ftpSession.setId(syncId);
            ftpSession.setHost(host);
            ftpSession.setUser(user);
            ftpSession.setPort(port);
            ftpSession.setPassword(password);

            ftpSession.setRemoteDirectory(remoteDirectory);
            ftpSession.setLocalDirectory(localDirectory);
            ftpSession.setSaveDeletedFiles(saveDeletedFiles);
            ftpSession.setSaveOverwrittenFile(saveOverwrittenFiles);
            ftpSession.setRecycleBin(recycleBin);


            ftpSession.setTurnOnSynchronisation(turnOnSynchronisation);
            ftpSession.setMaster(master);
            ftpSession.setSyncLocalDirectory(syncLocalDirectory);
            ftpSession.setSyncRemoteDirectory(syncRemoteDirectory);
            ftpSession.setRecursive(recursive);
            ftpSession.setMobileNetwork(mobileNetwork);

            ftpSession.setDays(days);
            ftpSession.setTime(time);
            //Set up alarm for synchronisation

            if (turnOnSynchronisation) {

                for (String day : getSelectedSyncDays()) {

                    int alarmID = syncId * multiplier;

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());

                    calendar.set(Calendar.MINUTE, 0);
                    if (day.equals("Monday")) {
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                        alarmID += 1;
                    } else if (day.equals("Tuesday")) {
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
                        alarmID += 2;
                    } else if (day.equals("Wednesday")) {
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
                        alarmID += 3;
                    } else if (day.equals("Thursday")) {
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
                        alarmID += 4;
                    } else if (day.equals("Friday")) {
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
                        alarmID += 5;
                    } else if (day.equals("Saturday")) {
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                        alarmID += 6;
                    } else if (day.equals("Sunday")) {
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                        alarmID += 7;
                    }

                    Intent intent = new Intent(SynchronisationReceiver.ACTION_ALARM);
                    Bundle b = new Bundle();
                    b.putParcelable(FileSystemActivity.SESSION_STRING, ftpSession);
                    b.putInt(SessionContract.TYPE, SessionContract.FTP);
                    intent.putExtra(SessionContract.CURRENT_SESSION, b);
                    alarmIntent = PendingIntent.getBroadcast(this.getApplicationContext(), alarmID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    if (time.equals(getResources().getString(R.string.every_hour))) {
                        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                                1000 * 60 * 60, alarmIntent);
                    } else if (time.equals(getResources().getString(R.string.every_quarter))) {
                        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                                AlarmManager.INTERVAL_FIFTEEN_MINUTES, alarmIntent);
                    } else if (time.equals(getResources().getString(R.string.every_half))) {
                        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                                AlarmManager.INTERVAL_HALF_HOUR, alarmIntent);
                    } else {
                        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(Utils.stringToList(time, ":").get(0)));
                        calendar.set(Calendar.MINUTE, Integer.parseInt(Utils.stringToList(time, ":").get(1)));
                        alarmMgr.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                                alarmIntent);
                    }
                }
            }

        } else if (TYPE == SessionContract.SFTP) {
            sftpSession.setId(syncId);
            sftpSession.setHost(host);
            sftpSession.setUser(user);
            sftpSession.setPort(port);
            sftpSession.setPassword(password);

            sftpSession.setRemoteDirectory(remoteDirectory);
            sftpSession.setLocalDirectory(localDirectory);
            sftpSession.setSaveDeletedFiles(saveDeletedFiles);
            sftpSession.setSaveOverwrittenFile(saveOverwrittenFiles);
            sftpSession.setRecycleBin(recycleBin);

            sftpSession.setSSHKey(sshKey);
            sftpSession.setSSHPass(sshPass);

            sftpSession.setTurnOnSynchronisation(turnOnSynchronisation);
            sftpSession.setMaster(master);
            sftpSession.setSyncLocalDirectory(syncLocalDirectory);
            sftpSession.setSyncRemoteDirectory(syncRemoteDirectory);
            sftpSession.setRecursive(recursive);
            sftpSession.setMobileNetwork(mobileNetwork);

            sftpSession.setDays(days);
            sftpSession.setTime(time);

            this.sftpSession = sftpSession;
            if (turnOnSynchronisation) {
                for (String day : getSelectedSyncDays()) {
                    int alarmID = syncId * multiplier;
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());

                    calendar.set(Calendar.MINUTE, 0);
                    if (day.equals("Monday")) {
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                        alarmID += 1;
                    } else if (day.equals("Tuesday")) {
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
                        alarmID += 2;
                    } else if (day.equals("Wednesday")) {
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
                        alarmID += 3;
                    } else if (day.equals("Thursday")) {
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
                        alarmID += 4;
                    } else if (day.equals("Friday")) {
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
                        alarmID += 5;
                    } else if (day.equals("Saturday")) {
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                        alarmID += 6;
                    } else if (day.equals("Sunday")) {
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                        alarmID += 7;
                    }

                    Intent intent = new Intent(SynchronisationReceiver.ACTION_ALARM);
                    Bundle b = new Bundle();

                    b.putParcelable(FileSystemActivity.SESSION_STRING, sftpSession);
                    b.putInt(SessionContract.TYPE, SessionContract.SFTP);
                    intent.putExtra(SessionContract.CURRENT_SESSION, b);
                    alarmIntent = PendingIntent.getBroadcast(this.getApplicationContext(), alarmID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    if (time.equals(getResources().getString(R.string.every_hour))) {
                        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                                1000 * 60 * 60, alarmIntent);
                    } else if (time.equals(getResources().getString(R.string.every_quarter))) {
                        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                                AlarmManager.INTERVAL_FIFTEEN_MINUTES, alarmIntent);
                    } else if (time.equals(getResources().getString(R.string.every_half))) {
                        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                                AlarmManager.INTERVAL_HALF_HOUR, alarmIntent);
                    } else {
                        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(Utils.stringToList(time, ":").get(0)));
                        calendar.set(Calendar.MINUTE, Integer.parseInt(Utils.stringToList(time, ":").get(1)));
                        alarmMgr.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                                alarmIntent);
                    }
                }
            }
        }
        return type;
    }

    public void updateUI(SFTPSession session) {
        hostEditText.setText(session.getHost());
        userEditText.setText(session.getUser());
        typeSpinner.setSelection(1); //SFTP - 1;

        portEditText.setText(String.valueOf(session.getPort()));
        passwordEditText.setText(session.getPassword());
        if (!(session.getRemoteDirectory().length() == 1 && session.getRemoteDirectory().contains("/")))
            remoteDirectoryEditText.setText(session.getRemoteDirectory());

        String temp = session.getLocalDirectory().replace("storage/emulated/0/", "");

        if (!(temp.length() == 1 && temp.contains("/"))) {
            localDirectoryEditText.setText(temp);
        }
        saveDeletedFilesToggleButton.setChecked(session.isSaveDeletedFiles());
        saveOverWrittenFilesToggleButton.setChecked(session.isSaveOverwrittenFile());
        recycleBinEditText.setText(session.getRecycleBin());
        temp = session.getSSHKey().replace("storage/emulated/0/", "");

        if (!(temp.length() == 1 && temp.contains("/"))) {
            sshKeyEditText.setText(temp);
        }
        sshPassEditText.setText(session.getSSHPass());
        turnOnToggleButton.setChecked(session.isTurnOnSynchronisation());
        {
            String[] masterStrings = getResources().getStringArray(R.array.master_machine);
            int pos = 0;
            for (int i = 0; i < masterStrings.length; i++) {
                if (masterStrings[i].equals(session.getMaster())) {
                    pos = i;
                    break;
                }
            }
            masterSpinner.setSelection(pos);
        }
        temp = session.getSyncLocalDirectory().replace("storage/emulated/0/", "");

        if (!(temp.length() == 1 && temp.contains("/"))) {
            syncLocalDirectoryEditText.setText(temp);
        }
        if (!(session.getSyncRemoteDirectory().length() == 1 && session.getSyncRemoteDirectory().contains("/")))
            syncRemoteDirectoryEditText.setText(session.getSyncRemoteDirectory());
        recursiveToggleButton.setChecked(session.isRecursive());
        mobileNetworkToggleButton.setChecked(session.isMobileNetwork());

        String syncDays = session.getDays();
        if (syncDays.contains("Monday")) {
            scheduleMondayButton.setActivated(true);
        }
        if (syncDays.contains("Tuesday")) {
            scheduleTuesdayButton.setActivated(true);
        }
        if (syncDays.contains("Wednesday")) {
            scheduleWednesdayButton.setActivated(true);
        }
        if (syncDays.contains("Thursday")) {
            scheduleThursdayButton.setActivated(true);
        }
        if (syncDays.contains("Friday")) {
            scheduleFridayButton.setActivated(true);
        }
        if (syncDays.contains("Saturday")) {
            scheduleSaturdayButton.setActivated(true);
        }
        if (syncDays.contains("Sunday")) {
            scheduleSundayButton.setActivated(true);
        }

        {
            String[] dailyStrings = getResources().getStringArray(R.array.schedule_times);
            int pos = -1;
            for (int i = 0; i < dailyStrings.length; i++) {
                if (dailyStrings[i].equals(session.getTime())) {
                    pos = i;
                    break;
                }
            }
            if (pos == -1) {

                ArrayAdapter<CharSequence> adapter = new ArrayAdapter(SessionSettingsActivity.this,
                        android.R.layout.simple_spinner_item,
                        new ArrayList(Arrays.asList(getResources().getStringArray(R.array.schedule_times))));
                adapter.remove(getResources().getString(R.string.only_at));
                adapter.add(session.getTime());
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                dailyScheduleSpinner.setAdapter(adapter);
                dailyScheduleSpinner.setSelection(getResources().getStringArray(R.array.schedule_times).length - 1, false);
            }
            dailyScheduleSpinner.setSelection(pos, false);
        }
    }

    public void updateUI(FTPSession session) {
        hostEditText.setText(session.getHost());
        if (session.getUser().equals("anonymous")) {
            userEditText.setText("");
            passwordEditText.setText("");
        } else {
            userEditText.setText(session.getUser());
            passwordEditText.setText(session.getPassword());
        }

        typeSpinner.setSelection(0); //FTP - 1;
        portEditText.setText(String.valueOf(session.getPort()));

        if (!(session.getRemoteDirectory().length() == 1 && session.getRemoteDirectory().contains("/")))
            remoteDirectoryEditText.setText(session.getRemoteDirectory());

        String temp = session.getLocalDirectory().replace("storage/emulated/0/", "");

        if (!(temp.length() == 1 && temp.contains("/"))) {
            localDirectoryEditText.setText(temp);
        }
        saveDeletedFilesToggleButton.setChecked(session.isSaveDeletedFiles());
        saveOverWrittenFilesToggleButton.setChecked(session.isSaveOverwrittenFile());
        recycleBinEditText.setText(session.getRecycleBin());

        turnOnToggleButton.setChecked(session.isTurnOnSynchronisation());
        {
            String[] masterStrings = getResources().getStringArray(R.array.master_machine);
            int pos = 0;
            for (int i = 0; i < masterStrings.length; i++) {
                if (masterStrings[i].equals(session.getMaster())) {
                    pos = i;
                    break;
                }
            }
            masterSpinner.setSelection(pos);
        }
        temp = session.getSyncLocalDirectory().replace("storage/emulated/0/", "");

        if (!(temp.length() == 1 && temp.contains("/"))) {
            syncLocalDirectoryEditText.setText(temp);
        }
        if (!(session.getSyncRemoteDirectory().length() == 1 && session.getSyncRemoteDirectory().contains("/")))
            syncRemoteDirectoryEditText.setText(session.getSyncRemoteDirectory());
        recursiveToggleButton.setChecked(session.isRecursive());
        mobileNetworkToggleButton.setChecked(session.isMobileNetwork());

        String syncDays = session.getDays();
        if (syncDays.contains("Monday")) {
            scheduleMondayButton.setActivated(true);
        }
        if (syncDays.contains("Tuesday")) {
            scheduleTuesdayButton.setActivated(true);
        }
        if (syncDays.contains("Wednesday")) {
            scheduleWednesdayButton.setActivated(true);
        }
        if (syncDays.contains("Thursday")) {
            scheduleThursdayButton.setActivated(true);
        }
        if (syncDays.contains("Friday")) {
            scheduleFridayButton.setActivated(true);
        }
        if (syncDays.contains("Saturday")) {
            scheduleSaturdayButton.setActivated(true);
        }
        if (syncDays.contains("Sunday")) {
            scheduleSundayButton.setActivated(true);
        }

        {
            String[] dailyStrings = getResources().getStringArray(R.array.schedule_times);
            int pos = -1;
            for (int i = 0; i < dailyStrings.length; i++) {
                if (dailyStrings[i].equals(session.getTime())) {
                    pos = i;
                    break;
                }
            }
            if (pos == -1) {

                ArrayAdapter<CharSequence> adapter = new ArrayAdapter(SessionSettingsActivity.this,
                        android.R.layout.simple_spinner_item,
                        new ArrayList(Arrays.asList(getResources().getStringArray(R.array.schedule_times))));
                adapter.remove(getResources().getString(R.string.only_at));
                adapter.add(session.getTime());
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                dailyScheduleSpinner.setAdapter(adapter);
                dailyScheduleSpinner.setSelection(getResources().getStringArray(R.array.schedule_times).length - 1, false);
            }
            dailyScheduleSpinner.setSelection(pos, false);
        }
    }

    private List<String> getSelectedSyncDays() {
        List<String> days = new ArrayList<String>();

        if (scheduleMondayButton.isActivated())
            days.add("Monday");
        if (scheduleTuesdayButton.isActivated())
            days.add("Tuesday");
        if (scheduleWednesdayButton.isActivated())
            days.add("Wednesday");
        if (scheduleThursdayButton.isActivated())
            days.add("Thursday");
        if (scheduleFridayButton.isActivated())
            days.add("Friday");
        if (scheduleSaturdayButton.isActivated())
            days.add("Saturday");
        if (scheduleSundayButton.isActivated())
            days.add("Sunday");

        return days;
    }

    public void setButtonState(View view) {
        if (view.isActivated())
            view.setActivated(false);
        else
            view.setActivated(true);
    }


    public void expandOrContractButton(View view) {
        ImageButton expandOrContract = (ImageButton) findViewById(view.getId());
        Drawable expandImage = getResources().getDrawable(R.drawable.ic_expand_more_black);
        Drawable contractImage = getResources().getDrawable(R.drawable.ic_expand_less_black);

        //Use Icons to RepresentState
        if (expandOrContract.getTag().toString().equals(getResources().getString(R.string.expand))) { //if Expand, then contract
            expandOrContract.setImageDrawable(contractImage);
            expandOrContract.setTag(getResources().getString(R.string.contract));
            if (expandOrContract.getId() == R.id.general_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.general_views_layout);
                layout.setVisibility(View.VISIBLE);
            } else if (expandOrContract.getId() == R.id.environment_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.environment_views);
                layout.setVisibility(View.VISIBLE);
            } else if (expandOrContract.getId() == R.id.ssh_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.ssh_views);
                layout.setVisibility(View.VISIBLE);
            } else if (expandOrContract.getId() == R.id.settings_synchronisation_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.synchronisation_views);
                layout.setVisibility(View.VISIBLE);
            }

        } else {
            expandOrContract.setImageDrawable(expandImage);
            expandOrContract.setTag(getResources().getString(R.string.expand));
            if (expandOrContract.getId() == R.id.general_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.general_views_layout);
                layout.setVisibility(View.GONE);
            } else if (expandOrContract.getId() == R.id.environment_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.environment_views);
                layout.setVisibility(View.GONE);
            } else if (expandOrContract.getId() == R.id.ssh_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.ssh_views);
                layout.setVisibility(View.GONE);
            } else if (expandOrContract.getId() == R.id.settings_synchronisation_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.synchronisation_views);
                layout.setVisibility(View.GONE);
            }
        }

    }

    public void expandOrContractRelativeLayout(View view) {
        RelativeLayout relativeLayout = (RelativeLayout) view;
        ImageButton expandOrContract = (ImageButton) relativeLayout.getChildAt(1);
        Drawable expandImage = getResources().getDrawable(R.drawable.ic_expand_more_black);
        Drawable contractImage = getResources().getDrawable(R.drawable.ic_expand_less_black);

        //Use Icons to RepresentState
        if (expandOrContract.getTag().toString().equals(getResources().getString(R.string.expand))) { //if Expand, then contract
            expandOrContract.setImageDrawable(contractImage);
            expandOrContract.setTag(getResources().getString(R.string.contract));
            if (expandOrContract.getId() == R.id.general_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.general_views_layout);
                layout.setVisibility(View.VISIBLE);
            } else if (expandOrContract.getId() == R.id.environment_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.environment_views);
                layout.setVisibility(View.VISIBLE);
            } else if (expandOrContract.getId() == R.id.ssh_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.ssh_views);
                layout.setVisibility(View.VISIBLE);
            } else if (expandOrContract.getId() == R.id.settings_synchronisation_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.synchronisation_views);
                layout.setVisibility(View.VISIBLE);
            }

        } else {
            expandOrContract.setImageDrawable(expandImage);
            expandOrContract.setTag(getResources().getString(R.string.expand));
            if (expandOrContract.getId() == R.id.general_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.general_views_layout);
                layout.setVisibility(View.GONE);
            } else if (expandOrContract.getId() == R.id.environment_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.environment_views);
                layout.setVisibility(View.GONE);
            } else if (expandOrContract.getId() == R.id.ssh_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.ssh_views);
                layout.setVisibility(View.GONE);
            } else if (expandOrContract.getId() == R.id.settings_synchronisation_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.synchronisation_views);
                layout.setVisibility(View.GONE);
            }
        }
    }

    public void expandOrContractTextView(View view) {
        TextView textView = (TextView) view;

        RelativeLayout relativeLayout = (RelativeLayout) textView.getParent();
        ImageButton expandOrContract = (ImageButton) relativeLayout.getChildAt(1);
        Drawable expandImage = getResources().getDrawable(R.drawable.ic_expand_more_black);
        Drawable contractImage = getResources().getDrawable(R.drawable.ic_expand_less_black);

        //Use Icons to RepresentState
        if (expandOrContract.getTag().toString().equals(getResources().getString(R.string.expand))) { //if Expand, then contract
            expandOrContract.setImageDrawable(contractImage);
            expandOrContract.setTag(getResources().getString(R.string.contract));
            if (expandOrContract.getId() == R.id.general_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.general_views_layout);
                layout.setVisibility(View.VISIBLE);
            } else if (expandOrContract.getId() == R.id.environment_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.environment_views);
                layout.setVisibility(View.VISIBLE);
            } else if (expandOrContract.getId() == R.id.ssh_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.ssh_views);
                layout.setVisibility(View.VISIBLE);
            } else if (expandOrContract.getId() == R.id.settings_synchronisation_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.synchronisation_views);
                layout.setVisibility(View.VISIBLE);
            }

        } else {
            expandOrContract.setImageDrawable(expandImage);
            expandOrContract.setTag(getResources().getString(R.string.expand));
            if (expandOrContract.getId() == R.id.general_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.general_views_layout);
                layout.setVisibility(View.GONE);
            } else if (expandOrContract.getId() == R.id.environment_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.environment_views);
                layout.setVisibility(View.GONE);
            } else if (expandOrContract.getId() == R.id.ssh_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.ssh_views);
                layout.setVisibility(View.GONE);
            } else if (expandOrContract.getId() == R.id.settings_synchronisation_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.synchronisation_views);
                layout.setVisibility(View.GONE);
            }
        }
    }

    public void exitSessionSettings(View view) {
        if (saved) {
            Intent resultIntent = new Intent();
            setResult(Activity.RESULT_OK, resultIntent);
        }
        finish();
    }


    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        int hourSelected;
        int minuteSelected;

        public void setActivity(Context activity) {
            this.activity = activity;
        }

        Context activity;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            Calendar c = Calendar.getInstance();
            hourSelected = c.get(Calendar.HOUR_OF_DAY);
            minuteSelected = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hourSelected, minuteSelected,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
            hourSelected = hourOfDay;
            minuteSelected = minute;

            time = String.valueOf(hourSelected) + ":"
                    + String.format("%02d", minuteSelected);
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter(activity,
                    android.R.layout.simple_spinner_item,
                    new ArrayList(Arrays.asList(getResources().getStringArray(R.array.schedule_times))));


            adapter.remove(getResources().getString(R.string.only_at));
            adapter.add(time);

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            dailyScheduleSpinner.setAdapter(adapter);
            dailyScheduleSpinner.setSelection(getResources().getStringArray(R.array.schedule_times).length - 1, false);
        }

    }

}
