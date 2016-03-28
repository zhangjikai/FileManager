package com.zhangjikai.utils;

/**
 * Created by zhangjk on 2016/1/17.
 */
public enum FileType {
    DIR("dir"), File("file");

    private String value;

    FileType(String value) {
        this.value = value;
    }


    @Override
    public String toString() {
        return this.value;
    }
}
