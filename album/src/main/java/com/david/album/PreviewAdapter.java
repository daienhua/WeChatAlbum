package com.david.album;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.david.album.fullscreen.SystemUiHider;
import com.david.album.view.RecyclingPagerAdapter;

import java.io.File;
import java.util.List;

import uk.co.senab.photoview.PhotoViewAttacher;

public class PreviewAdapter extends RecyclingPagerAdapter {

    private Activity mActivity;
    private List<Image> mImages;
    private SystemUiHider mSystemUiHider;

    public PreviewAdapter(Activity activity, List<Image> images, SystemUiHider systemUiHider) {
        this.mActivity = activity;
        this.mImages = images;
        this.mSystemUiHider = systemUiHider;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup container) {
        final ViewHolder holder;
        if (convertView == null) {
            View view =
                    mActivity.getLayoutInflater().inflate(R.layout.item_album_preview_media, container, false);
            holder = new ViewHolder(view);
            convertView = view;
            convertView.setTag(holder);
            holder.mAttacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
                @Override
                public void onViewTap(View view, float v, float v1) {
                    mSystemUiHider.toggle();
                }
            });
            holder.mImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSystemUiHider.toggle();
                }
            });
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final Image image = mImages.get(position);
        Glide.with(mActivity).asBitmap()
                .load(new File(image.getFilePath()))
                .apply(RequestOptions.fitCenterTransform())
                .into(new BitmapImageViewTarget(holder.mImage) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        super.setResource(resource);
                        holder.mAttacher.update();
                    }
                });
        return convertView;
    }

    @Override
    public int getCount() {
        return mImages.size();
    }

    static class ViewHolder {
        PhotoViewAttacher mAttacher;
        ImageView mImage;

        ViewHolder(View view) {
            mImage = (ImageView) view.findViewById(R.id.image);
            mAttacher = new PhotoViewAttacher(mImage);
        }
    }
}

