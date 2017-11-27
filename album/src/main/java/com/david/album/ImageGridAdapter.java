package com.david.album;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageGridAdapter extends BaseAdapter {
    private static final int TYPE_CAMERA = 0;
    private static final int TYPE_NORMAL = 1;
    private final int mMode;
    private LayoutInflater mInflater;
    private Activity mActivity;
    private List<Image> mImage = new ArrayList<>();
    private boolean mShowCamera = true;
    private boolean showSelectIndicator = true;
    private List<Image> mSelectedData;

    public ImageGridAdapter(Activity activity, List<Image> selectedData, boolean showCamera, int mode) {
        this.mActivity = activity;
        this.mInflater = activity.getLayoutInflater();
        this.mMode = mode;
        this.mShowCamera = showCamera;
        this.mSelectedData = selectedData;
    }

    /**
     * 显示选择指示器
     */
    public void showSelectIndicator(boolean b) {
        showSelectIndicator = b;
    }

    public boolean isShowCamera() {
        return mShowCamera;
    }

    public void setShowCamera(boolean b) {
        if (mShowCamera == b) return;

        mShowCamera = b;
        notifyDataSetChanged();
    }

    /**
     * 选择某个图片，改变选择状态
     */
    public void select(Image image) {
        if (mSelectedData.contains(image)) {
            mSelectedData.remove(image);
        } else {
            mSelectedData.add(image);
        }
        notifyDataSetChanged();
    }

    /**
     * 通过图片路径设置默认选择
     */
    public void setDefaultSelected(ArrayList<String> resultList) {
        for (String path : resultList) {
            Image image = getImageByPath(path);
            if (image != null) {
                mSelectedData.add(image);
            }
        }
        if (mSelectedData.size() > 0) {
            notifyDataSetChanged();
        }
    }

    public void setData(List<Image> data) {
        if (data != null) {
            mImage.clear();
            mImage.addAll(data);
            notifyDataSetChanged();
        }
    }

    private Image getImageByPath(String path) {
        if (mImage.size() > 0) {
            for (Image image : mImage) {
                if (image.getFilename().equalsIgnoreCase(path)) {
                    return image;
                }
            }
        }
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (mShowCamera) {
            return position == 0 ? TYPE_CAMERA : TYPE_NORMAL;
        }
        return TYPE_NORMAL;
    }

    @Override
    public int getCount() {
        return mImage.size() + (mShowCamera ? 1 : 0);
    }

    @Override
    public Image getItem(int i) {
        if (mShowCamera) {
            if (i == 0) {
                return null;
            }
            return mImage.get(i - 1);
        } else {
            return mImage.get(i);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        int type = getItemViewType(i);
        if (type == TYPE_CAMERA) {
            view = mInflater.inflate(R.layout.item_album_camera, viewGroup, false);
            view.setTag(null);
        } else if (type == TYPE_NORMAL) {
            ViewHolder holder;
            if (view == null) {
                view = mInflater.inflate(R.layout.item_album_image, viewGroup, false);
                holder = new ViewHolder(view);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            if (holder != null) {
                holder.bindData(getItem(i));
            }
        }

        return view;
    }

    class ViewHolder {
        ImageView image;
        View mask;
        ImageView indicator;

        ViewHolder(View view) {
            image = (ImageView) view.findViewById(R.id.image);
            mask = view.findViewById(R.id.mask);
            indicator = (ImageView) view.findViewById(R.id.checkmark);
            view.setTag(this);
        }

        void bindData(final Image data) {
            if (data == null) return;
            // 处理单选和多选状态
            if (showSelectIndicator) {
                if (mSelectedData.contains(data)) {
                    // 设置选中状态
                    indicator.setImageResource(R.drawable.ic_album_checkbox_orange_checked);
                    mask.setVisibility(View.VISIBLE);
                } else {
                    // 未选择
                    indicator.setImageResource(R.drawable.ic_album_checkbox_white_unchecked);
                    mask.setVisibility(View.GONE);
                }
            } else {
                indicator.setVisibility(View.GONE);
            }
            if (mMode == PhotoSelectActivity.MODE_SINGLE || mMode == PhotoSelectActivity.MODE_CROP) {
                indicator.setVisibility(View.GONE);
            }
            File imageFile = new File(data.getFilePath());

            Glide.with(mActivity).load(imageFile).apply(RequestOptions.placeholderOf(R.drawable.ic_album_image_default).centerCrop()).into(image);
        }
    }
}
