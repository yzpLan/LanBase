package com.yzplan.lanbase.utils.log;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.LogAdapter;
import com.orhanobut.logger.PrettyFormatStrategy;
import com.yzplan.lanbase.BaseApp;

public class DefaultLogAdapter implements LogAdapter {

    private final FormatStrategy formatStrategy;

    public DefaultLogAdapter() {
        formatStrategy = PrettyFormatStrategy.newBuilder()
                .tag(BaseApp.sLogTag)
                // 始终显示 1 行方法栈信息（类名、行号）
                .methodCount(1)
                // 始终跳过包装层级，精准指向业务代码
                .methodOffset(2)
                // 始终显示当前打印日志的线程名
                .showThreadInfo(true)
                .build();
    }

    @Override
    public boolean isLoggable(int priority, @Nullable String tag) {
        return BaseApp.sLogEnable || BaseApp.sLogToFile; // 全局开关控制
    }

    @Override
    public void log(int priority, @Nullable String tag, @NonNull String message) {
        // 1. 控制台打印
        if (BaseApp.sLogEnable) {
            formatStrategy.log(priority, tag, message);
        }
        // 2. 日志输出至文件
        if (BaseApp.sLogToFile && shouldWriteFile(priority)) {
            String fullTag = BaseApp.sLogTag + "-" + (tag == null ? "" : tag);
            String details = getMethodDetails();
            String line = String.format("%s | %s | %s | %s", level(priority), Thread.currentThread().getName(), details, message);
            LogFileUtils.writeLogAsync(fullTag, line);
        }
    }

    private boolean shouldWriteFile(int priority) {
        return priority >= Log.INFO; // 生产环境只记录 INFO 以上日志
    }

    /**
     * 精准获取调用处的方法详情
     * 适配：混淆环境、RxJava异步、Android 5.0+ 虚拟机
     */
    private String getMethodDetails() {
        // 1. 获取当前线程堆栈
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            if (className == null) continue;
            // 2. 深度过滤：排除系统、虚拟机、日志库、封装类及常用框架层
            if (!className.contains("java.lang.Thread") &&
                    !className.contains("dalvik.system.") &&
                    !className.contains("com.android.internal.") &&
                    !className.contains("com.orhanobut.logger") &&
                    !className.contains("DefaultLogAdapter") &&
                    !className.contains(".utils.log.L") &&
                    !className.contains("io.reactivex") &&
                    !className.contains("okhttp3")) {
//                // 3. 处理类名：获取简短类名（兼容无包名或混淆后的情况）
//                int lastDot = className.lastIndexOf(".");
//                 className = (lastDot == -1) ? className : className.substring(lastDot + 1);
                // 4. 处理行号：混淆后 LineNumberTable 可能被移除导致返回 -1
                int line = element.getLineNumber();
                String lineStr = (line >= 0) ? String.valueOf(line) : "UnknownLine";
                // 5. 组装格式：ClassName.MethodName(LineNumber)
                return className + "." + element.getMethodName() + "(" + lineStr + ")";
            }
        }
        return "Unknown";
    }

    private String level(int p) {
        switch (p) {
            case Log.VERBOSE:
                return "V";
            case Log.DEBUG:
                return "D";
            case Log.INFO:
                return "I";
            case Log.WARN:
                return "W";
            case Log.ERROR:
                return "E";
            default:
                return "U";
        }
    }
}
