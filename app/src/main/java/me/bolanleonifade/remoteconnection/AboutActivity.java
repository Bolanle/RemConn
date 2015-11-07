package me.bolanleonifade.remoteconnection;

import android.content.Intent;
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
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class AboutActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final long DRAWER_CLOSE_DELAY_MS = 250;
    private static final String NAVIGATION_POSITION = "navigationPosition";
    private static final int MENU_ITEMS = 2;

    private final Handler drawerActionHandler = new Handler();
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private int navigationPosition;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        drawerLayout = (DrawerLayout) findViewById(R.id.nav_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.about_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);

        if (null == savedInstanceState) {
            navigationPosition = R.id.drawer_about;
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
            intent = new Intent(this, SettingsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        }else if (itemId == R.id.drawer_about) {
            //do nothing
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
        inflater.inflate(R.menu.menu_about, menu);

        return super.onCreateOptionsMenu(menu);
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
            if (expandOrContract.getId() == R.id.about_general_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.about_general_views_layout);
                layout.setVisibility(View.VISIBLE);
            } else if (expandOrContract.getId() == R.id.about_authors_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.about_authors_views);
                layout.setVisibility(View.VISIBLE);
            }
        } else {
            expandOrContract.setImageDrawable(expandImage);
            expandOrContract.setTag(getResources().getString(R.string.expand));
            if (expandOrContract.getId() == R.id.about_general_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.about_general_views_layout);
                layout.setVisibility(View.GONE);
            } else if (expandOrContract.getId() == R.id.about_authors_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.about_authors_views);
                layout.setVisibility(View.GONE);
            } else if (expandOrContract.getId() == R.id.about_contact_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.about_contact_views);
                layout.setVisibility(View.GONE);
            } else if (expandOrContract.getId() == R.id.about_github_repo_layout_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.about_github_repo_layout_views);
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
            if (expandOrContract.getId() == R.id.about_general_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.about_general_views_layout);
                layout.setVisibility(View.VISIBLE);
            } else if (expandOrContract.getId() == R.id.about_authors_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.about_authors_views);
                layout.setVisibility(View.VISIBLE);
            } else if (expandOrContract.getId() == R.id.about_contact_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.about_contact_views);
                layout.setVisibility(View.VISIBLE);
            } else if (expandOrContract.getId() == R.id.about_github_repo_layout_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.about_github_repo_layout_views);
                layout.setVisibility(View.VISIBLE);
            }

        } else {
            expandOrContract.setImageDrawable(expandImage);
            expandOrContract.setTag(getResources().getString(R.string.expand));
            if (expandOrContract.getId() == R.id.about_general_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.about_general_views_layout);
                layout.setVisibility(View.GONE);
            } else if (expandOrContract.getId() == R.id.about_authors_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.about_authors_views);
                layout.setVisibility(View.GONE);
            } else if (expandOrContract.getId() == R.id.about_contact_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.about_contact_views);
                layout.setVisibility(View.GONE);
            } else if (expandOrContract.getId() == R.id.about_github_repo_layout_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.about_github_repo_layout_views);
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
            if (expandOrContract.getId() == R.id.about_general_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.about_general_views_layout);
                layout.setVisibility(View.VISIBLE);
            } else if (expandOrContract.getId() == R.id.about_authors_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.about_authors_views);
                layout.setVisibility(View.VISIBLE);
            } else if (expandOrContract.getId() == R.id.about_contact_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.about_contact_views);
                layout.setVisibility(View.VISIBLE);
            } else if (expandOrContract.getId() == R.id.about_github_repo_layout_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.about_github_repo_layout_views);
                layout.setVisibility(View.VISIBLE);
            }

        } else {
            expandOrContract.setImageDrawable(expandImage);
            expandOrContract.setTag(getResources().getString(R.string.expand));
            if (expandOrContract.getId() == R.id.about_general_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.about_general_views_layout);
                layout.setVisibility(View.GONE);
            } else if (expandOrContract.getId() == R.id.about_authors_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.about_authors_views);
                layout.setVisibility(View.GONE);
            } else if (expandOrContract.getId() == R.id.about_contact_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.about_contact_views);
                layout.setVisibility(View.GONE);
            } else if (expandOrContract.getId() == R.id.about_github_repo_layout_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.about_github_repo_layout_views);
                layout.setVisibility(View.GONE);
            }
        }
    }
}
