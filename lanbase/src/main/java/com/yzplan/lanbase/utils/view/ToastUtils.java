package com.yzplan.lanbase.utils.view;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.StringRes;

import com.yzplan.lanbase.BaseApp;


/**
 * Toast 工具类
 * 支持：子线程调用、防重复弹出、资源文件 ID 弹出
 */
public class ToastUtils {

    private static Toast sToast;
    // 使用主线程 Handler 确保线程安全
    private static final Handler sHandler = new Handler(Looper.getMainLooper());

    public static void showShort(CharSequence text) {
        show(text, Toast.LENGTH_SHORT);
    }

    public static void showLong(CharSequence text) {
        show(text, Toast.LENGTH_LONG);
    }

    public static void showShort(@StringRes int resId) {
        if (BaseApp.getContext() == null) return;
        show(BaseApp.getContext().getResources().getText(resId), Toast.LENGTH_SHORT);
    }

    /**
     * 核心显示逻辑：支持子线程调用
     */
    @SuppressLint("ShowToast")
    private static void show(final CharSequence text, final int duration) {
        if (BaseApp.getContext() == null) return;

        // 如果在子线程，则通过 Handler 切换回主线程
        if (Looper.myLooper() != Looper.getMainLooper()) {
            sHandler.post(() -> show(text, duration));
            return;
        }
        // 解决 Toast 重复弹出排队时间过长的问题
        if (sToast != null) {
            sToast.cancel();
        }
        sToast = Toast.makeText(BaseApp.getContext(), text, duration);
        sToast.show();
    }

}