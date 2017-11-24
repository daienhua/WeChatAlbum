package com.david.wechatalbum;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.david.album.Image;

import java.util.ArrayList;
import java.util.List;

/**
 * 图片选择 Adapter
 */
public class TestSelectPhotoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_CHOOSE = 1;
    private static final int TYPE_IMAGE = 2;
    private Activity mActivity;
    private int mMaxCount;
    private List<Image> mItems;
    private LayoutInflater mInflater;

    public TestSelectPhotoAdapter(Activity activity, List<Image> data, int maxCount) {
        mActivity = activity;
        mItems = data;
        mMaxCount = maxCount;
        mInflater = activity.getLayoutInflater();
    }

    @Override
    public int getItemCount() {
        int count = mItems.size();
        if (count == 0) {
            return 1;
        }
        if (count >= mMaxCount) {
            return count;
        } else {
            return count + 1;
        }
    }

    public void remove(Image object) {
        mItems.remove(object);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        int count = mItems.size();
        if (count == 0 || position == count) {
            return TYPE_CHOOSE;
        }
        return TYPE_IMAGE;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DataHolder(mInflater.inflate(R.layout.item_album_image_choose, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_CHOOSE: {
                ((DataHolder) holder).renderChoose(mActivity, (ArrayList<Image>) mItems, position,
                        mMaxCount);
                break;
            }
            case TYPE_IMAGE: {
                ((DataHolder) holder).renderImage(this, mActivity, mItems.get(position));
                break;
            }
        }
    }

    public interface ImageChangedListener {
        void selectPhoto(int maxCount, ArrayList<Image> selectPictures);
    }

    static class DataHolder extends RecyclerView.ViewHolder {
        ImageView mIcon;
        ImageView mClose;

        public DataHolder(View view) {
            super(view);
            mIcon = (ImageView) view.findViewById(R.id.icon);
            mClose = (ImageView) view.findViewById(R.id.close);
        }

        public void renderChoose(final Activity fragment, final ArrayList<Image> items, int position,
                                 final int maxCount) {
            mClose.setVisibility(View.GONE);
            mIcon.setImageResource(R.drawable.ic_album_post_add_picture);
            mIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (fragment instanceof ImageChangedListener) {
                        ((ImageChangedListener) fragment).selectPhoto(maxCount, items);
                    }
                }
            });
        }

        public void renderImage(final TestSelectPhotoAdapter adapter, final Activity fragment, final Image image) {
            Glide.with(fragment).load(image.getFilePath()).placeholder(R.drawable.ic_album_image_default).fitCenter().centerCrop().into(mIcon);
            mClose.setVisibility(View.VISIBLE);
            mClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.remove(image);
                }
            });
        }
    }
}
