package com.yzplan.lanbase;

import android.app.Application;
import android.content.Context;

import com.orhanobut.logger.Logger;
import com.yzplan.lanbase.utils.log.DefaultLogAdapter;
import com.yzplan.lanbase.utils.log.LogFileUtils;

/**
 * 基础库管家：负责管理全局 Context 和基础库的初始化
 */
public class BaseApp {
    // 全局 Application 实例
    private static Application sApplication;
    // 日志开关
    public static boolean sLogEnable = false;
    // 日志tag
    public static String sLogTag;
    // 是否开启文件日志存储
    public static boolean sLogToFile = false;

    /**
     * 初始化日志服务
     *
     * @param logTag 日志tag
     */
    public static void initLog(String logTag) {
        sLogEnable = true;
        sLogTag = logTag;
        Logger.clearLogAdapters();
        Logger.addLogAdapter(new DefaultLogAdapter());
    }

    /**
     * 初始化：在 App 启动时调用
     *
     * @param application 全局 Application
     */
    public static void init(Application application) {
        sApplication = application;
    }

    /**
     * 第二步：开启日志文件存储（可根据权限随时调用）
     *
     * @param logPath  存储路径
     * @param keepDays 保留天数
     */
    public static void openLogFileSave(String logPath, int keepDays) {
        sLogToFile = true;
        LogFileUtils.init(logPath);
        // 开启时顺便清理旧日志
        LogFileUtils.cleanExpiredLogs(keepDays);
    }

    /**
     * 获取全局 Context
     */
    public static Context getContext() {
        return sApplication.getApplicationContext();
    }

    /**
     * 获取Application 实例
     */
    public static Application getApplication() {
        return sApplication;
    }
}