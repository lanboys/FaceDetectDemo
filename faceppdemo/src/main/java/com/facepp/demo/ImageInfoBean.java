package com.facepp.demo;

/**
 * Created by 蓝兵 on 2018/11/29.
 */

public class ImageInfoBean {

    //private String url = "https://gss0.bdstatic.com/-4o3dSag_xI4khGkpoWK1HF6hhy/baike/c0%3Dbaike92%2C5%2C5%2C92%2C30/sign=775f519ac08065386fe7ac41f6b4ca21/fd039245d688d43f63d84526771ed21b0ff43bf5.jpg";
    private String url = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543468922327&di=30f1e3db9cb2beb7c1c71cfeaca28f46&imgtype=0&src=http%3A%2F%2Fwww.17qq.com%2Fimg_qqtouxiang%2F81330143.jpeg";
    private boolean isDetectSuccess;
    private boolean isDetect;
    private String imageId;

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isDetectSuccess() {
        return isDetectSuccess;
    }

    public void setDetectSuccess(boolean detectSuccess) {
        isDetectSuccess = detectSuccess;
    }

    public boolean isDetect() {
        return isDetect;
    }

    public void setDetect(boolean detect) {
        isDetect = detect;
    }

    @Override
    public String toString() {
        return "ImageInfoBean{" +
                "url='" + url + '\'' +
                ", isDetectSuccess=" + isDetectSuccess +
                ", isDetect=" + isDetect +
                '}';
    }
}
