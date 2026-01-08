package com.yzplan.lanbase.manager;

import android.view.View;

import com.yzplan.lanbase.utils.system.ClickUtils;


/**
 * 防抖点击监听器 (基于 ClickUtils)
 */
public abstract class SingleClickListener implements View.OnClickListener {

    @Override
    public void onClick(View v) {
        // 直接调用核心工具类判断
        if (ClickUtils.isFastClick(v)) {
            return;
        }
        onSingleClick(v);
    }

    /**
     * 子类需实现此方法，处理点击逻辑 (已过滤快速点击)
     */
    public abstract void onSingleClick(View v);
}