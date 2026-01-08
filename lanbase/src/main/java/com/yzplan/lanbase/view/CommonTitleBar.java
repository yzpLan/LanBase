package com.yzplan.lanbase.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yzplan.lanbase.R;


/**
 * 通用标题栏封装
 */
public class CommonTitleBar extends FrameLayout {
    private View rootView;
    private ImageView ivBack;
    private TextView tvTitle;
    private TextView tvRightAction;
    private ImageView ivRightAction;
    private View vDivider;

    public CommonTitleBar(@NonNull Context context) {
        this(context, null);
    }

    public CommonTitleBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CommonTitleBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
        initAttrs(context, attrs);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.lib_layout_common_title_bar, this, true);
        rootView = findViewById(R.id.root_view);
        ivBack = findViewById(R.id.iv_back);
        tvTitle = findViewById(R.id.tv_title);
        tvRightAction = findViewById(R.id.tv_right_action);
        ivRightAction = findViewById(R.id.iv_right_action);
        vDivider = findViewById(R.id.v_divider);
        // 默认点击返回键关闭当前 Activity
        ivBack.setOnClickListener(v -> {
            if (context instanceof Activity) {
                ((Activity) context).finish();
            }
        });
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        if (attrs == null) return;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.LibCommonTitleBar);
        // 1. 标题
        String title = ta.getString(R.styleable.LibCommonTitleBar_ctb_title);
        if (!TextUtils.isEmpty(title)) {
            tvTitle.setText(title);
        }
        // 2. 标题颜色
        int titleColor = ta.getColor(R.styleable.LibCommonTitleBar_ctb_titleColor, Color.parseColor("#333333"));
        tvTitle.setTextColor(titleColor);
        // 3. 是否显示返回键
        boolean showBack = ta.getBoolean(R.styleable.LibCommonTitleBar_ctb_showBack, true);
        ivBack.setVisibility(showBack ? VISIBLE : GONE);
        // 4. 返回键图标
        int backRes = ta.getResourceId(R.styleable.LibCommonTitleBar_ctb_backRes, -1);
        if (backRes != -1) {
            ivBack.setImageResource(backRes);
        }
        // 5. 右侧文字
        String rightText = ta.getString(R.styleable.LibCommonTitleBar_ctb_rightText);
        if (!TextUtils.isEmpty(rightText)) {
            setRightText(rightText);
        }
        // 6. 右侧图标 (如果设置了图标，优先显示图标)
        int rightIcon = ta.getResourceId(R.styleable.LibCommonTitleBar_ctb_rightIcon, -1);
        if (rightIcon != -1) {
            setRightIcon(rightIcon);
        }
        // 7. 是否显示底线
        boolean showLine = ta.getBoolean(R.styleable.LibCommonTitleBar_ctb_showLine, true);
        vDivider.setVisibility(showLine ? VISIBLE : GONE);
        // 8. 背景颜色
        int bgColor = ta.getColor(R.styleable.LibCommonTitleBar_ctb_background, Color.WHITE);
        rootView.setBackgroundColor(bgColor);
        ta.recycle();
    }

    // ==================== 公开 API ====================

    /**
     * 设置标题
     */
    public void setTitle(CharSequence title) {
        tvTitle.setText(title);
    }

    /**
     * 设置标题颜色
     */
    public void setTitleColor(@ColorInt int color) {
        tvTitle.setTextColor(color);
    }

    /**
     * 设置标题加粗
     */
    public void setTitleBold(boolean isBold) {
        tvTitle.setTypeface(isBold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
    }

    /**
     * 设置左侧返回键点击事件（如果不设置，默认是 finish activity）
     */
    public void setOnBackListener(OnClickListener listener) {
        ivBack.setOnClickListener(listener);
    }

    /**
     * 设置返回键是否可见
     */
    public void setBackVisible(boolean visible) {
        ivBack.setVisibility(visible ? VISIBLE : GONE);
    }

    /**
     * 设置右侧文字及点击事件
     */
    public void setRightText(String text, OnClickListener listener) {
        tvRightAction.setVisibility(VISIBLE);
        ivRightAction.setVisibility(GONE); // 互斥
        tvRightAction.setText(text);
        if (listener != null) {
            tvRightAction.setOnClickListener(listener);
        }
    }

    /**
     * 仅设置右侧文字 (XML初始化时用到)
     */
    public void setRightText(String text) {
        setRightText(text, null);
    }

    /**
     * 设置右侧图标及点击事件
     */
    public void setRightIcon(@DrawableRes int resId, OnClickListener listener) {
        ivRightAction.setVisibility(VISIBLE);
        tvRightAction.setVisibility(GONE); // 互斥
        ivRightAction.setImageResource(resId);
        if (listener != null) {
            ivRightAction.setOnClickListener(listener);
        }
    }

    /**
     * 仅设置右侧图标
     */
    public void setRightIcon(@DrawableRes int resId) {
        setRightIcon(resId, null);
    }

    /**
     * 获取右侧文字控件（用于修改颜色等高级操作）
     */
    public TextView getRightTextView() {
        return tvRightAction;
    }

    /**
     * 控制底部分割线显隐
     */
    public void setDividerVisible(boolean visible) {
        vDivider.setVisibility(visible ? VISIBLE : GONE);
    }

    /**
     * 设置整个标题栏背景颜色
     */
    public void setBarBackgroundColor(@ColorInt int color) {
        rootView.setBackgroundColor(color);
    }
}