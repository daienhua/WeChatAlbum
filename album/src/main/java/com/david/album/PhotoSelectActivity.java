package com.david.album;

import android.Manifest;
import android.animation.Animator;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.david.album.utils.AndroidUtils;
import com.david.album.utils.PermissionsChecker;
import com.david.album.view.HeightAnimation;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rx.functions.Action1;

/**
 * 图片选择Activity
 */
public class PhotoSelectActivity extends Activity {

    /**
     * 选择结果，返回为 {@link ArrayList}&lt;{@link Image}&gt; 或 {@link Image}
     */
    public static final String EXTRA_RESULT = "select_result";
    /**
     * 最大图片选择次数，int类型
     */
    public static final String EXTRA_SELECT_COUNT = "max_select_count";
    /**
     * 图片选择模式，int类型
     */
    public static final String EXTRA_SELECT_MODE = "select_count_mode";
    /**
     * 是否显示相机，boolean类型
     */
    public static final String EXTRA_SHOW_CAMERA = "show_camera";
    /**
     * 默认选择的数据集
     */
    public static final String EXTRA_DEFAULT_SELECTED_LIST = "default_result";
    /**
     * 单选 (返回 ArrayList)
     */
    public static final int MODE_SINGLE = 0;
    /**
     * 多选 (返回 ArrayList)
     */
    public static final int MODE_MULTI = 1;
    /**
     * 剪切 (返回 Image)
     */
    public static final int MODE_CROP = 2;
    public static final int MODE_CAMERA = 3;
    public static final int MODE_CAMERA_CROP = 4;

    public static final String EXTRA_CROP_ASPECTX = "crop_aspectx";
    public static final String EXTRA_CROP_ASPECTY = "crop_aspecty";
    public static final String EXTRA_CROP_OUTPUTX = "crop_outputx";
    public static final String EXTRA_CROP_OUTPUTY = "crop_outputy";
    static final String PREVIEW_LIST = "preview_list";
    static final String PREVIEW_COMPLETE = "preview_complete";
    static final String PREVIEW_SELECTED_LIST = "preview_selected_list";
    static final String CROP_DATA = "crop_data";
    // Save when recycled
    private static final String CAMERA_FILE_PATH = "camera_file_path";
    private static final String CROP_FILE_PATH = "crop_file_path";
    // 请求加载系统照相机
    private static final int REQUEST_CAMERA = 100;
    // 预览
    private static final int REQUEST_PREVIEW = 101;
    // 剪切
    private static final int REQUEST_CROP = 102;
    // 不同loader定义
    private static final int LOADER_ALL = 0;
    private static final int LOADER_CATEGORY = 1;
    public static int PICTRUE_LIMIT_SIZE = 9;
    private GridView mList;
    private FrameLayout mListFrame;
    private TextView mPreview;
    private FrameLayout mBottomCapture;
    private View mImageFolderBackground;
    private ListView mIdListDirs;
    private ScrollView mImageFolderLayout;
    private TextView mFolderName;
    private LinearLayout mLlBack;
    private TextView mTvSure;

    // 结果数据
    private ArrayList<Image> mSelectedImages = new ArrayList<>();
    // 文件夹数据
    private ArrayList<Folder> mFolders = new ArrayList<>();
    private Folder mCurrentFolder;
    private ArrayList<Image> mImages = new ArrayList<>();

    private File mImgDir = new File("");// 图片数量最多的文件夹
    private List<String> mImgs = new ArrayList<String>();//所有的图片
    private ImageFolderAdapter mImageFolderAdapter;

    private int mMode = MODE_SINGLE;
    private boolean mIsShowCamera = false;

