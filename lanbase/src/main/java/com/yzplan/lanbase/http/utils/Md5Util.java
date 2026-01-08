package com.yzplan.lanbase.http.utils;

import android.text.TextUtils;

import com.yzplan.lanbase.utils.data.JsonUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Md5加密与签名工具类
 */
public class Md5Util {

    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * 获取文件的 MD5 值
     * 使用流分块读取，防止大文件 OOM，并自动关闭流
     *
     * @param file 文件对象
     * @return 文件的 MD5 字符串，失败返回空字符串
     */
    public static String getFileMD5(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return "";
        }
        FileInputStream fis = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            fis = new FileInputStream(file);
            byte[] buffer = new byte[8192]; // 8KB 缓冲区
            int len;
            while ((len = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, len);
            }
            return bytesToHex(digest.digest());
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        } finally {
            // 必须关闭流，防止资源泄露
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 计算字符串的 MD5
     *
     * @param s 待加密字符串
     * @return 加密后的字符串
     */
    public static String md5(String s) {
        if (TextUtils.isEmpty(s)) {
            return "";
        }
        try {
            byte[] btInput = s.getBytes(StandardCharsets.UTF_8);
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(btInput);
            byte[] md = mdInst.digest();
            return bytesToHex(md);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 将字节数组转换为 16 进制字符串 (内部复用方法)
     */
    private static String bytesToHex(byte[] bytes) {
        if (bytes == null) return "";
        char[] result = new char[bytes.length * 2];
        int index = 0;
        for (byte b : bytes) {
            result[index++] = HEX_DIGITS[(b >>> 4) & 0xf];
            result[index++] = HEX_DIGITS[b & 0xf];
        }
        return new String(result);
    }

    /**
     * 签名算法
     * 逻辑：Key 升序排序 -> 拼接 Value -> 拼接盐 -> MD5
     *
     * @param parameter 请求参数 Map
     * @param salt      盐值
     * @return 签名字符串
     */
    public static String sign(Map<String, Object> parameter, String salt) {
        if (parameter == null) {
            return md5(salt);
        }
        SortedMap<String, Object> sortedMap = new TreeMap<>(parameter);
        StringBuilder sb = new StringBuilder();
        for (Object value : sortedMap.values()) {
            if (value != null) {
                String strVal;
                if (value instanceof Map || value instanceof java.util.List) {
                    strVal = JsonUtil.toJson(value);
                } else {
                    strVal = value.toString();
                }
                sb.append(strVal);
            }
        }
        String originalString = sb.toString();
        return md5(originalString + salt);
    }

}