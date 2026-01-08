package com.yzplan.lanbase.manager;

import android.app.Activity;

import java.util.Stack;

/**
 * Activity 管理工具类
 */
public class ActivityManager {

    private static volatile ActivityManager instance;
    private Stack<Activity> activityStack;

    private ActivityManager() {
        activityStack = new Stack<>();
    }

    public static ActivityManager getInstance() {
        if (instance == null) {
            synchronized (ActivityManager.class) {
                if (instance == null) {
                    instance = new ActivityManager();
                }
            }
        }
        return instance;
    }

    /**
     * 添加 Activity 到堆栈
     */
    public void addActivity(Activity activity) {
        if (activityStack == null) {
            activityStack = new Stack<>();
        }
        activityStack.add(activity);
    }

    /**
     * 从堆栈移除 Activity
     */
    public void removeActivity(Activity activity) {
        if (activityStack != null) {
            activityStack.remove(activity);
        }
    }

    /**
     * 获取当前 Activity (堆栈中最后一个压入的)
     */
    public Activity currentActivity() {
        if (activityStack != null && !activityStack.isEmpty()) {
            return activityStack.lastElement();
        }
        return null;
    }

    /**
     * 结束指定类名的 Activity
     * 例如：finishActivity(LoginActivity.class);
     */
    public void finishActivity(Class<?> cls) {
        if (activityStack == null) return;
        // 遍历查找
        for (Activity activity : activityStack) {
            if (activity.getClass().equals(cls)) {
                finishActivity(activity);
                break;
            }
        }
    }

    /**
     * 结束指定的 Activity 实例 (辅助方法)
     */
    public void finishActivity(Activity activity) {
        if (activity != null) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
    }

    /**
     * 结束所有 Activity
     */
    public void finishAllActivity() {
        if (activityStack != null) {
            for (Activity activity : activityStack) {
                if (activity != null && !activity.isFinishing()) {
                    activity.finish();
                }
            }
            activityStack.clear();
        }
    }

    /**
     * 退出应用程序
     */
    public void exitApp() {
        try {
            finishAllActivity();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}