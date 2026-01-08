package com.yzplan.lanbase.utils.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;

/**
 * 通用文件原子操作工具类
 */
public class FileUtils {

    /**
     * 删除文件或递归删除目录
     */
    public static void delete(File file) {
        if (file == null || !file.exists()) return;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) delete(f);
            }
        }
        file.delete();
    }

    /**
     * 格式化文件大小 (B, KB, MB, GB, TB)
     */
    public static String formatSize(long size) {
        if (size <= 0) return "0B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        if (digitGroups >= units.length) digitGroups = units.length - 1;
        return new DecimalFormat("#,##0.#")
                .format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    /**
     * 安全创建父目录
     */
    public static boolean makeParentDirs(File file) {
        if (file == null) return false;
        File parent = file.getParentFile();
        return parent != null && (parent.exists() || parent.mkdirs());
    }

    /**
     * 复制文件（基于 NIO，效率最高）
     */
    public static boolean copyFile(File src, File dest) {
        if (src == null || !src.exists() || dest == null) return false;
        try (FileChannel in = new FileInputStream(src).getChannel();
             FileChannel out = new FileOutputStream(dest).getChannel()) {
            in.transferTo(0, in.size(), out);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取文件或目录的总大小
     */
    public static long getSize(File file) {
        if (file == null || !file.exists()) return 0;
        if (!file.isDirectory()) return file.length();
        long size = 0;
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) size += getSize(f);
        }
        return size;
    }
}