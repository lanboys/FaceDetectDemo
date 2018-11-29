package com.facepp.demo;

import java.util.List;

public class ApiResult  {

    protected int code;
    protected String msg;
    protected List<ImageInfoBean> data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<ImageInfoBean> getData() {
        return data;
    }

    public void setData(List<ImageInfoBean> data) {
        this.data = data;
    }
}
