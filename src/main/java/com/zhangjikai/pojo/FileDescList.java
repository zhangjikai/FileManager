package com.zhangjikai.pojo;

import java.util.List;

/**
 * Created by zhangjk on 2016/1/17.
 */
public class FileDescList {
    private List<FileDesc> result;

    public FileDescList() {
    }

    public FileDescList(List<FileDesc> result) {
        this.result = result;
    }

    public List<FileDesc> getResult() {
        return result;
    }

    public void setResult(List<FileDesc> result) {
        this.result = result;
    }
}
