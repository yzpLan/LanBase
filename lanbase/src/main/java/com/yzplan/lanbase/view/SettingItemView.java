package com.yzplan.lanbase.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.yzplan.lanbase.R;

public class SettingItemView extends LinearLayout {

    private LinearLayout llRoot;
    private ImageView ivIcon;
    private TextView tvTitle;
    private TextView tvRightText;
    private ImageView ivArrow;

    public SettingItemView(@NonNull Context context) { this(context, null); }
    public SettingItemView(@NonNull Context context, @Nullable AttributeSet attrs) { this(context, attrs, 0); }
    public SettingItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
        initAttrs(context, attrs);
        transferPadding();
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.lib_widget_setting_item, this, true);
        llRoot = findViewById(R.id.ll_root);
        ivIcon = findViewById(R.id.iv_icon);
        tvTitle = findViewById(R.id.tv_title);
        tvRightText = findViewById(R.id.tv_right_text);
        ivArrow = findViewById(R.id.iv_arrow);

        // 必须去掉自身的背景，否则会有两层点击效果或截断效果
        super.setBackgroundResource(0);
    }

    private void transferPadding() {
        // 获取外部 XML 设置给 SettingItemView 的 padding
        int pL = getPaddingLeft();
        int pT = getPaddingTop();
        int pR = getPaddingRight();
        int pB = getPaddingBottom();

        // 如果设置了 padding，则转移
        if (pL != 0 || pT != 0 || pR != 0 || pB != 0) {
            if (llRoot != null) {
                llRoot.setPadding(pL, pT, pR, pB);
            }
            // 自身清空，确保背景铺满
            super.setPadding(0, 0, 0, 0);
        }
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SettingItemView);

        // 1. 图标内容与大小
        int iconResId = ta.getResourceId(R.styleable.SettingItemView_siv_icon, 0);
        int iconSize = ta.getDimensionPixelSize(R.styleable.SettingItemView_siv_icon_size, dp2px(18));
        if (iconResId != 0) {
            ivIcon.setVisibility(VISIBLE);
            ivIcon.setImageResource(iconResId);
            updateViewSize(ivIcon, iconSize, iconSize);
        } else {
            ivIcon.setVisibility(GONE);
        }

        // 2. 标题内容与样式
        tvTitle.setText(ta.getString(R.styleable.SettingItemView_siv_title));
        int titleSize = ta.getDimensionPixelSize(R.styleable.SettingItemView_siv_title_size, sp2px(16));
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleSize);
        tvTitle.setTextColor(ta.getColor(R.styleable.SettingItemView_siv_title_color, Color.parseColor("#333333")));

        // 3. 右侧文字内容与样式
        tvRightText.setText(ta.getString(R.styleable.SettingItemView_siv_right_text));
        int rtSize = ta.getDimensionPixelSize(R.styleable.SettingItemView_siv_right_text_size, sp2px(14));
        tvRightText.setTextSize(TypedValue.COMPLEX_UNIT_PX, rtSize);
        tvRightText.setTextColor(ta.getColor(R.styleable.SettingItemView_siv_right_text_color, Color.parseColor("#999999")));

        // 4. 箭头控制
        boolean showArrow = ta.getBoolean(R.styleable.SettingItemView_siv_show_arrow, true);
        ivArrow.setVisibility(showArrow ? VISIBLE : GONE);
        int arrowSize = ta.getDimensionPixelSize(R.styleable.SettingItemView_siv_arrow_size, dp2px(16));
        updateViewSize(ivArrow, arrowSize, arrowSize);
        ivArrow.setColorFilter(ta.getColor(R.styleable.SettingItemView_siv_arrow_color, Color.parseColor("#CCCCCC")));

        ta.recycle();
    }

    private void updateViewSize(View view, int w, int h) {
        android.view.ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = w;
        params.height = h;
        view.setLayoutParams(params);
    }

    private int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private int sp2px(float sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }

    // --- 标题 (Title) 相关 ---
    public SettingItemView setTitle(String text) {
        tvTitle.setText(text);
        return this;
    }

    public SettingItemView setTitleColor(int color) {
        tvTitle.setTextColor(color);
        return this;
    }

    public SettingItemView setTitleSize(float sp) {
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        return this;
    }

    // --- 右侧文字 (RightText) 相关 ---
    public SettingItemView setRightText(String text) {
        tvRightText.setText(text);
        return this;
    }

    public SettingItemView setRightTextColor(int color) {
        tvRightText.setTextColor(color);
        return this;
    }

    public SettingItemView setRightTextSize(float sp) {
        tvRightText.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        return this;
    }

    // --- 图标 (Icon) 相关 ---
    public SettingItemView setIcon(int resId) {
        if (resId != 0) {
            ivIcon.setVisibility(VISIBLE);
            ivIcon.setImageResource(resId);
        } else {
            ivIcon.setVisibility(GONE);
        }
        return this;
    }

    public SettingItemView setIconSize(int dp) {
        int px = dp2px(dp);
        updateViewSize(ivIcon, px, px);
        return this;
    }

    // --- 箭头 (Arrow) 相关 ---
    public SettingItemView setArrowVisible(boolean visible) {
        ivArrow.setVisibility(visible ? VISIBLE : GONE);
        return this;
    }

    public SettingItemView setArrowColor(int color) {
        ivArrow.setColorFilter(color);
        return this;
    }

    // --- 点击事件 ---
    public SettingItemView setOnItemClickListener(OnClickListener listener) {
        if (llRoot != null) {
            llRoot.setOnClickListener(listener);
        }
        return this;
    }
}