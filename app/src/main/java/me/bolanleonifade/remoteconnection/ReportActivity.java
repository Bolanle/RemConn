package me.bolanleonifade.remoteconnection;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class ReportActivity extends AppCompatActivity {

    public static final String REPORT_STRING = "REPORT";
    public TextView reportToolbarTextView;


    private TextView reportGeneralUserValueTextView;
    private TextView reportGeneralHostValueTextView;
    private TextView reportGeneralLocalDirectoryValueTextView;
    private TextView reportGeneralRemoteDirectoryValueTextView;
    private TextView reportGeneralTimeValueTextView;

    private TextView reportFilesUploadedTextView;
    private TextView reportFilesDownloadedTextView;
    private TextView reportErrorTextView;
    private ImageButton reportGoBackImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        getIntent().setExtrasClassLoader(Report.class.getClassLoader());
        Report report = getIntent().getParcelableExtra(REPORT_STRING);

        reportToolbarTextView = (TextView) findViewById(R.id.report_toolbar_title);
        reportToolbarTextView.setText("Report: " + report.getHost());

        reportGeneralUserValueTextView = (TextView) findViewById(R.id.report_general_user_value_text_view);
        reportGeneralUserValueTextView.setText(report.getUser());

        reportGeneralHostValueTextView = (TextView) findViewById(R.id.report_general_host_value_text_view);
        reportGeneralHostValueTextView.setText(report.getHost());

        reportGeneralLocalDirectoryValueTextView = (TextView) findViewById(R.id.report_general_local_dir_value_text_view);

        if (report.getLocalDirectory().contains("storage/emulated/0/")) {
            reportGeneralLocalDirectoryValueTextView.setText(report.getLocalDirectory().replace("storage/emulated/0/", ""));
        }else {
            reportGeneralLocalDirectoryValueTextView.setText(report.getLocalDirectory().replace("/storage/emulated/0/", ""));
        }

        reportGeneralRemoteDirectoryValueTextView = (TextView) findViewById(R.id.report_general_remote_dir_value_text_view);
        reportGeneralRemoteDirectoryValueTextView.setText(report.getRemoteDirectory());

        reportGeneralTimeValueTextView = (TextView) findViewById(R.id.report_general_time_value_text_view);
        reportGeneralTimeValueTextView.setText(report.getTimeStamp());

        reportFilesUploadedTextView = (TextView) findViewById(R.id.report_files_uploaded_text_view);
        String filesUploadedFormatted = report.getFilesUploaded().replace(",", "\n");
        reportFilesUploadedTextView.setText(filesUploadedFormatted);

        reportFilesDownloadedTextView = (TextView) findViewById(R.id.report_files_downloaded_text_view);
        String filesDownloadedFormatted = report.getFilesDownloaded().replace(",", "\n");
        reportFilesDownloadedTextView.setText(filesDownloadedFormatted);

        reportErrorTextView = (TextView) findViewById(R.id.report_error_text_view);
        reportErrorTextView.setText(report.getError());

        reportGoBackImageButton = (ImageButton) findViewById(R.id.report_go_back);
        reportGoBackImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_report, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
            if (expandOrContract.getId() == R.id.report_general_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.report_general_views_layout);
                layout.setVisibility(View.VISIBLE);
            } else if (expandOrContract.getId() == R.id.report_files_uploaded_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.report_files_uploaded_views);
                layout.setVisibility(View.VISIBLE);
            } else if (expandOrContract.getId() == R.id.report_files_downloaded_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.report_files_downloaded_views);
                layout.setVisibility(View.VISIBLE);
            } else if (expandOrContract.getId() == R.id.report_error_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.report_error_views);
                layout.setVisibility(View.VISIBLE);
            }

        } else {
            expandOrContract.setImageDrawable(expandImage);
            expandOrContract.setTag(getResources().getString(R.string.expand));
            if (expandOrContract.getId() == R.id.general_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.general_views_layout);
                layout.setVisibility(View.GONE);
            } else if (expandOrContract.getId() == R.id.report_files_uploaded_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.report_files_uploaded_views);
                layout.setVisibility(View.GONE);
            } else if (expandOrContract.getId() == R.id.report_files_downloaded_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.report_files_downloaded_views);
                layout.setVisibility(View.GONE);
            } else if (expandOrContract.getId() == R.id.report_error_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.report_error_views);
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
            if (expandOrContract.getId() == R.id.report_general_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.report_general_views_layout);
                layout.setVisibility(View.VISIBLE);
            } else if (expandOrContract.getId() == R.id.report_files_uploaded_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.report_files_uploaded_views);
                layout.setVisibility(View.VISIBLE);
            } else if (expandOrContract.getId() == R.id.report_files_downloaded_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.report_files_downloaded_views);
                layout.setVisibility(View.VISIBLE);
            } else if (expandOrContract.getId() == R.id.report_error_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.report_error_views);
                layout.setVisibility(View.VISIBLE);
            }

        } else {
            expandOrContract.setImageDrawable(expandImage);
            expandOrContract.setTag(getResources().getString(R.string.expand));
            if (expandOrContract.getId() == R.id.general_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.general_views_layout);
                layout.setVisibility(View.GONE);
            } else if (expandOrContract.getId() == R.id.report_files_uploaded_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.report_files_uploaded_views);
                layout.setVisibility(View.GONE);
            } else if (expandOrContract.getId() == R.id.report_files_downloaded_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.report_files_downloaded_views);
                layout.setVisibility(View.GONE);
            } else if (expandOrContract.getId() == R.id.report_error_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.report_error_views);
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
            if (expandOrContract.getId() == R.id.report_general_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.report_general_views_layout);
                layout.setVisibility(View.VISIBLE);
            } else if (expandOrContract.getId() == R.id.report_files_uploaded_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.report_files_uploaded_views);
                layout.setVisibility(View.VISIBLE);
            } else if (expandOrContract.getId() == R.id.report_files_downloaded_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.report_files_downloaded_views);
                layout.setVisibility(View.VISIBLE);
            } else if (expandOrContract.getId() == R.id.report_error_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.report_error_views);
                layout.setVisibility(View.VISIBLE);
            }

        } else {
            expandOrContract.setImageDrawable(expandImage);
            expandOrContract.setTag(getResources().getString(R.string.expand));
            if (expandOrContract.getId() == R.id.general_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.general_views_layout);
                layout.setVisibility(View.GONE);
            } else if (expandOrContract.getId() == R.id.report_files_uploaded_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.report_files_uploaded_views);
                layout.setVisibility(View.GONE);
            } else if (expandOrContract.getId() == R.id.report_files_downloaded_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.report_files_downloaded_views);
                layout.setVisibility(View.GONE);
            } else if (expandOrContract.getId() == R.id.report_error_image_button) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.report_error_views);
                layout.setVisibility(View.GONE);
            }
        }
    }
}
