package com.yzplan.lanbase.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.yzplan.lanbase.R;
import com.yzplan.lanbase.manager.SingleClickListener;
import com.yzplan.lanbase.utils.data.TimeUtils;
import com.yzplan.lanbase.utils.view.DatePickUtils;
import com.yzplan.lanbase.utils.view.ToastUtils;

import java.util.Calendar;
import java.util.Date;

/**
 * 通用日期/时间选择器
 * 1. 支持精度切换：仅日期(DAY) 或 日期+时间(TIME)
 * 2. 支持模式切换：范围选择 或 单日期选择(is_single)
 * 3. 支持样式自定义：XML 属性配置 或 代码链式调用
 */
public class DatePickerView extends LinearLayout {
    private TextView tvStart, tvEnd, tvTo;
    private Date startDate, endDate;

    private DateMode dateMode = DateMode.TIME; // 默认时间精度
    private boolean isSingle = false;          // 默认范围模式
    private int limitDays = 180;               // 默认限制天数
    private OnDateChangeListener listener;

    public enum DateMode {DAY, TIME}

    public interface OnDateChangeListener {
        /**
         * @param start 开始时间 (单选模式下与end相同)
         * @param end   结束时间
         */
        void onDateChanged(Date start, Date end);
    }

    public DatePickerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
        parseAttrs(context, attrs);
        updateUI();
    }

    private void init(Context context) {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        // 绑定 XML 布局
        LayoutInflater.from(context).inflate(R.layout.lib_view_date_range_picker, this, true);
        tvStart = findViewById(R.id.tv_start_date);
        tvEnd = findViewById(R.id.tv_end_date);
        tvTo = findViewById(R.id.tv_to);

        startDate = TimeUtils.getNowDate();
        endDate = TimeUtils.getNowDate();

        // 绑定点击事件 (防抖点击)
        tvStart.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                showPicker(true);
            }
        });
        tvEnd.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                showPicker(false);
            }
        });
    }

    /**
     * 解析自定义属性
     */
    private void parseAttrs(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.DatePickerView);

            // 1. 逻辑属性解析
            int modeIndex = ta.getInt(R.styleable.DatePickerView_picker_mode, 1);
            dateMode = (modeIndex == 0) ? DateMode.DAY : DateMode.TIME;
            isSingle = ta.getBoolean(R.styleable.DatePickerView_is_single, false);
            limitDays = ta.getInt(R.styleable.DatePickerView_limit_days, 180);
            // 2. UI 样式解析 (App 层设置则覆盖，不设置保留 Lib 默认样式)
            int dividerColor = ta.getColor(R.styleable.DatePickerView_picker_divider_color, -1);
            if (dividerColor != -1) {
                setDividerColor(dividerColor);
            }
            int bgRes = ta.getResourceId(R.styleable.DatePickerView_picker_item_background, -1);
            if (bgRes != -1) {
                setItemBackgroundResource(bgRes);
            }
            String dividerText = ta.getString(R.styleable.DatePickerView_picker_divider_text);
            if (dividerText != null) {
                setDividerText(dividerText);
            }
            ta.recycle();
        }
    }

    /**
     * 弹出选择器
     */
    private void showPicker(boolean isStart) {
        Calendar current = TimeUtils.dateToCalendar(isStart ? startDate : endDate);
        Calendar min = TimeUtils.getCalendarBefore(limitDays);
        Calendar max = TimeUtils.getNowCalendar();

        if (dateMode == DateMode.TIME) {
            DatePickUtils.showTimePicker(getContext(), current, min, max, (date, dateStr, timestamp) -> handleResult(isStart, date));
        } else {
            DatePickUtils.showDayPicker(getContext(), current, min, max, (date, dateStr, timestamp) -> handleResult(isStart, date));
        }
    }

    /**
     * 处理选择结果
     */
    private void handleResult(boolean isStart, Date date) {
        if (isSingle) {
            startDate = date;
            endDate = date; // 单选模式强制同步
        } else {
            if (isStart) {
                if (TimeUtils.compareDate(date, endDate) > 0) {
                    ToastUtils.showShort("开始时间不能晚于结束时间");
                    return;
                }
                startDate = date;
            } else {
                if (TimeUtils.compareDate(date, startDate) < 0) {
                    ToastUtils.showShort("结束时间不能早于开始时间");
                    return;
                }
                endDate = date;
            }
        }
        updateUI();
        if (listener != null) listener.onDateChanged(startDate, endDate);
    }

    /**
     * 刷新 UI 状态
     */
    private void updateUI() {
        // 单选模式隐藏结束日期
        int visibility = isSingle ? GONE : VISIBLE;
        tvEnd.setVisibility(visibility);
        tvTo.setVisibility(visibility);

        // 根据精度格式化输出
        String format = (dateMode == DateMode.TIME) ? TimeUtils.FORMAT_YMDHMS : TimeUtils.FORMAT_YMD;
        tvStart.setText(TimeUtils.dateToString(startDate, format));
        tvEnd.setText(TimeUtils.dateToString(endDate, format));
    }

    // ================== 对外 API (支持链式调用) ==================

    /**
     * 设置精度模式 (DAY/TIME)
     */
    public DatePickerView setDateMode(DateMode mode) {
        this.dateMode = mode;
        updateUI();
        return this;
    }

    /**
     * 设置是否为单日期模式
     */
    public DatePickerView setSingle(boolean single) {
        this.isSingle = single;
        updateUI();
        return this;
    }

    /**
     * 设置限制范围(天数)
     */
    public DatePickerView setLimitDays(int days) {
        this.limitDays = days;
        return this;
    }

    /**
     * 设置时间框背景资源
     */
    public DatePickerView setItemBackgroundResource(int resId) {
        if (tvStart != null) tvStart.setBackgroundResource(resId);
        if (tvEnd != null) tvEnd.setBackgroundResource(resId);
        return this;
    }

    /**
     * 代码设置中间分隔符颜色
     */
    public DatePickerView setDividerColor(int color) {
        if (tvTo != null) tvTo.setTextColor(color);
        return this;
    }

    /**
     * 设置中间分隔符文字
     */
    public DatePickerView setDividerText(String text) {
        if (tvTo != null) tvTo.setText(text);
        return this;
    }

    /**
     * 设置默认日期
     */
    public DatePickerView setDefaultDate(Date start, Date end) {
        this.startDate = start;
        this.endDate = end;
        updateUI();
        return this;
    }

    /**
     * 设置日期变更监听
     */
    public DatePickerView setOnDateChangeListener(OnDateChangeListener listener) {
        this.listener = listener;
        return this;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }
}