package me.bolanleonifade.remoteconnection;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;


public class SettingsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final long DRAWER_CLOSE_DELAY_MS = 250;
    private static final String NAVIGATION_POSITION = "navigationPosition";
    private static final int MENU_ITEMS = 2;

    private final Handler drawerActionHandler = new Handler();
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private int navigationPosition;

    private EditText localDirectoryEditText;
    private EditText remoteDirectoryEditText;
    private Switch hiddenFiles;
    private Switch turnOnSynchronisation;
    private Spinner deleteWhenSpinner;

    private Button saveButton;
    public static int DELETEID = 555;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        drawerLayout = (DrawerLayout) findViewById(R.id.nav_drawer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);

        if (null == savedInstanceState) {
            navigationPosition = R.id.drawer_settings;
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
        SharedPreferences preferences = getSharedPreferences(SessionContract.PREFERENCES, Context.MODE_PRIVATE);
        localDirectoryEditText = (EditText) findViewById(R.id.settings_general_local_directory_edit_text);
        localDirectoryEditText.setText(preferences.getString(SessionContract.PREFERRED_LOCAL_DIR, "").replace("/storage/emulated/0", ""));

        remoteDirectoryEditText = (EditText) findViewById(R.id.settings_general_remote_directory_edit_text);
        remoteDirectoryEditText.setText(preferences.getString(SessionContract.PREFERRED_REMOTE_DIR, ""));

        hiddenFiles = (Switch) findViewById(R.id.settings_hidden_files_toggle_button);
        hiddenFiles.setChecked(preferences.getBoolean(SessionContract.PREFERRED_HIDDEN_FILE_DISPLAY, true));

        turnOnSynchronisation = (Switch) findViewById(R.id.settings_synchronisation_turn_on_toggle_button);
        turnOnSynchronisation.setChecked(preferences.getBoolean(SessionContract.TURN_ON_SYNCHRONISATION, true));

        deleteWhenSpinner = (Spinner) findViewById(R.id.settings_synchronisation_delete_when_spinner);
        deleteWhenSpinner.setSelection(preferences.getInt(SessionContract.PREFERRED_DELETE_WHEN, 0));

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.delete_periods,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deleteWhenSpinner.setAdapter(adapter);

        saveButton = (Button) findViewById(R.id.settings_save_settings);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePreferences(v);
            }
        });
    }

    private void navigate(final int itemId) {
        Intent intent;
        if(itemId  == R.id.drawer_sessions ) {
            intent = new Intent(this , SessionsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        }else if (itemId == R.id.drawer_reports) {
            intent = new Intent(this , ReportsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        }else if (itemId == R.id.drawer_settings) {
            //do nothing
        }else if (itemId == R.id.drawer_about) {
            intent = new Intent(this, AboutActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        }
        finish();
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
            startActivity(intent);

        }else if(item.getItemId() == R.id.sync_sessions) {
            //Launch service to sync
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
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
        inflater.inflate(R.menu.menu_settings, menu);

        return super.onCreateOptionsMenu(menu);
    }


    public void savePreferences(View view) {
        SharedPreferences preferences = getSharedPreferences(SessionContract.PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        String directory = remoteDirectoryEditText.getText().toString();
        if(directory.isEmpty())
            editor.putString(SessionContract.PREFERRED_REMOTE_DIR, null);
        else {
            directory = !directory.startsWith("/")
                    ? "/" + directory : directory;
            editor.putString(SessionContract.PREFERRED_REMOTE_DIR, directory);
        }

        directory =  localDirectoryEditText.getText().toString();
        if(directory.isEmpty())
            editor.putString(SessionContract.PREFERRED_LOCAL_DIR, null);
        else {
            directory = directory.startsWith("/")
                    ? directory.replaceFirst("/", "") : directory;
            directory = "/storage/emulated/0/" + directory;
            editor.putString(SessionContract.PREFERRED_LOCAL_DIR, directory);
        }
        editor.putBoolean(SessionContract.PREFERRED_HIDDEN_FILE_DISPLAY, hiddenFiles.isChecked());
        editor.putBoolean(SessionContract.TURN_ON_SYNCHRONISATION, turnOnSynchronisation.isChecked());
        editor.putInt(SessionContract.PREFERRED_DELETE_WHEN, deleteWhenSpinner.getSelectedItemPosition());
        editor.commit();
        //check spinner;
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(DeletionReceiver.ACTION_DELETE);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this.getApplicationContext(), DELETEID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        if (deleteWhenSpinner.getSelectedItemPosition() == 0) {  //Every week
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
            alarmManager.cancel(alarmIntent);
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    1000 * 60 * 60 * 24 * 7, alarmIntent);
        }else if(deleteWhenSpinner.getSelectedItemPosition() == 1) { //Every 2 weeks
            calendar.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
            alarmManager.cancel(alarmIntent);
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    1000 * 60 * 60 * 24 * 7 * 30, alarmIntent);
            alarmIntent = PendingIntent.getBroadcast(this, DELETEID + 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            calendar.set(Calendar.DAY_OF_MONTH, (Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + 14) % 30);
            alarmManager.cancel(alarmIntent);
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    1000 * 60 * 60 * 24 * 7 * 30, alarmIntent);
        }else if (deleteWhenSpinner.getSelectedItemPosition() == 2) { //Every month
            calendar.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
            alarmManager.cancel(alarmIntent);
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    1000 * 60 * 60 * 24 * 7 * 30, alarmIntent);
        }else if (deleteWhenSpinner.getSelectedItemPosition() == 3) { //Every 6 months
            calendar.set(Calendar.MONTH, (Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + 6) % 12);
            alarmManager.cancel(alarmIntent);
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    1000 * 60 * 60 * 24 * 7 * 30 * 6, alarmIntent);
        }

        Toast.makeText(this, "Settings Saved", Toast.LENGTH_SHORT).show();
    }

    public void expandOrContractRelativeLayout(View view) {
        RelativeLayout relativeLayout =  (RelativeLayout) view;
        ImageButton expandOrContract = (ImageButton) relativeLayout.getChildAt(1);
        Drawable expandImage = getResources().getDrawable(R.drawable.ic_expand_more_black);
        Drawable contractImage = getResources().getDrawable(R.drawable.ic_expand_less_black);

        //Use Icons to RepresentState
        if (expandOrContract.getTag().toString().equals(getResources().getString(R.string.expand))) { //if Expand, then contract
            expandOrContract.setImageDrawable(contractImage);
            expandOrContract.setTag(getResources().getString(R.string.contract));
            if (expandOrContract.getId() == R.id.settings_general_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.settings_general_views_layout);
                layout.setVisibility(View.VISIBLE);
            } else if (expandOrContract.getId() == R.id.settings_synchronisation_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.settings_synchronisation_views);
                layout.setVisibility(View.VISIBLE);
            }

        } else {
            
            expandOrContract.setImageDrawable(expandImage);
            expandOrContract.setTag(getResources().getString(R.string.expand));
            if (expandOrContract.getId() == R.id.settings_general_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.settings_general_views_layout);
               
                layout.setVisibility(View.GONE);
            } else if (expandOrContract.getId() == R.id.settings_synchronisation_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.settings_synchronisation_views);
               
                layout.setVisibility(View.GONE);
            }
        }
    }

    public void expandOrContractButton(View view) {
        ImageButton expandOrContract = (ImageButton) findViewById(view.getId());
        Drawable expandImage = getResources().getDrawable(R.drawable.ic_expand_more_black);
        Drawable contractImage = getResources().getDrawable(R.drawable.ic_expand_less_black);

        //Use Icons to RepresentState
        if (expandOrContract.getTag().toString().equals(getResources().getString(R.string.expand))) { //if Expand, then contract
            expandOrContract.setImageDrawable(contractImage);
            expandOrContract.setTag(getResources().getString(R.string.contract));
            if (expandOrContract.getId() == R.id.settings_general_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.settings_general_views_layout);
                layout.setVisibility(View.VISIBLE);
            } else if (expandOrContract.getId() == R.id.settings_synchronisation_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.settings_synchronisation_views);
                layout.setVisibility(View.VISIBLE);
            }

        } else {
            
            expandOrContract.setImageDrawable(expandImage);
            expandOrContract.setTag(getResources().getString(R.string.expand));
            if (expandOrContract.getId() == R.id.settings_general_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.settings_general_views_layout);
               
                layout.setVisibility(View.GONE);
            } else if (expandOrContract.getId() == R.id.settings_synchronisation_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.settings_synchronisation_views);
               
                layout.setVisibility(View.GONE);
            }
        }
    }

    public void expandOrContractTextView(View view) {
        TextView textView = (TextView) view;

        RelativeLayout relativeLayout =  (RelativeLayout) textView.getParent();
        ImageButton expandOrContract = (ImageButton) relativeLayout.getChildAt(1);
        Drawable expandImage = getResources().getDrawable(R.drawable.ic_expand_more_black);
        Drawable contractImage = getResources().getDrawable(R.drawable.ic_expand_less_black);

        //Use Icons to RepresentState
        if (expandOrContract.getTag().toString().equals(getResources().getString(R.string.expand))) { //if Expand, then contract
            expandOrContract.setImageDrawable(contractImage);
            expandOrContract.setTag(getResources().getString(R.string.contract));
            if (expandOrContract.getId() == R.id.settings_general_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.settings_general_views_layout);
                layout.setVisibility(View.VISIBLE);
            } else if (expandOrContract.getId() == R.id.settings_synchronisation_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.settings_synchronisation_views);
                layout.setVisibility(View.VISIBLE);
            }

        } else {
            expandOrContract.setImageDrawable(expandImage);
            expandOrContract.setTag(getResources().getString(R.string.expand));
            
            if (expandOrContract.getId() == R.id.settings_general_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.settings_general_views_layout);
               
                layout.setVisibility(View.GONE);
            } else if (expandOrContract.getId() == R.id.settings_synchronisation_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.settings_synchronisation_views);
               
                layout.setVisibility(View.GONE);
            }
        }
    }
}
