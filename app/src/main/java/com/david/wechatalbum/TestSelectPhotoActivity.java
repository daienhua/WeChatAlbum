package com.david.wechatalbum;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.david.album.Image;
import com.david.album.PhotoSelectActivity;
import com.david.album.utils.AndroidUtils;
import com.tbruyelle.rxpermissions.Permission;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.util.ArrayList;

import rx.functions.Action1;

public class TestSelectPhotoActivity extends AppCompatActivity implements TestSelectPhotoAdapter.ImageChangedListener, View.OnClickListener {

    private static final int REQUEST_CODE_SELECT_SINGLE = 100;
    private static final int REQUEST_CODE_SELECT_CROP = 101;
    private static final int REQUEST_CODE_SELECT_MULTI = 102;
    /****获取权限 begin****/
    private static final int SPLASH_SHOW_TIME = 2000;
    private ImageView mImageView;
    private RecyclerView mRecyclerView;
    private LinearLayout mLlImageLayout;
    private TextView mTvSelectOnePhoto;
    private TextView mTvSelectPhotoCrop;
    private TextView mTvSelectMultiPhoto;
    private ArrayList<Image> mPictures = new ArrayList<>();
    private Handler mHandler = new Handler();
    private Runnable mRunnable;

    private boolean isHasReadExternalPermission = false;
    private boolean isHasWriteExternalPermission = false;

    private boolean isCheckReadExternalPermission = false;
    private boolean isCheckWriteExternalPermission = false;

    private boolean isHandleStorage = false;

    /****获取权限 end****/

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTvSelectOnePhoto = (TextView) findViewById(R.id.tv_select);
        mTvSelectOnePhoto.setOnClickListener(this);
        mTvSelectMultiPhoto = (TextView) findViewById(R.id.tv_select_multi);
        mTvSelectMultiPhoto.setOnClickListener(this);
        mTvSelectPhotoCrop = (TextView) findViewById(R.id.tv_select_crop);
        mTvSelectPhotoCrop.setOnClickListener(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_recyclerView);
        mImageView = (ImageView) findViewById(R.id.iv_imageView);
        mLlImageLayout = (LinearLayout) findViewById(R.id.ll_imageLayout);

        showPermisionCheckDialog();

