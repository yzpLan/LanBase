package com.yzplan.lanbase.utils.data;

import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * 订单号工具类
 */
public class OrderIdUtils {
    // 订单号总长度
    private static final int ORDER_LENGTH = 20;
    // 订单生成的时间格式 (精确到毫秒)
    private static final String TIME_FORMAT_ORDER = "yyyyMMddHHmmssSSS";
    // UI显示的时间格式 (通常不需要显示毫秒)
    private static final String TIME_FORMAT_DISPLAY = "yyyy-MM-dd HH:mm:ss";
    // 原子计数器，保证同一毫秒内的序列唯一 (0 - 999)
    private static final AtomicInteger atomicInteger = new AtomicInteger(0);

    /**
     * 生成本地唯一订单号
     * 格式：17位时间戳 + 3位递增序列
     * 例：20231212103055888001
     */
    public static String getLocalTermOdrId() {
        // 1. 获取当前时间 (17位)
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT_ORDER, Locale.CHINA);
        String timeStr = sdf.format(new Date());
        // 2. 获取递增序列 (0-999 循环)
        int seq = atomicInteger.getAndIncrement() % 1000;
        // 如果是负数（溢出情况），取绝对值
        if (seq < 0) seq = Math.abs(seq);
        // 3. 补齐3位 (例如 5 -> 005)
        String seqStr = String.format(Locale.CHINA, "%03d", seq);
        // 4. 拼接
        return timeStr + seqStr;
    }

    // ================== 解析与校验 ==================

    /**
     * 判断是否符合我们的订单号规则 (严格校验版)
     * 1. 长度必须 20 位
     * 2. 必须全数字
     * 3. 前 17 位必须是合法的日期 (利用 SimpleDateFormat 严格模式)
     * 4. 年份必须在合理范围内 (例如 2020 - 2099)，防止解析出 1800 年这种怪数据
     */
    public static boolean isMyOrderType(String orderId) {
        // 1. 基础校验：空、长度、纯数字
        if (TextUtils.isEmpty(orderId)
                || orderId.length() != ORDER_LENGTH
                || !isNumeric(orderId)) {
            return false;
        }
        // 2. 截取时间部分 (前17位)
        String timePart = orderId.substring(0, 17);
        // 3. 尝试解析时间
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT_ORDER, Locale.CHINA);
        // 【关键】设置为严格模式！
        // 默认为 true (宽容模式)，它会把 "2023-13-01" 解析成 "2024-01-01"。
        // 设置为 false 后，如果月份 > 12 或日期不对，直接抛异常，校验更准。
        sdf.setLenient(false);
        try {
            Date date = sdf.parse(timePart);
            // 4. 【可选】逻辑合理性校验 (比如年份必须在 2020~2030 之间)
            // 这一步是为了防止 000000... 或者 999999... 这种极其离谱的数字通过校验
            if (date != null) {
                // 简单的年份检查：取年份的前4位字符串转int (最快的方式)
                int year = Integer.parseInt(orderId.substring(0, 4));
                // 假设你的业务是 2020 年开始的，肯定不会超过 2099 年
                return year >= 2020 && year <= 2099;
            }
            return true;
        } catch (ParseException e) {
            // 解析失败，说明前17位不是有效的时间 (比如 20231301...)
            return false;
        }
    }

    /**
     * 从订单号中提取时间字符串 (用于UI显示)
     * 输入：20231212103055888001
     * 输出：2023-12-12 10:30:55
     */
    public static String getOrderTimeStr(String orderId) {
        Date date = getOrderDate(orderId);
        if (date == null) {
            return "";
        }
        // 显示的时候通常不需要毫秒，给用户看秒即可
        SimpleDateFormat displaySdf = new SimpleDateFormat(TIME_FORMAT_DISPLAY, Locale.CHINA);
        return displaySdf.format(date);
    }

    /**
     * 从订单号中解析出 Date 对象
     */
    public static Date getOrderDate(String orderId) {
        if (!isMyOrderType(orderId)) {
            return null;
        }
        try {
            String timePart = orderId.substring(0, 17);
            SimpleDateFormat orderSdf = new SimpleDateFormat(TIME_FORMAT_ORDER, Locale.CHINA);
            return orderSdf.parse(timePart);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取时间戳 (long)
     */
    public static long getOrderTimestamp(String orderId) {
        Date date = getOrderDate(orderId);
        return date != null ? date.getTime() : 0;
    }

    /**
     * 正则校验数字
     */
    private static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }
}