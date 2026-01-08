package com.yzplan.lanbase.utils.view;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * 状态栏工具类
 */
public class StatusBarUtils {

    /**
     * 设置状态栏颜色和文字模式
     *
     * @param activity   当前 Activity
     * @param color      背景颜色值 (例如: ContextCompat.getColor(context, R.color.xxx))
     * @param isTextDark true = 黑色文字(用于浅色背景); false = 白色文字(用于深色背景)
     */
    public static void setStatusBar(Activity activity, int color, boolean isTextDark) {
        if (activity == null) return;
        Window window = activity.getWindow();
        // 1. 设置背景颜色
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(color);
        // 2. 设置字体颜色 (必须判断 API 23，否则低版本会报错或无效)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = window.getDecorView();
            int visibility = decorView.getSystemUiVisibility();
            if (isTextDark) {
                // 浅色背景 -> 设置黑色字体
                visibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                // 深色背景 -> 清除标记，恢复白色字体
                visibility &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            decorView.setSystemUiVisibility(visibility);
        }
    }

    /**
     * 图片背景页面：沉浸式状态栏（内容延伸到状态栏底部）
     *
     * @param activity   当前 Activity
     * @param isTextDark true = 黑色文字; false = 白色文字
     */
    public static void setTransparentStatusBar(Activity activity, boolean isTextDark) {
        if (activity == null) return;
        Window window = activity.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(0x00000000);
        int visibility = window.getDecorView().getSystemUiVisibility();
        // 布局内容延伸到状态栏
        visibility |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        // 设置文字颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isTextDark) {
                visibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                visibility &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
        }
        window.getDecorView().setSystemUiVisibility(visibility);
    }

    /**
     * 设置全屏模式（隐藏状态栏）
     * 适用于：启动页、全屏视频页
     *
     * @param activity 当前 Activity
     */
    public static void setFullScreen(Activity activity) {
        if (activity == null) return;
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
}