package com.yzplan.lanbase.utils.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 时间工具类
 */
public class TimeUtils {

    // ================== 常用格式常量 ==================
    public static final String FORMAT_YMDHMS = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_YMD = "yyyy-MM-dd";
    public static final String FORMAT_YMD_CN = "yyyy年MM月dd日";
    public static final String FORMAT_HMS = "HH:mm:ss";
    public static final String FORMAT_FULL_SN = "yyyyMMddHHmmss";
    public static final String FORMAT_YMD_COMPACT = "yyyyMMdd";

    private TimeUtils() {
    }

    /**
     * 使用 ThreadLocal 保证 SimpleDateFormat 的线程安全，同时避免频繁创建对象的开销
     */
    private static final ThreadLocal<SimpleDateFormat> SDF_THREAD_LOCAL = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(FORMAT_YMDHMS, Locale.CHINA);
        }
    };

    /**
     * 获取当前线程的 SimpleDateFormat 实例，并应用指定的 Pattern
     */
    private static SimpleDateFormat getSafeDateFormat(String pattern) {
        SimpleDateFormat simpleDateFormat = SDF_THREAD_LOCAL.get();
        if (simpleDateFormat == null) {
            simpleDateFormat = new SimpleDateFormat(pattern, Locale.CHINA);
        } else {
            simpleDateFormat.applyPattern(pattern);
        }
        return simpleDateFormat;
    }

    // ================== 1. 获取当前时间 ==================

    /**
     * 获取当前时间字符串 (默认格式: yyyy-MM-dd HH:mm:ss)
     */
    public static String getNowString() {
        return dateToString(new Date(), FORMAT_YMDHMS);
    }

    /**
     * 获取当前时间字符串 (指定格式)
     */
    public static String getNowString(String format) {
        return dateToString(new Date(), format);
    }

    /**
     * 获取当前时间戳 (毫秒)
     */
    public static long getNowMills() {
        return System.currentTimeMillis();
    }

    /**
     * 获取当前时间Date
     */
    public static Date getNowDate() {
        return new Date();
    }

    /**
     * 获取当前时间Calendar
     */
    public static Calendar getNowCalendar() {
        return Calendar.getInstance();
    }


    // ================== 2. Date <-> String 转换 ==================

    /**
     * Date 转 字符串
     *
     * @param date   Date对象
     * @param format 格式
     * @return 格式化后的字符串
     */
    public static String dateToString(Date date, String format) {
        if (date == null) return "";
        return getSafeDateFormat(format).format(date);
    }

    /**
     * 字符串 转 Date
     *
     * @param timeString 时间字符串
     * @param format     格式 (必须与 timeString 匹配)
     * @return Date对象，解析失败返回 null
     */
    public static Date stringToDate(String timeString, String format) {
        if (timeString == null || timeString.isEmpty()) {
            return null;
        }
        try {
            return getSafeDateFormat(format).parse(timeString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ================== 3. Long(时间戳) <-> String 转换 ==================

    /**
     * 时间戳(毫秒) 转 字符串
     *
     * @param millis 时间戳
     * @param format 格式
     */
    public static String millisToString(long millis, String format) {
        return dateToString(new Date(millis), format);
    }

    /**
     * 字符串 转 时间戳(毫秒)
     *
     * @param timeString 时间字符串
     * @param format     对应的格式
     * @return 毫秒时间戳，失败返回 -1
     */
    public static long stringToMillis(String timeString, String format) {
        Date date = stringToDate(timeString, format);
        return date == null ? -1 : date.getTime();
    }

    // ================== 4. Calendar 与 Date/Long 转换 (新增) ==================

    /**
     * Date 转 Calendar
     * 用于：把后台返回的 Date 转成 Calendar 给 PickerView 回显
     */
    public static Calendar dateToCalendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        if (date != null) {
            calendar.setTime(date);
        }
        return calendar;
    }

    /**
     * 时间戳 转 Calendar
     * 用于：把保存的 long 时间戳转成 Calendar 给 PickerView 回显
     */
    public static Calendar millisToCalendar(long millis) {
        Calendar calendar = Calendar.getInstance();
        if (millis > 0) {
            calendar.setTimeInMillis(millis);
        }
        return calendar;
    }

    // ================== 5. 日期计算 ==================

    /**
     * 获取 N 天前的 Calendar 对象
     *
     * @param days 天数 (例如传 3，就是获取 3天前 的当前时间)
     * @return Calendar
     */
    public static Calendar getCalendarBefore(int days) {
        // 获取日历实例
        Calendar calendar = Calendar.getInstance();
        // 当前时间减去天数 (使用负数表示向前推)
        calendar.add(Calendar.DAY_OF_YEAR, -days);
        return calendar;
    }

    /**
     * 获取 N 天前的 Date 对象
     *
     * @param days 天数 (例如传 3，就是获取 3天前 的当前时间)
     * @return Date对象
     */
    public static Date getDateBefore(int days) {
        // 获取日历实例
        Calendar calendar = Calendar.getInstance();
        // 当前时间减去天数 (使用负数表示向前推)
        calendar.add(Calendar.DAY_OF_YEAR, -days);
        return calendar.getTime();
    }


    /**
     * 比较两个日期大小
     *
     * @return 1: dt1 > dt2 (dt1晚); -1: dt1 < dt2 (dt1早); 0: 相等
     */
    public static int compareDate(Date dt1, Date dt2) {
        return Long.compare(dt1.getTime(), dt2.getTime());
    }

    // ================== 6. 业务常用 ==================

    /**
     * 友好时间显示 (刚刚, x分钟前, x小时前, 昨天, 日期)
     */
    public static String getFriendlyTimeSpanByNow(String timeString, String format) {
        long now = System.currentTimeMillis();
        long span = now - stringToMillis(timeString, format);
        if (span < 0) return timeString;
        long second = 1000;
        long minute = 60 * second;
        long hour = 60 * minute;
        long day = 24 * hour;
        if (span < minute) {
            return "刚刚";
        } else if (span < hour) {
            return String.format(Locale.getDefault(), "%d分钟前", span / minute);
        } else if (span < day) {
            return String.format(Locale.getDefault(), "%d小时前", span / hour);
        } else if (span < 2 * day) {
            return "昨天";
        } else {
            // 超过2天，显示日期 (yyyy-MM-dd)
            return stringToDate(timeString, format) != null
                    ? dateToString(stringToDate(timeString, format), FORMAT_YMD)
                    : timeString;
        }
    }

}