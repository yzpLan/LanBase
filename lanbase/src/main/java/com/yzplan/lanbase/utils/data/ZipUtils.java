package com.yzplan.lanbase.utils.data;


import com.yzplan.lanbase.utils.log.L;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils {
    private static final String TAG = "ZipUtils";

    /**
     * 1. 压缩单个文件或文件夹
     *
     * @param resFiles 源文件或目录
     * @param zipFile  生成的压缩包文件
     */
    public static boolean zip(File resFiles, File zipFile) {
        if (resFiles == null || !resFiles.exists()) return false;
        List<File> fileList = new ArrayList<>();
        fileList.add(resFiles);
        return zipFiles(fileList, zipFile);
    }

    /**
     * 2. 压缩文件集合（统一核心入口）
     *
     * @param resFiles 待压缩的文件列表
     * @param zipFile  生成的压缩包文件
     */
    @SuppressWarnings("IOStreamConstructor")
    public static boolean zipFiles(List<File> resFiles, File zipFile) {
        if (resFiles == null || resFiles.isEmpty() || zipFile == null) return false;
        L.i(TAG, "开始压缩至: " + zipFile.getAbsolutePath());
        // 确保父目录存在
        File parent = zipFile.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            for (File file : resFiles) {
                if (file == null || !file.exists()) continue;
                // 递归压缩逻辑
                compress(file, zos, file.getName());
            }
            zos.finish();
            return true;
        } catch (Exception e) {
            L.e(TAG, "压缩失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 3. 递归压缩核心逻辑
     */
    @SuppressWarnings("IOStreamConstructor")
    private static void compress(File file, ZipOutputStream zos, String name) throws Exception {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null || files.length == 0) {
                zos.putNextEntry(new ZipEntry(name + "/"));
                zos.closeEntry();
            } else {
                for (File f : files) {
                    compress(f, zos, name + "/" + f.getName());
                }
            }
        } else {
            zos.putNextEntry(new ZipEntry(name));
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = bis.read(buffer)) != -1) {
                    zos.write(buffer, 0, len);
                }
            }
            zos.closeEntry();
        }
    }
}
