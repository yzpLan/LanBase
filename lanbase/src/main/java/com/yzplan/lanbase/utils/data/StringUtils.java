package com.yzplan.lanbase.utils.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串工具类
 */
public class StringUtils {

    /**
     * 判断字符串是否为空(包括对"null"字符串和纯空格)
     *
     * @param str 需要判断的字符串
     * @return true 为空; false 不为空
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty() || "null".equalsIgnoreCase(str.trim());
    }

    /**
     * 如果字符串为空，则返回默认值
     *
     * @param str      需要判断的字符串
     * @param defValue 默认值
     * @return 如果不为空返回 str，否则返回 defValue
     */
    public static String defaultIfEmpty(String str, String defValue) {
        return isNullOrEmpty(str) ? defValue : str;
    }

    /**
     * 隐藏手机号码中间四位 (脱敏)
     *
     * @param mobile 手机号
     * @return 格式化后的手机号，例如 138****0000
     */
    public static String hideMobile(String mobile) {
        if (isNullOrEmpty(mobile) || mobile.length() != 11) {
            return defaultIfEmpty(mobile, "");
        }
        // 将手机号的第4位到第7位替换为星号
        return mobile.substring(0, 3) + "****" + mobile.substring(7);
    }

    /**
     * 判断字符串是否为纯数字
     *
     * @param str 字符串
     * @return true 是纯数字; false 不是
     */
    public static boolean isNumeric(String str) {
        if (isNullOrEmpty(str)) {
            return false;
        }
        Pattern pattern = Pattern.compile("-?[0-9]+(\\.[0-9]+)?");
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }

    /**
     * 反转字符串
     *
     * @param str 字符串
     * @return 反转后的字符串
     */
    public static String reverse(String str) {
        if (isNullOrEmpty(str)) {
            return "";
        }
        return new StringBuilder(str).reverse().toString();
    }

    /**
     * 字符串填充 (Pad)：将源字符串使用指定字符前补或后补满指定长度
     *
     * @param source  源字符串
     * @param len     目标长度
     * @param fillStr 填充字符，例如 '0' 或 ' '
     * @param isLeft  true 为前补 (左对齐)，false 为后补 (右对齐)
     * @return 填充后的字符串
     */
    public static String pad(String source, int len, char fillStr, boolean isLeft) {
        if (isNullOrEmpty(source)) {
            source = "";
        }
        int sourceLen = source.length();
        if (sourceLen >= len) {
            return source;
        }

        StringBuilder sb = new StringBuilder();
        int paddingLen = len - sourceLen;

        // 1. 生成填充字符串
        for (int i = 0; i < paddingLen; i++) {
            sb.append(fillStr);
        }

        // 2. 拼接
        if (isLeft) {
            // 前补 (左对齐)：填充 + 源字符串
            return sb + source;
        } else {
            // 后补 (右对齐)：源字符串 + 填充
            return source + sb;
        }
    }
}