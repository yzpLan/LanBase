package com.yzplan.lanbase.utils.data;

import android.text.TextUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 金额处理工具类 (分与元的转换)
 */
public class MoneyUtils {

    /**
     * 【核心】分 转 元
     * 用于 UI 显示
     * 例: "100" -> "1.00"
     * 例: "1050" -> "10.50"
     */
    public static String fenToYuan(String amountFen) {
        if (TextUtils.isEmpty(amountFen)) {
            return "0.00";
        }
        try {
            // 使用 BigDecimal 进行精确运算
            BigDecimal fen = new BigDecimal(amountFen);
            return fen.movePointLeft(2).setScale(2, RoundingMode.HALF_UP).toString();
        } catch (Exception e) {
            return "0.00";
        }
    }

    /**
     * 【核心】元 转 分
     * 用于提交接口参数
     * 例: "10.50" -> "1050"
     * 例: "1" -> "100"
     */
    public static String yuanToFen(String amountYuan) {
        if (TextUtils.isEmpty(amountYuan)) {
            return "0";
        }
        try {
            BigDecimal yuan = new BigDecimal(amountYuan);
            return yuan.movePointRight(2).setScale(0, RoundingMode.HALF_UP).toString();
        } catch (Exception e) {
            return "0";
        }
    }

    /**
     * 格式化显示 (带人民币符号)
     * 例: "1050" -> "¥10.50"
     */
    public static String fenToYuanWithSymbol(String amountFen) {
        return "¥" + fenToYuan(amountFen);
    }
}
