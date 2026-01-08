package com.yzplan.lanbase.utils.view;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 日期选择工具类
 * 基于 com.contrarywind:Android-PickerView:4.1.9
 */
public class DatePickUtils {

    public static final String FMT_YMD = "yyyy-MM-dd";
    public static final String FMT_YMD_HMS = "yyyy-MM-dd HH:mm:ss";

    public interface OnDatePickedListener {
        void onPicked(Date date, String dateStr, long timestamp);
    }

    // ================== 1. 年月日 (yyyy-MM-dd) ==================

    /**
     * 简单版：默认当前时间，默认范围(前后10年)
     */
    public static void showDayPicker(Context context, OnDatePickedListener listener) {
        showDayPicker(context, null, null, null, listener);
    }

    /**
     * 进阶版：指定选中时间，默认范围(前后10年)
     */
    public static void showDayPicker(Context context, Calendar selectedDate, OnDatePickedListener listener) {
        showDayPicker(context, selectedDate, null, null, listener);
    }

    /**
     * 【完全体】：指定选中时间 + 指定起止范围
     * @param startRange 开始范围 (如：2020-01-01)
     * @param endRange   结束范围 (如：2030-12-31)
     */
    public static void showDayPicker(Context context, Calendar selectedDate, Calendar startRange, Calendar endRange, OnDatePickedListener listener) {
        boolean[] type = new boolean[]{true, true, true, false, false, false};
        show(context, "选择日期", type, FMT_YMD, selectedDate, startRange, endRange, listener);
    }

    // ================== 2. 年月日 时分秒 (yyyy-MM-dd HH:mm:ss) ==================

    /**
     * 简单版
     */
    public static void showTimePicker(Context context, OnDatePickedListener listener) {
        showTimePicker(context, null, null, null, listener);
    }

    /**
     * 进阶版
     */
    public static void showTimePicker(Context context, Calendar selectedDate, OnDatePickedListener listener) {
        showTimePicker(context, selectedDate, null, null, listener);
    }

    /**
     * 【完全体】：指定选中时间 + 指定起止范围
     */
    public static void showTimePicker(Context context, Calendar selectedDate, Calendar startRange, Calendar endRange, OnDatePickedListener listener) {
        boolean[] type = new boolean[]{true, true, true, true, true, true};
        show(context, "选择时间", type, FMT_YMD_HMS, selectedDate, startRange, endRange, listener);
    }

    // ================== 核心构建方法 ==================

    /**
     * @param startRange 开始时间范围 (传入 null 则使用默认：当前时间往前推10年)
     * @param endRange   结束时间范围 (传入 null 则使用默认：当前时间往后推10年)
     */
    private static void show(Context context, String title, boolean[] type, String format,
                             Calendar selectedDate, Calendar startRange, Calendar endRange,
                             OnDatePickedListener listener) {
        // 1. 处理默认选中时间
        if (selectedDate == null) {
            selectedDate = Calendar.getInstance();
        }

        // 2. 处理时间范围 (如果你不传，为了防止PickerView显示异常，这里还是给个兜底默认值)
        if (startRange == null) {
            startRange = Calendar.getInstance();
            startRange.add(Calendar.YEAR, -10); // 默认兜底：前10年
        }
        if (endRange == null) {
            endRange = Calendar.getInstance();
            endRange.add(Calendar.YEAR, 10);    // 默认兜底：后10年
        }

        // 3. 构建选择器
        TimePickerView pvTime = new TimePickerBuilder(context, new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {
                if (listener != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.CHINA);
                    String str = sdf.format(date);
                    listener.onPicked(date, str, date.getTime());
                }
            }
        })
                .setType(type)
                .setTitleText(title)
                .setTitleSize(16)
                .setTitleColor(Color.BLACK)
                .setSubmitColor(Color.parseColor("#E63D38"))
                .setCancelColor(Color.GRAY)
                .setDate(selectedDate)
                .setRangDate(startRange, endRange) // 【关键】设置用户传入的范围
                .setLabel("年", "月", "日", "时", "分", "秒")
                .isCenterLabel(false)
                .setOutSideCancelable(true)
                .build();

        pvTime.show();
    }
}