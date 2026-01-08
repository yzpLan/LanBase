package com.yzplan.lanbase.utils.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.yzplan.lanbase.BaseApp;


/**
 * SharedPreferences 工具类
 */
public class SpUtils {
    private static final String SP_FILE_NAME = "config_sp";
    private static volatile SpUtils instance;
    private static SharedPreferences sp;
    private static SharedPreferences.Editor editor;

    private SpUtils() {
    }

    /**
     * 获取单例实例
     *
     * @return SPUtils 实例
     */
    public static SpUtils getInstance() {
        if (instance == null) {
            synchronized (SpUtils.class) {
                if (instance == null) {
                    Context context = BaseApp.getContext();
                    if (context == null) {
                        throw new NullPointerException("BaseApp 未初始化");
                    }
                    sp = BaseApp.getContext().getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);
                    editor = sp.edit();
                    instance = new SpUtils();
                }
            }
        }
        return instance;
    }

    public void putString(String key, String value) {
        editor.putString(key, value).apply();
    }

    public void putInt(String key, int value) {
        editor.putInt(key, value).apply();
    }

    public void putBoolean(String key, boolean value) {
        editor.putBoolean(key, value).apply();
    }

    public String getString(String key, String defValue) {
        return sp.getString(key, defValue);
    }

    public int getInt(String key, int defValue) {
        return sp.getInt(key, defValue);
    }

    public boolean getBoolean(String key, boolean defValue) {
        return sp.getBoolean(key, defValue);
    }

    /**
     * 移除指定的 Key
     */
    public void remove(String key) {
        editor.remove(key).apply();
    }

    /**
     * 清空所有数据
     */
    public void clear() {
        editor.clear().apply();
    }
}