        initViewData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isCheckReadExternalPermission && isCheckWriteExternalPermission
                && isHandleStorage) {
            isCheckReadExternalPermission = false;
            isCheckWriteExternalPermission = false;
            isHandleStorage = false;
            showPermisionCheckDialog();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
        }
    }

    private void initViewData() {
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerView.addItemDecoration(
                new GridSpacingItemDecoration(3, AndroidUtils.dp2px(this, 10), true));
        mRecyclerView.setAdapter(new TestSelectPhotoAdapter(this, mPictures, 9));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQUEST_CODE_SELECT_SINGLE:
            case REQUEST_CODE_SELECT_CROP: {
                if (data != null) {
                    Image image = (Image) data.getExtras().getSerializable(PhotoSelectActivity.EXTRA_RESULT);
                    if (image != null) {
                        mImageView.setVisibility(View.VISIBLE);
                        mLlImageLayout.setVisibility(View.GONE);
                        Glide.with(this).load(image.getFilePath()).placeholder(R.drawable.ic_album_image_default).fitCenter().centerCrop().into(mImageView);
                    }
                }
                break;
            }
            case REQUEST_CODE_SELECT_MULTI: {
                if (data != null) {
                    ArrayList<Image> images = (ArrayList<Image>) data.getExtras().getSerializable(PhotoSelectActivity.EXTRA_RESULT);
                    if (images != null && images.size() != 0) {
                        mImageView.setVisibility(View.GONE);
                        mLlImageLayout.setVisibility(View.VISIBLE);
                        mPictures.clear();
                        mPictures.addAll(images);
                        mRecyclerView.getAdapter().notifyDataSetChanged();
                    }
                }
                break;
            }
        }
    }

    /**
     * 选择单张图片
     */
    private void selectSinglePhoto() {
        Intent intent = new Intent(TestSelectPhotoActivity.this, PhotoSelectActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean(PhotoSelectActivity.EXTRA_SHOW_CAMERA, true);
        bundle.putInt(PhotoSelectActivity.EXTRA_SELECT_MODE, PhotoSelectActivity.MODE_SINGLE);
        bundle.putInt(PhotoSelectActivity.EXTRA_SELECT_COUNT, 1);
        intent.putExtras(bundle);
        startActivityForResult(intent, REQUEST_CODE_SELECT_SINGLE);
    }

    /**
     * 选择图片并裁剪
     */
    private void selectSinglePhotoCrop() {
        Intent intent = new Intent(TestSelectPhotoActivity.this, PhotoSelectActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean(PhotoSelectActivity.EXTRA_SHOW_CAMERA, true);
        bundle.putInt(PhotoSelectActivity.EXTRA_SELECT_MODE, PhotoSelectActivity.MODE_CROP);
        bundle.putInt(PhotoSelectActivity.EXTRA_SELECT_COUNT, 1);
        //设置裁剪宽高比
//        bundle.putInt(PhotoSelectActivity.EXTRA_CROP_ASPECTX, 1);
//        bundle.putInt(PhotoSelectActivity.EXTRA_CROP_ASPECTY, 1);
        //设置裁剪尺寸
        bundle.putInt(PhotoSelectActivity.EXTRA_CROP_OUTPUTX, 400);
        bundle.putInt(PhotoSelectActivity.EXTRA_CROP_OUTPUTY, 400);
        intent.putExtras(bundle);
        startActivityForResult(intent, REQUEST_CODE_SELECT_CROP);
    }

    /**
     * 选择多张图片
     *
     * @param maxCount
     * @param pictures
     */
    private void selectMultiPhoto(final int maxCount, final ArrayList<Image> pictures) {
        Intent intent = new Intent(TestSelectPhotoActivity.this, PhotoSelectActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean(PhotoSelectActivity.EXTRA_SHOW_CAMERA, true);
        bundle.putInt(PhotoSelectActivity.EXTRA_SELECT_MODE, PhotoSelectActivity.MODE_MULTI);
        bundle.putInt(PhotoSelectActivity.EXTRA_SELECT_COUNT, maxCount);
        bundle.putSerializable(PhotoSelectActivity.EXTRA_DEFAULT_SELECTED_LIST, pictures);
        intent.putExtras(bundle);
        startActivityForResult(intent, REQUEST_CODE_SELECT_MULTI);
    }

    @Override
    public void selectPhoto(final int maxCount, final ArrayList<Image> selectPictures) {
        selectMultiPhoto(maxCount, selectPictures);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_select: {
                selectSinglePhoto();
                break;
            }
            case R.id.tv_select_crop: {
                selectSinglePhotoCrop();
                break;
            }
            case R.id.tv_select_multi: {
                selectMultiPhoto(9, mPictures);
                break;
            }
        }
    }

    private void startUp() {
//        mRunnable = new Runnable() {
//            @Override
//            public void run() {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Intent intent = new Intent(SplashActivity.this, TestSelectPhotoActivity.class);
//                        // 普通跳转到主界面
//                        startActivity(intent);
//                        finish();
//                    }
//                });
//            }
//        };
//        mHandler.postDelayed(mRunnable, SPLASH_SHOW_TIME);
    }

    private void showPermisionCheckDialog() {
        RxPermissions.getInstance(this)
                .requestEach(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_PHONE_STATE
                )
                .subscribe(new Action1<Permission>() {
                    @Override
                    public void call(Permission permission) {
                        if (permission.name.equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            isHasReadExternalPermission = permission.granted;
                            isCheckReadExternalPermission = true;
                        } else if (permission.name.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            isHasWriteExternalPermission = permission.granted;
                            isCheckWriteExternalPermission = true;
                        }

                        if (isCheckReadExternalPermission && isCheckWriteExternalPermission) {
                            if (!isHasReadExternalPermission || !isHasWriteExternalPermission) {
                                showStoragePermissionAlertDialog();
                                return;
                            }
                            startUp();
                        }
                    }
                });
    }

    private void showStoragePermissionAlertDialog() {
        final AlertDialog dialog = new AlertDialog.Builder(this,
                android.support.v7.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setTitle("开启权限")
                .setMessage("如果你拒绝使用存储权限,将会导致无法使用现有功能.\n请在[设置]>[权限]中打开")
                .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        try {
                            Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                            String pkg = "com.android.settings";
                            String cls = "com.android.settings.applications.InstalledAppDetails";
                            intent.setComponent(new ComponentName(pkg, cls));
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton("我知道了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                })
                .setCancelable(false)
                .create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                isHandleStorage = true;
            }
        });
        dialog.show();
    }

}
