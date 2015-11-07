package me.bolanleonifade.remoteconnection;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.vfs2.FileObject;

/**
 * Created by Ester on 19/06/2015.
 */
public class ImageItemView extends LinearLayout {

    private ImageView sideImage;
    private TextView mainTextView;
    private TextView subTextView;
    private FileObject fileObject;
    private Report report;


    private Session session;
    private CheckBox checkItem;

    public ImageItemView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        TypedArray array = context.obtainStyledAttributes(attributeSet,
                R.styleable.view_attrs, 0, 0);
        boolean twoLayers = array.getBoolean(R.styleable.view_attrs_two_layers, false);
        array.recycle();

        setUpView(context, twoLayers);
    }

    public ImageItemView(Context context, boolean twoLayers) {
        super(context);
        //Set up grid layout
        setUpView(context, twoLayers);
    }

    private void setUpView (Context context, boolean twoLayers) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        int pixels = (int) (55 * scale + 0.5f);
        setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                pixels));
        setPadding(0, (int) ( 5 * scale + 0.5f), 0, 0);
        //setBackgroundColor(context.getResources().getColor(R.color.material_deep_teal_500));

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.image_item_view_layout, this, true);

        {
            checkItem = (CheckBox) getChildAt(0);
            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(checkItem.getLayoutParams());
            params.setMargins((int) (10 * scale + 0.5f), (int) (10 * scale + 0.5f), 0, 0);
            checkItem.setLayoutParams(params);
            checkItem.setGravity(Gravity.CENTER_VERTICAL);
            checkItem.setVisibility(GONE);
        }
        {
            sideImage = (ImageView) getChildAt(1);

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(sideImage.getLayoutParams());
            params.setMargins((int) (10 * scale + 0.5f), 0 , 0, 0);
            sideImage.setLayoutParams(params);
        }

        LinearLayout fileDetails = (LinearLayout) getChildAt(2);
        {
            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            fileDetails.setLayoutParams(params);
        }
        if (twoLayers) {

            mainTextView = (TextView) fileDetails.getChildAt(0);
            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LayoutParams.WRAP_CONTENT);
            mainTextView.setTypeface(null, Typeface.BOLD);
            mainTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            mainTextView.setPadding((int) (10 * scale + 0.5f), 0, 0, 0);
            mainTextView.setLayoutParams(params);


            subTextView = (TextView) fileDetails.getChildAt(1);
            params =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LayoutParams.WRAP_CONTENT);

            subTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            subTextView.setPadding((int) (10 * scale + 0.5f), 0 , 0, 0);
            subTextView.setVisibility(VISIBLE);
            subTextView.setLayoutParams(params);
        } else {

            mainTextView = (TextView) fileDetails.getChildAt(0);
            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

            mainTextView.setLayoutParams(params);
            mainTextView.setPadding((int) (10 * scale + 0.5f), 0, 0, 0);
            mainTextView.setTypeface(null, Typeface.BOLD);
            mainTextView.setGravity(Gravity.CENTER_VERTICAL);
            mainTextView.setLines(2);
            mainTextView.setEllipsize(TextUtils.TruncateAt.END);

            subTextView = (TextView) fileDetails.getChildAt(1);
            subTextView.setVisibility(GONE);
        }
    }

    public ImageView getSideImage() {
        return sideImage;
    }

    public void setSideImage(ImageView sideImage) {
        this.sideImage = sideImage;
    }

    public TextView getMainTextView() {
        return mainTextView;
    }

    public void setMainTextView(TextView mainTextView) {
        this.mainTextView = mainTextView;
    }

    public TextView getSubTextView() {
        return subTextView;
    }

    public void setSubTextView(TextView subTextView) {
        this.subTextView = subTextView;
    }

    public FileObject getFileObject() {
        return fileObject;
    }

    public void setFileObject(FileObject fileObject) {
        this.fileObject = fileObject;
    }

    public CheckBox getCheckItem() {
        return checkItem;
    }

    public void setCheckItem(CheckBox checkItem) {
        this.checkItem = checkItem;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }
}
