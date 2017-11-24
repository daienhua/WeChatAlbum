package com.david.album;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.david.album.view.ViewPagerFixed;

import java.util.ArrayList;

/**
 * 预览图片
 */
public class PreviewActivity extends Activity {
    RelativeLayout mBack;
    FrameLayout mHeader;
    ViewPagerFixed mPager;
    TextView mComplete;
    FrameLayout mFooter;
    TextView mImagePosition;
    CheckBox mSelected;

    private ArrayList<Image> mImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_preview);

        mBack = (RelativeLayout) findViewById(R.id.back);
        mHeader = (FrameLayout) findViewById(R.id.header);
        mPager = (ViewPagerFixed) findViewById(R.id.pager);
        mComplete = (TextView) findViewById(R.id.complete);
        mFooter = (FrameLayout) findViewById(R.id.footer);

        mImages =
                (ArrayList<Image>) getIntent().getExtras().getSerializable(PhotoSelectActivity.PREVIEW_LIST);

        mImagePosition = (TextView) findViewById(R.id.imagePosition);
        mSelected = (CheckBox) findViewById(R.id.selected);
        if (mImages != null) {
            mImagePosition.setText(1 + "/" + mImages.size());
            mComplete.setText(getResources().getString(R.string.album_complete_with_count, mImages.size() + ""));
        }

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        PreviewAdapter adapter = new PreviewAdapter(this, mImages);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (mImages != null) {
                    mImagePosition.setText(position + 1 + "/" + mImages.size());
                }
                mSelected.setChecked(mImages.get(position).isSelected());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mPager.setAdapter(adapter);

        mSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mImages.get(mPager.getCurrentItem()).setIsSelected(isChecked);
                //显示已经选择的图片数量
                showSelectedImageSize();
            }
        });

        mComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishing(true);
                finish();
            }
        });
    }

    /**
     * 显示已经选择的图片数量
     */
    private void showSelectedImageSize() {
        if (mImages != null) {
            int selectedSize = 0;
            for (Image image : mImages) {
                if (image.isSelected()) {
                    selectedSize++;
                }
            }
            if (selectedSize == 0) {
                mComplete.setText(getResources().getString(R.string.album_complete));
                mComplete.setEnabled(false);
                mComplete.setTextColor(Color.parseColor("#999999"));
            } else {
                mComplete.setText(getResources().getString(R.string.album_complete_with_count, selectedSize + ""));
                mComplete.setEnabled(true);
                mComplete.setTextColor(Color.parseColor("#ffffff"));
            }
        }
    }


    @Override
    public void onBackPressed() {
        finishing(false);
        super.onBackPressed();
    }

    public void finishing(boolean complete) {
        ArrayList<Image> images = new ArrayList<>();
        for (Image image : mImages) {
            if (image.isSelected()) {
                images.add(image);
            }
        }
        Intent intent = new Intent();
        intent.putExtra(PhotoSelectActivity.PREVIEW_COMPLETE, complete);
        intent.putExtra(PhotoSelectActivity.PREVIEW_LIST, images);
        setResult(Activity.RESULT_OK, intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
