package com.yzplan.lanbase.manager;


import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.yzplan.lanbase.BaseApp;
import com.yzplan.lanbase.utils.log.L;
import com.yzplan.lanbase.utils.log.LogFileUtils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 基础崩溃捕捉器
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "CrashHandler";

    private static CrashHandler sInstance;
    private Thread.UncaughtExceptionHandler mDefaultHandler;

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        if (sInstance == null) {
            sInstance = new CrashHandler();
        }
        return sInstance;
    }

    /**
     * 在 lib 层初始化
     */
    public void init() {
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        // 1. 提取堆栈字符串
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        // 2. 拼接设备基本信息，方便 lib 层通用排查
        String info = "Device: " + Build.BRAND + " " + Build.MODEL +
                "\nSDK: " + Build.VERSION.SDK_INT +
                "\nThread: " + t.getName() +
                "\nStack:\n" + sw;
        L.e(TAG, info);

        Log.e(BaseApp.sLogTag, info);

        // 3. 调用 lib 层自身的同步写入方法
        LogFileUtils.writeLogSync("CRASH", info);

        // 4. 移交系统处理或退出
        if (mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(t, e);
        } else {
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }
}
