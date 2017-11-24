package com.david.album.utils;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Window;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AndroidUtils {

    public static Intent getImageFromCamera(Context context, File file) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getImageUri(context, file));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //添加这一句表示对目标应用临时授权该Uri所代表的文件
        return intent;
    }

    /**
     * 获取Image uri
     *
     * @param context
     * @param file
     * @return
     */
    public static Uri getImageUri(Context context, File file) {
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ContentValues contentValues = new ContentValues(1);
            contentValues.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
            uri = getImageContentUri(context, file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }

    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            cursor.close();
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
        } else {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, filePath);
            return context.getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        }
    }

    public static Intent getImageFromCrop(Uri data, Uri out, int aspectX, int aspectY, int outputX,
                                          int outputY) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(data, "image/*");
        intent.putExtra("output", out);
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", aspectX);// 裁剪框比例
        intent.putExtra("aspectY", aspectY);
        intent.putExtra("outputX", outputX);// 输出图片大小
        intent.putExtra("outputY", outputY);
        intent.putExtra("scale", true);// 去黑边
        intent.putExtra("scaleUpIfNeeded", true);// 去黑边
        intent.putExtra("return-data", false);// 不返回图片数据. 如果为 true 的话, 返回图片太大就会 crash
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //添加这一句表示对目标应用临时授权该Uri所代表的文件
        return intent;
    }

    /**
     * Try to return the absolute file path from the given Uri
     *
     * @param context
     * @param uri
     * @return the file path or null
     */
    public static String getRealFilePath(final Context context, final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    public static int getWidth(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    public static int getHeight(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    public static int getDensity(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.densityDpi;
    }

    public static int dp2px(Context context, float dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 在系统 'Pictures' 文件夹中生成以 '.jpg' 结束的文件
     */
    public static File getTmpFile() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return getTmpFile(null);
        }
        throw new IllegalStateException("The media not mounted.");
    }

    /**
     * @param parent 在 parent 目录中生成临时文件
     */
    public synchronized static File getTmpFile(File parent, String random) {
        if (!parent.exists()) {
            parent.mkdirs();
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
        StringBuilder builder = new StringBuilder();
        builder.append("tmp_");
        builder.append(timeStamp);
        if (random != null) builder.append("_").append(random);
        String fileName = builder.toString();
        File file = new File(parent, fileName);
        if (file.exists()) {
            file = getTmpFile(parent, RandomStringUtils.randomAlphabetic(4));
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
        }
        return file;
    }

    /**
     * @param parent 在 parent 目录中生成临时文件夹
     */
    public synchronized static File getTmpDir(File parent, String random) {
        if (!parent.exists()) {
            parent.mkdirs();
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
        StringBuilder builder = new StringBuilder();
        builder.append("tmp_");
        builder.append(timeStamp);
        if (random != null) builder.append("_").append(random);
        String fileName = builder.toString();
        File file = new File(parent, fileName);
        if (file.exists()) {
            file = getTmpDir(parent, RandomStringUtils.randomAlphabetic(4));
        }
        file.mkdir();
        return file;
    }

    private synchronized static File getTmpFile(String random) {
        File pic = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!pic.exists()) {
            pic.mkdirs();
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
        StringBuilder builder = new StringBuilder();
        builder.append("media_");
        builder.append(timeStamp);
        if (random != null) builder.append("_").append(random);
        builder.append(".jpg");
        String fileName = builder.toString();
        File file = new File(pic, fileName);
        if (file.exists()) {
            file = getTmpFile(RandomStringUtils.randomAlphabetic(4));
        }
        return file;
    }

    public synchronized static File getCacheFile(String fileName) {
        File pic = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!pic.exists()) {
            pic.mkdirs();
        }
        StringBuilder builder = new StringBuilder();
        builder.append("media_");
        if (fileName != null) builder.append(fileName);
        builder.append(".png");
        File file = new File(pic, builder.toString());
        return file;
    }

    public static boolean isKeyPanShow(Context context, Window window) {
        return getKeyPanHeight(context, window) != 0;
    }

    public static int getKeyPanHeight(Context context, Window window) {
        return getHeight(context) - getStatusBarHeight(window) - getContentHeight(window);
    }

    public static int getStatusBarHeight(Window window) {
        Rect rect = new Rect();
        window.getDecorView().getWindowVisibleDisplayFrame(rect);
        return rect.top;
    }

    public static int getContentHeight(Window window) {
        Rect rect = new Rect();
        window.getDecorView().getWindowVisibleDisplayFrame(rect);
        return rect.height();
    }

    public static void copy(Context context, String content) {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            cm.setPrimaryClip(ClipData.newPlainText(content, content));
        } else {
            cm.setText(content);
        }
    }

    /**
     * 遍历删除文件和子文件
     */
    public static boolean deleteFile(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            for (File temp : children) {
                boolean success = deleteFile(temp);
                if (!success) {
                    return false;
                }
            }
        }
        return file.delete();
    }

    /**
     * 获取手机唯一标识序列号
     *
     * @return
     */
    public static String getUniqueSerialNumber() {
        String phoneName = Build.MODEL;// Galaxy nexus 品牌类型
        String manuFacturer = Build.MANUFACTURER;//samsung 品牌
        return manuFacturer + "-" + phoneName + "-" + getSerialNumber();
    }

    /**
     * 序列号
     *
     * @return
     */
    public static String getSerialNumber() {
        String serial = null;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serial;
    }

    /**
     * 获取状态栏高度
     *
     * @param activity
     * @return
     */
    public static int getStatusBarHeight(Activity activity) {
        int height = 0;
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen",
                "android");
        if (resourceId > 0) {
            height = activity.getResources().getDimensionPixelSize(resourceId);
        }
        return height;
    }

    public static int getNavBarHeight(Activity activity) {
        int resourceId = activity.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return activity.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

}
