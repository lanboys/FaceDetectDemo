//package com.facepp.demo;
//
//import com.google.gson.Gson;
//
//import java.util.List;
//
///**
// * Created by 蓝兵 on 2018/11/29.
// */
//
//public class xxx {
//
//    /**
//     * code : 0
//     * data : [{"imageId":"0456dc","url":"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543468922327&di=30f1e3db9cb2beb7c1c71cfeaca28f46&imgtype=0&src=http%3A%2F%2Fwww.17qq.com%2Fimg_qqtouxiang%2F81330143.jpeg"},{"imageId":"7ce525","url":"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543468922327&di=30f1e3db9cb2beb7c1c71cfeaca28f46&imgtype=0&src=http%3A%2F%2Fwww.17qq.com%2Fimg_qqtouxiang%2F81330143.jpeg"},{"imageId":"a2c8bf","url":"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543468922327&di=30f1e3db9cb2beb7c1c71cfeaca28f46&imgtype=0&src=http%3A%2F%2Fwww.17qq.com%2Fimg_qqtouxiang%2F81330143.jpeg"},{"imageId":"76bdcc","url":"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543468922327&di=30f1e3db9cb2beb7c1c71cfeaca28f46&imgtype=0&src=http%3A%2F%2Fwww.17qq.com%2Fimg_qqtouxiang%2F81330143.jpeg"},{"imageId":"3ff745","url":"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1543468922327&di=30f1e3db9cb2beb7c1c71cfeaca28f46&imgtype=0&src=http%3A%2F%2Fwww.17qq.com%2Fimg_qqtouxiang%2F81330143.jpeg"}]
//     */
//
//    private int code;
//    private List<DataBean> data;
//
//    public static xxx objectFromData(String str) {
//
//        return new Gson().fromJson(str, xxx.class);
//    }
//
//    public int getCode() {
//        return code;
//    }
//
//    public void setCode(int code) {
//        this.code = code;
//    }
//
//    public List<DataBean> getData() {
//        return data;
//    }
//
//    public void setData(List<DataBean> data) {
//        this.data = data;
//    }
//
//
//}
