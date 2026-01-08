package com.yzplan.lanbase.utils.view;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.yzplan.lanbase.R;

/**
 * Glide 图片加载工具类
 */
public class GlideUtils {

    // 默认占位图和错误图
    private static final int PLACEHOLDER = R.drawable.lib_glide_placeholder;
    private static final int ERROR = R.drawable.lib_glide_error;

    /**
     * 基础加载
     */
    public static void load(Context context, String url, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .placeholder(PLACEHOLDER)
                .error(ERROR)
                .into(imageView);
    }

    /**
     * 基础加载 (自定义占位图)
     */
    public static void load(Context context, String url, ImageView imageView, int placeholder, int error) {
        Glide.with(context)
                .load(url)
                .placeholder(placeholder)
                .error(error)
                .into(imageView);
    }

    /**
     * 加载圆形图片
     */
    public static void loadCircle(Context context, String url, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .placeholder(PLACEHOLDER)
                .error(ERROR)
                .transform(new CircleCrop())
                .into(imageView);
    }

    /**
     * 加载圆形图片 (自定义占位图)
     */
    public static void loadCircle(Context context, String url, ImageView imageView, int placeholder, int error) {
        Glide.with(context)
                .load(url)
                .placeholder(placeholder)
                .error(error)
                .transform(new CircleCrop())
                .into(imageView);
    }

    /**
     * 加载圆角图片
     *
     * @param radiusDp 圆角弧度 (单位dp)
     */
    public static void loadRounded(Context context, String url, ImageView imageView, int radiusDp) {
        int radiusPx = (int) (radiusDp * context.getResources().getDisplayMetrics().density);
        Glide.with(context)
                .load(url)
                .placeholder(PLACEHOLDER)
                .error(ERROR)
                .transform(new CenterCrop(), new RoundedCorners(radiusPx))
                .into(imageView);
    }

    /**
     * 加载圆角图片 (自定义占位图)
     */
    public static void loadRounded(Context context, String url, ImageView imageView, int radiusDp, int placeholder, int error) {
        int radiusPx = (int) (radiusDp * context.getResources().getDisplayMetrics().density);
        Glide.with(context)
                .load(url)
                .placeholder(placeholder)
                .error(error)
                .transform(new CenterCrop(), new RoundedCorners(radiusPx))
                .into(imageView);
    }

    /**
     * 清理缓存
     */
    public static void clearCache(Context context) {
        // 清理内存 (必须在主线程调用)
        Glide.get(context).clearMemory();
        // 清理磁盘 (必须在子线程调用)
        new Thread(() -> Glide.get(context).clearDiskCache()).start();
    }
}
