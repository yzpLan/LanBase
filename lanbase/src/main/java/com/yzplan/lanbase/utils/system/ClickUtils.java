package com.yzplan.lanbase.utils.system;

import android.view.View;

import com.yzplan.lanbase.R;


public class ClickUtils {
    // 两次点击间隔不能少于 800ms
    private static final int MIN_DELAY_TIME = 800;

    /**
     * 判断是否是快速点击 (绑定到具体 View，互不影响)
     *
     * @param v 点击的控件
     * @return true=是快速点击(应拦截); false=非快速点击(通过)
     */
    public static boolean isFastClick(View v) {
        if (v == null) {
            return false;
        }
        long currentClickTime = System.currentTimeMillis();
        long lastClickTime = 0;
        // 1. 尝试从 View 的 Tag 中取出上次点击时间
        Object tag = v.getTag(R.id.lib_click_timestamp);
        if (tag != null) {
            lastClickTime = (long) tag;
        }
        // 2. 判断时间间隔
        if ((currentClickTime - lastClickTime) < MIN_DELAY_TIME) {
            // 防抖拦截
            return true;
        }
        // 3. 记录本次点击时间到 View 的 Tag 中
        v.setTag(R.id.lib_click_timestamp, currentClickTime);
        return false;
    }
}