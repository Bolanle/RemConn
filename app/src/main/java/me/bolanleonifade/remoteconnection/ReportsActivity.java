package me.bolanleonifade.remoteconnection;

import android.content.Intent;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class ReportsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final long DRAWER_CLOSE_DELAY_MS = 250;
    private static final String NAVIGATION_POSITION = "navigationPosition";
    private static final int MENU_ITEMS = 2;

    private final Handler drawerActionHandler = new Handler();
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private int navigationPosition;

    private ReportDataSource reportDataSource;

    private LinearLayout reportsContentLinearLayout;
    private int count;
    private ImageButton reportsGoBackImageButton;
    private MenuItem deleteReports;
    private MenuItem countDeletingReports;
    private TextView noReportTextView;

    private String REMOVEABLE = "REMOVEABLE";
    private String ORIGINAL = "ORIGINAL";

    private boolean selecting;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        drawerLayout = (DrawerLayout) findViewById(R.id.nav_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.reports_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);

        if (null == savedInstanceState) {
            navigationPosition = R.id.drawer_reports;
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
        noReportTextView = (TextView) findViewById(R.id.reports_no_reports_view);
        reportDataSource = new ReportDataSource(this.getBaseContext());

        reportsContentLinearLayout = (LinearLayout) findViewById(R.id.reports_content);
        reportsGoBackImageButton = (ImageButton) findViewById(R.id.reports_go_back);
        reportsGoBackImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitSelection();
            }
        });
        getReports();
    }

    private void navigate(final int itemId) {
        Intent intent;
        if (itemId == R.id.drawer_sessions) {
            intent = new Intent(this, SessionsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);

        } else if (itemId == R.id.drawer_reports) {
            //do nothing
        } else if (itemId == R.id.drawer_settings) {
            intent = new Intent(this, SettingsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        } else if (itemId == R.id.drawer_about) {
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
        if (item.getItemId() == R.id.delete_selected_reports) {
            ArrayList<Report> reports = getSelectedReports();
            for (Report report : reports) {
                reportDataSource.deleteReport(report);
            }
            exitSelection();
            getReports();
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            if(selecting) {
                exitSelection();
            }else {
                super.onBackPressed();
            }
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
        inflater.inflate(R.menu.menu_reports, menu);

        countDeletingReports = menu.getItem(0);
        deleteReports = menu.getItem(1);
        return super.onCreateOptionsMenu(menu);
    }

    public void getReports() {
        reportsContentLinearLayout.removeAllViews();
        ArrayList<Report> reports = reportDataSource.getReports();

        if(reports.size() > 0 ) {

            noReportTextView.setVisibility(View.INVISIBLE);

            for (Report report : reports) {
                ImageItemView display = new ImageItemView(this, true);
                if (report.getStatus().equals("OK"))
                    display.getSideImage().setImageResource(R.drawable.sync_ok_icon);
                else
                    display.getSideImage().setImageResource(R.drawable.sync_error_icon);
                display.getMainTextView().setText(report.getUser() + "@" + report.getHost());
                display.getSubTextView().setText(report.getTimeStamp());
                display.setOnClickListener(new ReportOnClickListener());
                display.setOnLongClickListener(new ReportOnLongClickListener());
                display.setReport(report);
                display.setTag(ORIGINAL);

                reportsContentLinearLayout.addView(display);
            }
        }
        else {
            noReportTextView.setVisibility(View.VISIBLE);
        }
    }

    public class ReportOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            ImageItemView selectedReport = (ImageItemView) v;
            Intent intent = new Intent(ReportsActivity.this, ReportActivity.class);
            intent.putExtra(ReportActivity.REPORT_STRING, selectedReport.getReport());
            startActivity(intent);
        }
    }

    public class ReportOnLongClickListener implements View.OnLongClickListener {

        @Override
        public boolean onLongClick(View v) {
            ImageItemView selectedReport = (ImageItemView) v;
            count = 0;

            drawerToggle.setDrawerIndicatorEnabled(false);
            reportsGoBackImageButton.setVisibility(View.VISIBLE);
            deleteReports.setVisible(true);

            count += 1;
            countDeletingReports.setVisible(true);
            countDeletingReports.setTitle(String.valueOf(count));

            selectedReport.getSideImage().setImageResource(R.drawable.remove_icon);
            selectedReport.setTag(REMOVEABLE);

            int childCount = reportsContentLinearLayout.getChildCount();

            for (int i = 0; i < childCount; i++) {
                ImageItemView view = (ImageItemView) reportsContentLinearLayout.getChildAt(i);
                view.setOnLongClickListener(null);
                view.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        ImageItemView view = (ImageItemView) v;
                        if (!view.getTag().equals(REMOVEABLE)) {
                            count += 1;
                            countDeletingReports.setTitle(String.valueOf(count));
                            view.getSideImage().setImageResource(R.drawable.remove_icon);
                            view.setTag(REMOVEABLE);

                        } else {
                            count -= 1;
                            countDeletingReports.setTitle(String.valueOf(count));
                            if (view.getReport().getStatus().equals("OK"))
                                view.getSideImage().setImageResource(R.drawable.sync_ok_icon);
                            else
                                view.getSideImage().setImageResource(R.drawable.sync_error_icon);
                            view.setTag(ORIGINAL);
                        }
                    }

                });
            }
            return true;
        }
    }

    public void exitSelection() {
        count = 0;
        drawerToggle.setDrawerIndicatorEnabled(true);
        reportsGoBackImageButton.setVisibility(View.GONE);
        countDeletingReports.setVisible(false);
        deleteReports.setVisible(false);


        int childCount = reportsContentLinearLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageItemView view = (ImageItemView) reportsContentLinearLayout.getChildAt(i);
            view.setTag(ORIGINAL);
            if (view.getReport().getStatus().equals("OK"))
                view.getSideImage().setImageResource(R.drawable.sync_ok_icon);
            else
                view.getSideImage().setImageResource(R.drawable.sync_error_icon);
            view.setOnClickListener(new ReportOnClickListener());
            view.setOnLongClickListener(new ReportOnLongClickListener());


        }
    }

    public ArrayList<Report> getSelectedReports() {
        ArrayList<Report> selected = new ArrayList<>();
        int childCount = reportsContentLinearLayout.getChildCount();

        for (int i = 0; i < childCount; i++) {
            ImageItemView item = (ImageItemView) reportsContentLinearLayout.getChildAt(i);

            if (item.getTag().equals(REMOVEABLE)) {
                selected.add(item.getReport());
            }
        }
        return selected;
    }
}
