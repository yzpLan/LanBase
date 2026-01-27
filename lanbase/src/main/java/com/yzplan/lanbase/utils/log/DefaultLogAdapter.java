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
                .methodCount(0)
                .showThreadInfo(true)
                .build();
    }

    @Override
    public boolean isLoggable(int priority, @Nullable String tag) {
        return BaseApp.sLogEnable || BaseApp.sLogToFile;
    }

    @Override
    public void log(int priority, @Nullable String tag, @NonNull String message) {
        // 获取精准的堆栈元素
        StackTraceElement targetElement = getTargetStackElement();

        // 关键点 2：构造符合 Logcat 跳转规则的字符串格式: "at 全类名.方法名(文件名:行号)"
        String clickableStack = "Unknown";
        if (targetElement != null) {
            clickableStack = String.format("at %s.%s(%s:%d)",
                    targetElement.getClassName(),
                    targetElement.getMethodName(),
                    targetElement.getFileName(),
                    targetElement.getLineNumber());
        }

        // 1. 控制台打印：将可点击的堆栈信息拼接到消息头部
        if (BaseApp.sLogEnable) {
            // 在消息前加上换行和 at 信息，Android Studio 会自动识别为蓝链
            formatStrategy.log(priority, tag, clickableStack + "\n" + message);
        }

        // 2. 日志输出至文件
        if (BaseApp.sLogToFile && priority >= Log.INFO) {
            String fullTag = BaseApp.sLogTag + "-" + (tag == null ? "" : tag);
            String line = String.format("%s | %s | %s | %s",
                    level(priority),
                    Thread.currentThread().getName(),
                    clickableStack,
                    message);
            LogFileUtils.writeLogAsync(fullTag, line);
        }
    }

    /**
     * 自动穿透基类，获取真实的业务代码调用处
     */
    private StackTraceElement getTargetStackElement() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        boolean foundLogTool = false;

        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            if (className == null) continue;

            // 首先定位到日志工具类本身
            if (className.contains(".utils.log.L") || className.contains("DefaultLogAdapter")) {
                foundLogTool = true;
                continue;
            }

            // 一旦经过了工具类，接下来的第一个“非基类、非框架类”就是我们要的业务代码
            if (foundLogTool) {
                if (isTargetClass(className)) {
                    return element;
                }
            }
        }
        return null;
    }

    /**
     * 过滤掉所有的基类和系统框架类，确保定位到具体的子类
     */
    private boolean isTargetClass(String className) {
        return !className.startsWith("java.") &&
                !className.startsWith("android.") &&
                !className.startsWith("dalvik.") &&
                !className.startsWith("com.orhanobut.logger");
    }

    private String level(int p) {
        switch (p) {
            case Log.VERBOSE: return "V";
            case Log.DEBUG: return "D";
            case Log.INFO: return "I";
            case Log.WARN: return "W";
            case Log.ERROR: return "E";
            default: return "U";
        }
    }
}