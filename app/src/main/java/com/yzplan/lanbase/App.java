package com.yzplan.lanbase;

import android.app.Application;


public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        BaseApp.init(this);
        BaseApp.initLog("YZPP");
        String path = "/mnt/sdcard/mtms/log/outapp/" + getPackageName();
        BaseApp.openLogFileSave(path, 15);
    }
}
