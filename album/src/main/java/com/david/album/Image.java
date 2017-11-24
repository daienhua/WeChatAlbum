package com.david.album;

import android.os.Parcel;

import java.io.Serializable;

/**
 * <p><font color=red>已实现Cloneable，增减字段需注意修改{@link #clone()}</font></p>
 */
public class Image implements Comparable<Image>, Serializable, Cloneable {
    private long mSize;
    private long mTime;
    private String mimeType;
    private String filename;
    private String filePath;
    private int width;
    private int height;
    /**
     * 传原图|压缩图片
     */
    private boolean mIsOriginal;
    private boolean isSelected;

    public Image() {
    }

    public Image(String path) {
        this.filePath = path;
    }

    public Image(String path, String name, int width, int height, long size, long time,
                 String mimeType, boolean isSelected) {
        this.filePath = path;
        this.filename = name;
        this.width = width;
        this.height = height;
        this.mSize = size;
        this.mTime = time;
        this.mimeType = mimeType;
        this.isSelected = isSelected;
    }

    protected Image(Parcel in) {
        this.filePath = in.readString();
        this.filename = in.readString();
        this.width = in.readInt();
        this.height = in.readInt();
        this.mSize = in.readLong();
        this.mTime = in.readLong();
        this.mIsOriginal = in.readByte() != 0;
        this.isSelected = in.readByte() != 0;
    }

    @Override
    public Image clone() {
        try {
            return (Image) super.clone();
        } catch (CloneNotSupportedException e) {
            return new Image();
        }
    }

    public long getmSize() {
        return mSize;
    }

    public void setmSize(long mSize) {
        this.mSize = mSize;
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(long mTime) {
        this.mTime = mTime;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean original() {
        return mIsOriginal;
    }

    public void setIsOriginal(boolean mIsOriginal) {
        this.mIsOriginal = mIsOriginal;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Image image = (Image) o;

        return filePath.equals(image.filePath);
    }

    @Override
    public int hashCode() {
        return filePath.hashCode();
    }

    @Override
    public int compareTo(Image another) {
        return (int) (mTime - another.mTime);
    }

}
