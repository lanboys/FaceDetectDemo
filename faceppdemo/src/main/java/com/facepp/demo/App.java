package com.facepp.demo;

import android.app.Application;

import com.facepp.demo.load.AppUtil;

/**
 * Created by 蓝兵 on 2018/11/28.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //全局初始化
        AppUtil.initGlobal(this, getApplicationContext(), true);
    }
}
