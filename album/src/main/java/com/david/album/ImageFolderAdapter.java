package com.david.album;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

public class ImageFolderAdapter extends BaseAdapter {
    private Activity mActivity;
    private List<Folder> list;
    private String mFolderName;

    public ImageFolderAdapter(Activity mActivity, List<Folder> list, String folderName) {
        super();
        this.mActivity = mActivity;
        this.list = list;
        this.mFolderName = folderName;
    }

    public void changeData(List<Folder> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Folder getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mActivity).inflate(R.layout.item_album_list_dir, null);
            holder = new ViewHolder();
            holder.cover = (ImageView) convertView.findViewById(R.id.cover);
            holder.selectedTag = (ImageView) convertView.findViewById(R.id.id_dir_choose);
            holder.dirItemName = (TextView) convertView.findViewById(R.id.id_dir_item_name);
            holder.dirItemNum = (TextView) convertView.findViewById(R.id.id_dir_item_count);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (position == 0) {
            holder.dirItemName.setText(mActivity.getResources().getString(R.string.album_all_picture));
        } else {
            if (getItem(position).name != null) {
                holder.dirItemName.setText(getItem(position).name);
                holder.dirItemNum.setText("(" + getItem(position).images.size() + ")");
            }
        }

        if (mFolderName.equalsIgnoreCase(getItem(position).name)) {
            holder.selectedTag.setVisibility(View.VISIBLE);
        } else {
            if (position == 0 && "所有图片".equals(mFolderName)) {
                holder.selectedTag.setVisibility(View.VISIBLE);
            } else {
                holder.selectedTag.setVisibility(View.GONE);
            }
        }

        File imageFile = new File(getItem(position).images.get(0).getFilePath());
        Glide.with(mActivity).load(imageFile).placeholder(R.drawable.ic_album_image_default).fitCenter().centerCrop().into(holder.cover);
        return convertView;
    }

    class ViewHolder {
        TextView dirItemName;
        TextView dirItemNum;
        ImageView selectedTag;
        ImageView cover;
    }
}
