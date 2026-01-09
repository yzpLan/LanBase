package com.yzplan.lanbase.app;

import android.app.Application;

import com.alibaba.android.arouter.launcher.ARouter;
import com.yzplan.lanbase.BaseApp;


public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        initARouter();
        initBase();

    }

    private void initARouter() {
        if (BuildConfig.DEBUG) {
            ARouter.openLog();     // 打印日志
            ARouter.openDebug();   // 开启调试模式
        }
        ARouter.init(this);
    }

    private void initBase() {
        BaseApp.init(this);
        BaseApp.initLog("YZPP");
        String path = "/mnt/sdcard/mtms/log/outapp/" + getPackageName();
        BaseApp.openLogFileSave(path, 15);
    }
}
