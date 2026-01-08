package com.yzplan.lanbase.utils.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.TextUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.HashMap;
import java.util.Map;

/**
 * 二维码生成工具类
 * 依赖: com.google.zxing:core:3.x.x
 */
public class QRCodeUtils {

    /**
     * 生成简单的二维码
     *
     * @param content 字符串内容
     * @param size    图片宽高 (px)，例如 400
     * @return Bitmap 对象
     */
    public static Bitmap createQRCode(String content, int size) {
        return createQRCode(content, size, Color.BLACK, Color.WHITE);
    }

    /**
     * 生成自定义颜色的二维码
     *
     * @param content   字符串内容
     * @param size      图片宽高 (px)
     * @param foreColor 前景色 (二维码线条颜色)
     * @param backColor 背景色
     */
    public static Bitmap createQRCode(String content, int size, int foreColor, int backColor) {
        if (TextUtils.isEmpty(content)) {
            return null;
        }

        try {
            // 1. 配置参数
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8"); // 字符集
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // 容错级别 H (最高)，为了防遮挡
            hints.put(EncodeHintType.MARGIN, 1); // 白边大小，0-4，数值越小周边空白越少

            // 2. 生成矩阵
            BitMatrix bitMatrix = new QRCodeWriter().encode(
                    content,
                    BarcodeFormat.QR_CODE,
                    size,
                    size,
                    hints
            );

            // 3. 将矩阵转换为像素数组
            int[] pixels = new int[size * size];
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * size + x] = foreColor; // 黑色点
                    } else {
                        pixels[y * size + x] = backColor; // 白色背景
                    }
                }
            }

            // 4. 生成 Bitmap
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, size, 0, 0, size, size);
            return bitmap;

        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 生成带 Logo 的二维码
     *
     * @param content 内容
     * @param size    二维码大小
     * @param logo    Logo 图片
     * @return 合成后的 Bitmap
     */
    public static Bitmap createQRCodeWithLogo(String content, int size, Bitmap logo) {
        // 1. 先生成原版二维码
        Bitmap qrCode = createQRCode(content, size);
        if (qrCode == null) return null;
        if (logo == null) return qrCode;

        // 2. 获取 Logo 的宽高
        int logoWidth = logo.getWidth();
        int logoHeight = logo.getHeight();

        // 3. 计算 Logo 在二维码中的缩放比例 (Logo 一般占二维码的 1/5 到 1/4)
        float scaleFactor = size * 1.0f / 5 / logoWidth;

        // 4. 创建画布
        Bitmap combinedBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(combinedBitmap);

        // 5. 绘制二维码
        canvas.drawBitmap(qrCode, 0, 0, null);

        // 6. 绘制缩放后的 Logo
        canvas.scale(scaleFactor, scaleFactor, size / 2f, size / 2f);
        canvas.drawBitmap(logo, (size - logoWidth) / 2f, (size - logoHeight) / 2f, null);

        // 7. (可选) 给 Logo 绘制一个白边外框，看起来更清晰
        // drawLogoStroke(canvas, size, logoWidth, logoHeight);

        return combinedBitmap;
    }
}