package com.detu.myapplication;

import android.app.Application;

/**
 * Created by zhangmint on 2017/6/7.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        WifiAdmin.init(this);
    }
}
