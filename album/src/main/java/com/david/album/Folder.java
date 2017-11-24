package com.david.album;

import java.util.List;

/**
 * 文件夹
 */
public class Folder {
    public static final Folder FOLDER_ALL = new Folder();
    public String name;
    public String path;
    public List<Image> images;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Folder folder = (Folder) o;

        return !(path != null ? !path.equals(folder.path) : folder.path != null);
    }

    @Override
    public int hashCode() {
        return path != null ? path.hashCode() : 0;
    }
}