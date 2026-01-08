package com.yzplan.lanbase.utils.system;

import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * 键盘控制工具类
 */
public class KeyboardUtils {

    /**
     * 自动处理点击空白处隐藏软键盘的逻辑
     * 建议在 BaseActivity 的 dispatchTouchEvent 中调用
     */
    public static void autoHideKeyboard(Activity activity, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = activity.getCurrentFocus();
            if (isShouldHideKeyboard(v, event)) {
                hideSoftInput(activity, v.getWindowToken());
            }
        }
    }

    /**
     * 判断是否需要隐藏键盘
     */
    private static boolean isShouldHideKeyboard(View v, MotionEvent event) {
        if (v instanceof EditText) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left + v.getWidth();
            // 如果点击的是 EditText 区域，则返回 false (不隐藏)
            // 点击的是外部区域，返回 true (隐藏)
            return !(event.getX() > left && event.getX() < right && event.getY() > top && event.getY() < bottom);
        }
        return false;
    }

    /**
     * 执行隐藏软键盘
     */
    private static void hideSoftInput(Activity activity, android.os.IBinder token) {
        if (token != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }
}