    private File mCameraTmpFile;
    private File mCropTmpFile;
    private int mImageCount;
    private ImageGridAdapter mMediaSelectAdapter;
    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallback =
            new LoaderManager.LoaderCallbacks<Cursor>() {

                private final String[] IMAGE_PROJECTION = {
                        MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME,
                        MediaStore.Images.Media.DATE_ADDED, MediaStore.Images.Media.MIME_TYPE,
                        MediaStore.Images.Media._ID
                };

                @Override
                public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                    if (id == LOADER_ALL) {
                        CursorLoader cursorLoader =
                                new CursorLoader(PhotoSelectActivity.this, MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                        IMAGE_PROJECTION, null, null, IMAGE_PROJECTION[2] + " DESC");
                        return cursorLoader;
                    } else if (id == LOADER_CATEGORY) {
                        CursorLoader cursorLoader =
                                new CursorLoader(PhotoSelectActivity.this, MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                        IMAGE_PROJECTION,
                                        IMAGE_PROJECTION[0] + " like '%" + args.getString("path") + "%'", null,
                                        IMAGE_PROJECTION[2] + " DESC");
                        return cursorLoader;
                    }

                    return null;
                }

                @Override
                public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                    if (data != null) {
                        ArrayList<Folder> folders = new ArrayList<>();
                        int count = data.getCount();
                        if (count > 0) {
                            data.moveToFirst();
                            do {
                                String path = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
                                String name = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
                                if (!TextUtils.isEmpty(name) && name.contains("/")) {
                                    name = name.substring(name.lastIndexOf("/") + 1);
                                }
                                long dateTime = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));
                                String mimeType = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[3]));

                                long size = new File(path).length();
                                if (size <= 0) continue;

                                Image image = new Image(path, name, 0, 0, size, dateTime, mimeType, false);
                                // 获取文件夹名称
                                File imageFile = new File(image.getFilePath());
                                File folderFile = imageFile.getParentFile();
                                if (folderFile != null && !folderFile.getAbsolutePath()
                                        .contains("/temp/")) {//过滤掉/DIMC/temp（临时图片）
                                    Folder folder = new Folder();
                                    folder.name = folderFile.getName();
                                    folder.path = folderFile.getAbsolutePath();
                                    if (!folders.contains(Folder.FOLDER_ALL)) {
                                        Folder folderAll = new Folder();
                                        List<Image> imageList = new ArrayList<>();
                                        imageList.add(image);
                                        folderAll.images = imageList;
                                        folders.add(folderAll);
                                    } else {
                                        Folder f = folders.get(folders.indexOf(Folder.FOLDER_ALL));
                                        f.images.add(image);
                                    }
                                    if (!folders.contains(folder)) {
                                        List<Image> imageList = new ArrayList<>();
                                        imageList.add(image);
                                        folder.images = imageList;
                                        folders.add(folder);
                                    } else {
                                        // 更新
                                        Folder f = folders.get(folders.indexOf(folder));
                                        f.images.add(image);
                                    }
                                }
                            } while (data.moveToNext());

                            mFolders.clear();
                            mFolders.addAll(folders);
                            if (mCurrentFolder != null) {
                                mCurrentFolder = mFolders.get(mFolders.indexOf(mCurrentFolder));
                            } else {
                                mCurrentFolder = mFolders.get(mFolders.indexOf(Folder.FOLDER_ALL));
                                mImages.clear();
                                mImages.addAll(mCurrentFolder.images);
                            }
                            mMediaSelectAdapter.setData(mCurrentFolder.images);
                        }
                    }
                }

                @Override
                public void onLoaderReset(Loader<Cursor> loader) {

                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_photo_select);

        mList = (GridView) findViewById(R.id.list);
        mListFrame = (FrameLayout) findViewById(R.id.list_frame);
        mPreview = (TextView) findViewById(R.id.preview);
        mBottomCapture = (FrameLayout) findViewById(R.id.bottom_capture);
        mImageFolderBackground = findViewById(R.id.image_folder_background);
        mIdListDirs = (ListView) findViewById(R.id.id_list_dirs);
        mImageFolderLayout = (ScrollView) findViewById(R.id.image_folder_layout);
        mFolderName = (TextView) findViewById(R.id.folder_name);
        mLlBack = (LinearLayout) findViewById(R.id.ll_back);
        mTvSure = (TextView) findViewById(R.id.tv_sure);

        mLlBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mTvSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra(EXTRA_RESULT, mSelectedImages);
                setResult(Activity.RESULT_OK, data);
                onBackPressed();
            }
        });

        if (savedInstanceState != null) {
            String path = savedInstanceState.getString(CAMERA_FILE_PATH);
            if (path != null) {
                mCameraTmpFile = new File(path);
            }
            path = savedInstanceState.getString(CROP_FILE_PATH);
            if (path != null) {
                mCropTmpFile = new File(path);
            }
        }

        // 图片选择模式
        mMode = getIntent().getExtras().getInt(EXTRA_SELECT_MODE, MODE_SINGLE);

        // 选择图片数量
        mImageCount = getIntent().getExtras().getInt(EXTRA_SELECT_COUNT);

        List<Image> mDefaultSelectedList = null;
        if (mMode == MODE_MULTI) {
            mDefaultSelectedList =
                    (ArrayList<Image>) getIntent().getExtras().getSerializable(EXTRA_DEFAULT_SELECTED_LIST);
        } else if (mMode == MODE_CAMERA) {
            safeShowCameraAction();
        } else if (mMode == MODE_CAMERA_CROP) {
            safeShowCameraAction();
        }
        if (mDefaultSelectedList != null) {
            mSelectedImages.addAll(mDefaultSelectedList);
        }

        // 是否显示照相机
        mIsShowCamera = getIntent().getExtras().getBoolean(EXTRA_SHOW_CAMERA, false);

        getLoaderManager().initLoader(LOADER_ALL, null, mLoaderCallback);

        updateUI();
        mMediaSelectAdapter = new ImageGridAdapter(this, mSelectedImages, mIsShowCamera, mMode);
        mList.setAdapter(mMediaSelectAdapter);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mMediaSelectAdapter.isShowCamera()) {
                    // 如果显示照相机，则第一个Grid显示为照相机，处理特殊逻辑
                    if (i == 0) {
                        safeShowCameraAction();
                    } else {
                        // 正常操作
                        Image image = (Image) adapterView.getAdapter().getItem(i);
                        image.setIsSelected(true);
                        selectResource(image);
                    }
                } else {
                    // 正常操作
                    Image image = (Image) adapterView.getAdapter().getItem(i);
                    image.setIsSelected(true);
                    selectResource(image);
                }
            }
        });

        mFolderName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initListDirPopupWindow();
            }
        });

        if (mMode == MODE_SINGLE || mMode == MODE_CROP) {
            mPreview.setVisibility(View.GONE);
            mTvSure.setVisibility(View.GONE);
        }
    }

    private void safeShowCameraAction() {
        RxPermissions.getInstance(this)
                .request(Manifest.permission.CAMERA)
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        boolean isHasPermission = PermissionsChecker.isHasCameraPermission();
                        if (isHasPermission) {
                            showCameraAction();
                        } else {
                            showPermissionAlertDialog();
                        }
                    }
                });
    }

    private void showPermissionAlertDialog() {
        final AlertDialog dialog = new AlertDialog.Builder(this,
                android.support.v7.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setTitle("开启相机权限")
                .setMessage("如果你拒绝使用相机权限,将无法使用拍照、二维码扫描等功能.\n请在[设置]>[权限]中打开")
                .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        try {
                            Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                            String pkg = "com.android.settings";
                            String cls = "com.android.settings.applications.InstalledAppDetails";
                            intent.setComponent(new ComponentName(pkg, cls));
                            intent.setData(Uri.parse("package:" + PhotoSelectActivity.this.getPackageName()));
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
                        PhotoSelectActivity.this.onBackPressed();
                    }
                }).create();
        dialog.show();
    }

    /**
     * 选择相机
     */
    private void showCameraAction() {
        // 判断选择数量问题
        if (mMode == MODE_MULTI && mImageCount == mSelectedImages.size()) {
            Toast.makeText(this, getString(R.string.album_msg_amount_limit, "" + mImageCount), Toast.LENGTH_SHORT).show();
            return;
        }
        // 跳转到系统照相机
        try {
            // 设置系统相机拍照后的输出路径
            // 创建临时文件
            mCameraTmpFile = AndroidUtils.getTmpFile();
            Intent intent = AndroidUtils.getImageFromCamera(this, mCameraTmpFile);
            startActivityForResult(intent, REQUEST_CAMERA);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, getString(R.string.album_msg_no_camera), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.album_msg_no_sdcard), Toast.LENGTH_SHORT).show();

        }
    }

    /**
     * 选择图片操作
     */
    private void selectResource(Image image) {
        if (image != null) {
            // 多选模式
            if (mMode == MODE_MULTI) {
                if (mSelectedImages.contains(image)) {
                    mSelectedImages.remove(image);
                } else {
                    // 判断选择数量问题
                    if (mImageCount == mSelectedImages.size()) {
                        Toast.makeText(this, getString(R.string.album_msg_amount_limit, "" + mImageCount), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    image.setIsSelected(true);
                    mSelectedImages.add(image);
                }
                updateUI();
                mMediaSelectAdapter.notifyDataSetChanged();
            } else if (mMode == MODE_SINGLE || mMode == MODE_CAMERA) {
                Intent data = new Intent();
                data.putExtra(EXTRA_RESULT, image);
                this.setResult(Activity.RESULT_OK, data);
                this.onBackPressed();
            } else if (mMode == MODE_CROP || mMode == MODE_CAMERA_CROP) {
                try {
                    Uri data = AndroidUtils.getImageUri(this, new File(image.getFilePath()));
                    mCropTmpFile = AndroidUtils.getTmpFile();
                    Uri out;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        out = Uri.fromFile(mCropTmpFile);
                    } else {
                        out = AndroidUtils.getImageUri(this, mCropTmpFile);
                    }
                    int aspectX = getIntent().getExtras().getInt(EXTRA_CROP_ASPECTX);
                    int aspectY = getIntent().getExtras().getInt(EXTRA_CROP_ASPECTY);
                    int outputX = getIntent().getExtras().getInt(EXTRA_CROP_OUTPUTX);
                    int outputY = getIntent().getExtras().getInt(EXTRA_CROP_OUTPUTY);
                    Intent intent = AndroidUtils.getImageFromCrop(data, out, aspectX, aspectY, outputX, outputY);
                    startActivityForResult(intent, REQUEST_CROP);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(this, getString(R.string.album_msg_no_crop), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, getString(R.string.album_msg_no_sdcard), Toast.LENGTH_SHORT).show();

                }
            }
        }
    }

    /**
     * 初始化展示文件夹的popupWindow
     */
    private void initListDirPopupWindow() {
        if (dismissMenu()) {
            return;
        }
        mImageFolderBackground.setAlpha(0);
        mImageFolderBackground.animate().setDuration(300).alpha(0.6f).setListener(null).start();
        mImageFolderLayout.setVisibility(View.VISIBLE);
        mImageFolderAdapter = new ImageFolderAdapter(this, mFolders, mFolderName.getText().toString());
        mIdListDirs.setAdapter(mImageFolderAdapter);
        mIdListDirs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    if (position == 0) {
                        mFolderName.setText(getResources().getString(R.string.album_all_picture));
                    } else {
                        mFolderName.setText(mFolders.get(position).name);
                    }
                    if (position != 0) {
                        mImgDir = new File(mFolders.get(position).path);
                        mImgs = Arrays.asList(mImgDir.list(new FilenameFilter() {
                            @Override
                            public boolean accept(File dir, String filename) {
                                if (filename.endsWith(".jpg") || filename.endsWith(".png") || filename.endsWith(
                                        ".jpeg") || filename.endsWith(".gif")) {
                                    return true;
                                }
                                return false;
                            }
                        }));
                    }
                    mCurrentFolder = mFolders.get(position);
                    mImageFolderAdapter.changeData(mFolders);
                    List<Image> typeImages = new ArrayList<Image>();
                    if (position != 0) {
                        for (Image image : mImages) {
                            for (String filename : mImgs) {
                                if (image.getFilename().equals(filename)) {
                                    typeImages.add(image);
                                }
                            }
                        }
                        mMediaSelectAdapter.setData(typeImages);
                    } else {
                        mMediaSelectAdapter.setData(mImages);
                    }
                    dismissMenu();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        HeightAnimation animation =
                new HeightAnimation(mIdListDirs, 0, AndroidUtils.dp2px(this, 360));
        animation.setDuration(300);
        mIdListDirs.startAnimation(animation);
        mImageFolderBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissMenu();
            }
        });
    }

    private boolean dismissMenu() {
        if (mImageFolderLayout.getVisibility() == View.VISIBLE) {
            HeightAnimation animation =
                    new HeightAnimation(mIdListDirs, AndroidUtils.dp2px(this, 360), 0);
            animation.setDuration(300);
            mIdListDirs.startAnimation(animation);
            mImageFolderBackground.animate()
                    .setDuration(300)
                    .alpha(0)
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mImageFolderLayout.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    })
                    .start();
            return true;
        }
        return false;
    }


    private void updateUI() {
        if (mMode != MODE_MULTI) {
            return;
        }
        int selectedCount = mSelectedImages.size();
        String previewText = getString(R.string.album_preview);
        mPreview.setText(previewText);
        mPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PhotoSelectActivity.this, PreviewActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(PREVIEW_LIST, mSelectedImages);
                intent.putExtras(bundle);
                startActivityForResult(intent, REQUEST_PREVIEW);
            }
        });

        if (selectedCount > 0) {
            mTvSure.setText(getResources().getString(R.string.album_sure_with_count, selectedCount + "", mImageCount + ""));
            mPreview.setText(getResources().getString(R.string.album_preview_with_count, selectedCount + ""));
        } else {
            mTvSure.setText(getResources().getString(R.string.album_sure));
            mPreview.setText(getResources().getString(R.string.album_preview));
        }
        mTvSure.setEnabled(selectedCount != 0);
        mPreview.setEnabled(selectedCount != 0);
    }

    private Image getResource(File file, Intent data) {
        Image resource = null;
        if (file.exists()) {
            resource = new Image(file.getAbsolutePath());
        }
        if (data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                String filePath = AndroidUtils.getRealFilePath(this, uri);
                if (!TextUtils.isEmpty(filePath)) {
                    File image = new File(filePath);
                    if (image.exists()) {
                        resource = new Image(image.getAbsolutePath());
                    }
                }
            }
        }
        return resource;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            if (mMode == MODE_CAMERA || mMode == MODE_CAMERA_CROP) {
                onBackPressed();//如果选择拍照，取消拍照，则直接返回
            }
            return;
        }
        if (requestCode == REQUEST_CAMERA) {
            Image image = getResource(mCameraTmpFile, data);
            if (image == null) {
                return;
            }
            if (mMode != MODE_MULTI) {
                selectResource(image);
            } else {
                //拍完照片直接返回
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                image.setIsSelected(true);
                mSelectedImages.add(image);
                bundle.putSerializable(EXTRA_RESULT, mSelectedImages);
                intent.putExtras(bundle);
                setResult(Activity.RESULT_OK, intent);
                onBackPressed();
            }
        } else if (requestCode == REQUEST_CROP) {
            Image image = getResource(mCropTmpFile, data);
            if (image == null) {
                return;
            }
            Intent intent = new Intent();
            intent.putExtra(EXTRA_RESULT, image);
            setResult(Activity.RESULT_OK, intent);
            onBackPressed();
        } else if (requestCode == REQUEST_PREVIEW) {
            ArrayList<Image> images =
                    (ArrayList<Image>) data.getSerializableExtra(PREVIEW_LIST);
            boolean complete = data.getBooleanExtra(PREVIEW_COMPLETE, false);
            if (complete) {
                Intent intent = new Intent();
                intent.putExtra(EXTRA_RESULT, images);
                setResult(Activity.RESULT_OK, intent);
                onBackPressed();
            } else {
                mSelectedImages.clear();
                mSelectedImages.addAll(images);
                updateUI();
                mMediaSelectAdapter.notifyDataSetChanged();
            }
        }
    }

}
