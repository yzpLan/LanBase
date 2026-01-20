package com.yzplan.lanbase.utils.log;


import com.yzplan.lanbase.manager.CrashHandler;
import com.yzplan.lanbase.utils.data.FileUtils;
import com.yzplan.lanbase.utils.data.ZipUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 日志文件管理工具类
 * 功能：异步/同步写入、按日期检索、多文件合并、自动清理
 */
public class LogFileUtils {
    private static final String TAG = "LogFileUtils";
    private static String sLogDirPath; // 日志存储根目录
    private static final ExecutorService logExecutor = Executors.newSingleThreadExecutor(); // 单线程池确保写入顺序

    private static final SimpleDateFormat logTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA);
    private static final SimpleDateFormat fileNameFormat = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);

    /**
     * 初始化日志配置
     *
     * @param dirPath 存储路径（建议使用 getExternalFilesDir("log")）
     */
    public static void init(String dirPath) {
        if (dirPath == null) return;
        // 统一路径分隔符
        sLogDirPath = dirPath.endsWith(File.separator) ? dirPath : dirPath + File.separator;
        File dir = new File(sLogDirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // 初始化时清理一次临时合并文件夹
        clearTempFiles();
        CrashHandler.getInstance().init();
    }

    /**
     * 异步写入日志（常规使用）
     */
    public static void writeLogAsync(String tag, String msg) {
        if (sLogDirPath == null) return;
        logExecutor.execute(() -> performWrite(tag, msg));
    }

    /**
     * 同步写入日志（仅用于 Crash 捕捉等紧急场景，确保数据不丢失）
     */
    public static void writeLogSync(String tag, String msg) {
        if (sLogDirPath == null) return;
        performWrite(tag, msg);
    }

    /**
     * 核心写入逻辑
     */
    private static void performWrite(String tag, String msg) {
        if (sLogDirPath == null) return;
        // --- 增加自动修复逻辑 ---
        File dir = new File(sLogDirPath);
        if (!dir.exists()) {
            boolean success = dir.mkdirs();
            if (!success) {
                // 如果创建失败（比如权限依然没拿到），直接返回，避免触发异常
                return;
            }
        }
        // -----------------------
        String fileName = fileNameFormat.format(new Date()) + ".log";
        File logFile = new File(sLogDirPath, fileName);

        // 使用 true 参数代表追加写入
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            String content = String.format("%s [%s]: %s\n", logTimeFormat.format(new Date()), tag, msg);
            writer.write(content);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据日期范围获取日志文件集合
     *
     * @param startDateStr 开始日期 yyyyMMdd
     * @param endDateStr   结束日期 yyyyMMdd
     */
    public static List<File> getLogFilesByRange(String startDateStr, String endDateStr) {
        List<File> resultList = new ArrayList<>();
        if (sLogDirPath == null) return resultList;
        File logDir = new File(sLogDirPath);
        File[] files = logDir.listFiles();
        if (files == null) return resultList;
        try {
            Date startDate = fileNameFormat.parse(startDateStr);
            Date endDate = fileNameFormat.parse(endDateStr);
            if (startDate == null || endDate == null) return resultList;
            long start = startDate.getTime();
            long end = endDate.getTime();
            for (File file : files) {
                if (file.isFile() && file.getName().matches("\\d{8}\\.log")) {
                    String dateStr = file.getName().substring(0, 8);
                    Date fileDate = fileNameFormat.parse(dateStr);
                    if (fileDate != null) {
                        long fileTime = fileDate.getTime();
                        if (fileTime >= start && fileTime <= end) {
                            resultList.add(file);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Collections.sort(resultList, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return f1.getName().compareTo(f2.getName());
            }
        });
        return resultList;
    }

    /**
     * 合并指定范围的日志为一个临时文件（用于分享/查看）
     *
     * @param startDateStr 开始日期 yyyyMMdd
     * @param endDateStr   结束日期 yyyyMMdd
     */
    public static File getMergedLogFile(String startDateStr, String endDateStr) {
        List<File> files = getLogFilesByRange(startDateStr, endDateStr);
        if (files.isEmpty()) return null;
        File cacheDir = new File(sLogDirPath, "temp");
        if (!cacheDir.exists()) cacheDir.mkdirs();
        File mergedFile = new File(cacheDir, "merged_logs_" + startDateStr + "_" + endDateStr + ".txt");
        if (mergedFile.exists()) mergedFile.delete();
        L.i(TAG, "开始合并日志至: " + mergedFile.getName());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(mergedFile, true))) {
            for (File file : files) {
                writer.write("==================== File: " + file.getName() + " ====================\n");
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        writer.write(line);
                        writer.newLine();
                    }
                }
                writer.write("\n\n");
            }
            return mergedFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取合并并压缩后的日志包
     * 流程：筛选文件 -> 合并到 temp.txt -> 压缩为 .zip -> 返回 zip 文件
     *
     * @return 最终的 zip 文件，失败返回 null
     */
    public static File getMergedZipFile(String startDateStr, String endDateStr) {
        // 1. 获取合并后的临时 .txt 文件
        File mergedFile = getMergedLogFile(startDateStr, endDateStr);
        if (mergedFile == null || !mergedFile.exists()) return null;
        try {
            // 2. 创建压缩包 File 对象，统一传参类型
            String zipPath = mergedFile.getAbsolutePath().replace(".txt", ".zip");
            File zipFile = new File(zipPath);
            // 3. 调用重构后的 ZipUtils，直接传入 File 对象
            boolean success = ZipUtils.zip(mergedFile, zipFile);
            if (success) {
                L.i(TAG, "日志合并压缩成功: " + FileUtils.formatSize(zipFile.length()));
                // 4. 压缩成功后清理临时 txt 文件
                mergedFile.delete();
                return zipFile;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取原始日志文件集合的压缩包（不合并，直接打包多天文件）
     *
     * @return 压缩后的 zip 文件
     */
    public static File getLogsZipFile(String startDateStr, String endDateStr) {
        // 1. 获取文件列表
        List<File> files = getLogFilesByRange(startDateStr, endDateStr);
        if (files.isEmpty()) return null;
        // 2. 准备压缩包路径
        File cacheDir = new File(sLogDirPath, "temp");
        if (!cacheDir.exists()) cacheDir.mkdirs();
        File zipFile = new File(cacheDir, "logs_batch_" + startDateStr + "_" + endDateStr + ".zip");
        // 3. 执行批量压缩
        if (ZipUtils.zipFiles(files, zipFile)) {
            return zipFile;
        }
        return null;
    }

    /**
     * 自动清理过期日志
     *
     * @param keepDays keepDays 保留天数，建议 7 天
     */
    public static void cleanExpiredLogs(int keepDays) {
        if (sLogDirPath == null) return;
        File logDir = new File(sLogDirPath);
        File[] files = logDir.listFiles();
        if (files == null) return;
        long cutoffTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(keepDays);
        for (File file : files) {
            if (file.isFile() && file.getName().matches("\\d{8}\\.log")) {
                try {
                    String dateStr = file.getName().substring(0, 8);
                    Date fileDate = fileNameFormat.parse(dateStr);
                    if (fileDate != null && fileDate.getTime() < cutoffTime) {
                        file.delete();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 清理临时合并文件，做完操作记得调用此方法
     */
    public static void clearTempFiles() {
        if (sLogDirPath == null) return;
        File cacheDir = new File(sLogDirPath, "temp");
        if (cacheDir.exists()) {
            L.i(TAG, "清除缓存文件/文件夹:" + cacheDir.getAbsolutePath());
            deleteRecursive(cacheDir);
        }
    }

    /**
     * 递归删除
     */
    private static void deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) deleteRecursive(child);
            }
        }
        file.delete();
    }

    /**
     * 获取日志根目录
     */
    public static String getLogDirPath() {
        return sLogDirPath;
    }
}