package me.bolanleonifade.remoteconnection;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

public class SessionsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final long DRAWER_CLOSE_DELAY_MS = 250;
    private static final String NAVIGATION_POSITION = "navigationPosition";
    private static final int MENU_ITEMS = 2;

    String ORIGINAL = "ORIGINAL";
    String REMOVEABLE = "REMOVEABLE";
    String SYNCABLE = "SYNCABLE";

    private final Handler drawerActionHandler = new Handler();
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private int navigationPosition;
    private SessionDataSource sessionDataSource;
    public static int REQ_CODE = 10;
    private boolean performingSelectingTask;

    public LinearLayout sessionsContentLinearLayout;
    public ImageButton sessionsGoBackImageButton;
    public Menu menu;
    MenuItem addSessions;
    MenuItem syncSessions;
    MenuItem deleteSessions;
    MenuItem countSessions;
    MenuItem syncSelectedSessions;
    TextView noSessionTextView;
    int count;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sessions);

        drawerLayout = (DrawerLayout) findViewById(R.id.nav_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);

        if (null == savedInstanceState) {
            navigationPosition = R.id.drawer_sessions;
        } else {
            navigationPosition = savedInstanceState.getInt(NAVIGATION_POSITION);
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(this);


        navigationView.getMenu().findItem(navigationPosition).setChecked(true);


        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open,
                R.string.close);
        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();

        navigate(navigationPosition);

        File tempDirectory = new File("/storage/emulated/0/Android/data/me.bolanleonifade/temp");
        for (File subFile : tempDirectory.listFiles())
            subFile.delete();

        SharedPreferences preferences = getSharedPreferences(SessionContract.PREFERENCES, Context.MODE_PRIVATE);
        if (preferences.getBoolean("FIRST_TIME_RUNNING", true)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("FIRST_TIME_RUNNING", false);

            //Clean out reports every week
            AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(DeletionReceiver.ACTION_DELETE);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(this.getApplicationContext(), SettingsActivity.DELETEID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());

            calendar.set(Calendar.DAY_OF_WEEK, Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
            calendar.add(Calendar.WEEK_OF_YEAR, 1);
            alarmManager.cancel(alarmIntent);
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    1000 * 60 * 60 * 24 * 7, alarmIntent);
        }
        noSessionTextView = (TextView) findViewById(R.id.session_no_session_view);
        sessionsContentLinearLayout = (LinearLayout) findViewById(R.id.sessions_content);
        sessionsGoBackImageButton = (ImageButton) findViewById(R.id.sessions_go_back);
        sessionsGoBackImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitSelection();
            }
        });
        count = 0;
        update();
    }

    private void navigate(final int itemId) {
        // perform the actual navigation logic, updating the main content fragment etc
        Intent intent;
        if (itemId == R.id.drawer_sessions) {
            //do Nothing
        } else if (itemId == R.id.drawer_reports) {
            intent = new Intent(this, ReportsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        } else if (itemId == R.id.drawer_settings) {
            intent = new Intent(this, SettingsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        } else if (itemId == R.id.drawer_about) {
            intent = new Intent(this, AboutActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        }

    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem menuItem) {
        menuItem.setChecked(true);
        navigationPosition = menuItem.getItemId();

        drawerLayout.closeDrawer(GravityCompat.START);
        drawerActionHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                navigate(menuItem.getItemId());
            }
        }, DRAWER_CLOSE_DELAY_MS);
        return true;
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.support.v7.appcompat.R.id.home) {
            return drawerToggle.onOptionsItemSelected(item);
        }

        //Check if any top buttons
        if (item.getItemId() == R.id.add_sessions) {
            Intent intent = new Intent(this, SessionSettingsActivity.class);
            startActivityForResult(intent, REQ_CODE);

        } else if (item.getItemId() == R.id.sync_sessions) {
            count = 0;
            performingSelectingTask = true;
            drawerToggle.setDrawerIndicatorEnabled(false);
            sessionsGoBackImageButton.setVisibility(View.VISIBLE);
            syncSelectedSessions.setVisible(true);
            countSessions.setVisible(true);
            addSessions.setVisible(false);
            syncSessions.setVisible(false);
            deleteSessions.setVisible(false);
            countSessions.setTitle(String.valueOf(count));
            int childCount = sessionsContentLinearLayout.getChildCount();
            for (int i = 0; i < childCount; i++) {
                ImageItemView view = (ImageItemView) ((GridLayout) sessionsContentLinearLayout.getChildAt(i)).getChildAt(0);
                view.setOnLongClickListener(null);
                view.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        ImageItemView view = (ImageItemView) v;
                        if (!view.getTag().equals(SYNCABLE)) {
                            count += 1;
                            countSessions.setTitle(String.valueOf(count));
                            view.getSideImage().setImageResource(R.drawable.accept_icon);
                            view.setTag(SYNCABLE);

                        } else {
                            count -= 1;
                            countSessions.setTitle(String.valueOf(count));
                            if (view.getSession() instanceof FTPSession)
                                view.getSideImage().setImageResource(R.drawable.ftp_icon);
                            else
                                view.getSideImage().setImageResource(R.drawable.sftp_icon);
                            view.setTag(ORIGINAL);
                        }
                    }

                });
                ((GridLayout) sessionsContentLinearLayout.getChildAt(i)).getChildAt(1).setVisibility(View.GONE);
                view.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                view.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;

            }
        } else if (item.getItemId() == R.id.delete_sessions) {
            ArrayList<Session> toDelete = getSelectedSessions();
            exitSelection();
            for (Session session : toDelete) {
                sessionDataSource.deleteSession(session);

                SharedPreferences preferences = getSharedPreferences(SessionContract.PREFERENCES, Context.MODE_PRIVATE);
                boolean generalPrefTurnOnSynchronisation = preferences.getBoolean(SessionContract.TURN_ON_SYNCHRONISATION, true);
                if (session.isTurnOnSynchronisation() && generalPrefTurnOnSynchronisation) {

                    for (String day : Utils.stringToList(session.getDays(), ", ")) {

                        int alarmID = session.getId() * 1000;

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

                        if (session instanceof FTPSession) {
                            b.putParcelable(FileSystemActivity.SESSION_STRING, (FTPSession) session);
                            b.putInt(SessionContract.TYPE, SessionContract.FTP);
                        } else {

                            b.putParcelable(FileSystemActivity.SESSION_STRING, (SFTPSession) session);
                            b.putInt(SessionContract.TYPE, SessionContract.SFTP);
                        }

                        b.putInt(SessionContract.TYPE, SessionContract.FTP);
                        intent.putExtra(SessionContract.CURRENT_SESSION, b);
                        PendingIntent alarmIntent = PendingIntent.getBroadcast(this.getApplicationContext(), alarmID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                        AlarmManager alarmMgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
                        alarmMgr.cancel(alarmIntent);
                    }
                }
                update();

            }
        } else if (item.getItemId() == R.id.sync_selected_sessions) {
            ArrayList<Session> toSync = getSelectedSessions();
            exitSelection();
            for (Session session : toSync) {
                Bundle bundle = new Bundle();
                if (session instanceof FTPSession) {
                    bundle.putParcelable(FileSystemActivity.SESSION_STRING, (FTPSession) session);
                    bundle.putInt(SessionContract.TYPE, SessionContract.FTP);
                } else if (session instanceof SFTPSession) {
                    bundle.putParcelable(FileSystemActivity.SESSION_STRING, (SFTPSession) session);
                    bundle.putInt(SessionContract.TYPE, SessionContract.SFTP);
                }
                Intent syncService = new Intent(this, SynchronisationService.class);
                syncService.putExtra(SessionContract.CURRENT_SESSION, bundle);
                startService(syncService);
                Toast.makeText(this, "Synchronisation started", Toast.LENGTH_SHORT);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (REQ_CODE == requestCode)
            update();
    }

    private void update() {
        sessionsContentLinearLayout.removeAllViews();
        sessionDataSource = new SessionDataSource(this);
        ArrayList<Session> savedSessions = sessionDataSource.getSessions();

        if (savedSessions.size() > 0) {
            noSessionTextView.setVisibility(View.INVISIBLE);
            for (final Session session : savedSessions) {
                GridLayout viewLayout = new GridLayout(this);
                viewLayout.setColumnCount(2);
                viewLayout.setRowCount(1);

                //viewLayout.setLayoutParams(params);

                final ImageItemView fileItemDisplay = new ImageItemView(this, true);
                if (session instanceof FTPSession)
                    fileItemDisplay.getSideImage().setImageResource(R.drawable.ftp_icon);
                else if (session instanceof SFTPSession)
                    fileItemDisplay.getSideImage().setImageResource(R.drawable.sftp_icon);
                fileItemDisplay.setTag(ORIGINAL);
                fileItemDisplay.setSession(session);
                fileItemDisplay.getMainTextView().setText(session.getHost());
                fileItemDisplay.getSubTextView().setText(session.getUser());
                fileItemDisplay.setOnClickListener(new SessionItemOnClickListener());
                fileItemDisplay.setOnLongClickListener(new SessionItemOnLongClickListener());

                final float scale = getResources().getDisplayMetrics().density;
                int height_pixels = (int) (55 * scale + 0.5f);
                int widthPixels = (int) (300 * scale + 0.5f);
                fileItemDisplay.setLayoutParams(new LinearLayout.LayoutParams(widthPixels,
                        height_pixels));


                ImageButton button = new ImageButton(this);
                button.setImageResource(R.drawable.ic_settings_black);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(SessionsActivity.this, SessionSettingsActivity.class);
                        Bundle b = new Bundle();
                        if (session instanceof FTPSession) {
                            b.putParcelable(FileSystemActivity.SESSION_STRING, (FTPSession) session);
                            b.putInt(SessionContract.TYPE, SessionContract.FTP);
                            intent.putExtra(SessionContract.CURRENT_SESSION, b);
                        } else if (session instanceof SFTPSession) {
                            b.putParcelable(FileSystemActivity.SESSION_STRING, (SFTPSession) session);
                            b.putInt(SessionContract.TYPE, SessionContract.SFTP);
                            intent.putExtra(SessionContract.CURRENT_SESSION, b);
                        }
                        startActivityForResult(intent, REQ_CODE);
                    }
                });
                button.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                button.setScaleX((float) 0.7);
                button.setScaleY((float) 0.7);
                button.setPadding(10, 0, 0, 5);
                button.setColorFilter(getResources().getColor(R.color.dark_grey));
                viewLayout.addView(fileItemDisplay, 0);
                viewLayout.addView(button, 1);
                sessionsContentLinearLayout.addView(viewLayout);
            }
        } else {
            noSessionTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (performingSelectingTask) {
            exitSelection();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(NAVIGATION_POSITION, navigationPosition);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sessions_menu, menu);
        this.menu = menu;
        addSessions = menu.getItem(0);
        syncSessions = menu.getItem(1);
        countSessions = menu.getItem(2);
        deleteSessions = menu.getItem(3);
        syncSelectedSessions = menu.getItem(4);
        return super.onCreateOptionsMenu(menu);
    }

    public class SessionItemOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            ImageItemView display = (ImageItemView) v;
            Intent intent = new Intent(SessionsActivity.this, FileSystemActivity.class);
            if (display.getSession() instanceof FTPSession) {
                Bundle b = new Bundle();
                b.putParcelable(FileSystemActivity.SESSION_STRING, (FTPSession) display.getSession());
                b.putInt(SessionContract.TYPE, SessionContract.FTP);
                intent.putExtra(SessionContract.CURRENT_SESSION, b);

            } else if (display.getSession() instanceof SFTPSession) {
                Bundle b = new Bundle();
                b.putParcelable(FileSystemActivity.SESSION_STRING, (SFTPSession) display.getSession());
                b.putInt(SessionContract.TYPE, SessionContract.SFTP);
                intent.putExtra(SessionContract.CURRENT_SESSION, b);
            }

            startActivityForResult(intent, REQ_CODE);

        }
    }

    public class SessionItemOnLongClickListener implements View.OnLongClickListener {

        @Override
        public boolean onLongClick(View v) {
            ImageItemView view = (ImageItemView) v;
            performingSelectingTask = true;
            drawerToggle.setDrawerIndicatorEnabled(false);
            sessionsGoBackImageButton.setVisibility(View.VISIBLE);


            addSessions.setVisible(false);
            syncSessions.setVisible(false);
            deleteSessions.setVisible(true);
            countSessions.setVisible(true);
            count += 1;
            view.getSideImage().setImageResource(R.drawable.remove_icon);
            view.setTag(REMOVEABLE);
            countSessions.setTitle(String.valueOf(count));

            int childCount = sessionsContentLinearLayout.getChildCount();
            for (int i = 0; i < childCount; i++) {
                ImageItemView item = (ImageItemView) ((GridLayout) sessionsContentLinearLayout.getChildAt(i)).getChildAt(0);
                item.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        ImageItemView view = (ImageItemView) v;
                        if (!view.getTag().equals(REMOVEABLE)) {
                            count += 1;
                            countSessions.setTitle(String.valueOf(count));
                            view.getSideImage().setImageResource(R.drawable.remove_icon);
                            view.setTag(REMOVEABLE);

                        } else {
                            count -= 1;
                            countSessions.setTitle(String.valueOf(count));
                            if (view.getSession() instanceof FTPSession)
                                view.getSideImage().setImageResource(R.drawable.ftp_icon);
                            else
                                view.getSideImage().setImageResource(R.drawable.sftp_icon);
                            view.setTag(ORIGINAL);
                        }
                    }
                });
                item.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        exitSelection();
                        return true;
                    }
                });
                ((GridLayout) sessionsContentLinearLayout.getChildAt(i)).getChildAt(1).setVisibility(View.GONE);
                item.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                item.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;

            }


            performingSelectingTask = true;
            return true;
        }
    }

    public void exitSelection() {
        count = 0;
        performingSelectingTask = false;
        sessionsGoBackImageButton.setVisibility(View.GONE);
        drawerToggle.setDrawerIndicatorEnabled(true);
        addSessions.setVisible(true);
        syncSessions.setVisible(true);
        deleteSessions.setVisible(false);
        countSessions.setVisible(false);
        syncSelectedSessions.setVisible(false);


        int childCount = sessionsContentLinearLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageItemView item = (ImageItemView) ((GridLayout) sessionsContentLinearLayout.getChildAt(i)).getChildAt(0);
            if (item.getSession() instanceof FTPSession)
                item.getSideImage().setImageResource(R.drawable.ftp_icon);
            else if (item.getSession() instanceof SFTPSession)
                item.getSideImage().setImageResource(R.drawable.sftp_icon);
            item.setTag(ORIGINAL);
            item.setOnClickListener(new SessionItemOnClickListener());
            item.setOnLongClickListener(new SessionItemOnLongClickListener());

            ((GridLayout) sessionsContentLinearLayout.getChildAt(i)).getChildAt(1).setVisibility(View.VISIBLE);

            final float scale = getResources().getDisplayMetrics().density;
            int height_pixels = (int) (55 * scale + 0.5f);
            int widthPixels = (int) (300 * scale + 0.5f);
            item.getLayoutParams().height = height_pixels;
            item.getLayoutParams().width = widthPixels;
        }
    }

    public ArrayList<Session> getSelectedSessions() {
        ArrayList<Session> selected = new ArrayList<>();
        int childCount = sessionsContentLinearLayout.getChildCount();

        for (int i = 0; i < childCount; i++) {
            ImageItemView item = (ImageItemView) ((GridLayout) sessionsContentLinearLayout.getChildAt(i)).getChildAt(0);

            if (item.getTag().equals(REMOVEABLE)) {
                selected.add(item.getSession());
            }

        }
        return selected;
    }
}
