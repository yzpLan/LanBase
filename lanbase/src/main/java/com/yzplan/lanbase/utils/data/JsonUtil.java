package com.yzplan.lanbase.utils.data;

import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.ToNumberPolicy;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON 工具类 (基于 Gson 优化版)
 */
public class JsonUtil {

    private static final Gson gson = new GsonBuilder()
            .disableHtmlEscaping() // 防止 HTML 字符被转义
            .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE) // 解决 ID 变成科学计数法的问题
            .create();

    private static final Gson prettyGson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private JsonUtil() {}

    public static Gson getGson() {
        return gson;
    }

    /**
     * 对象转 JSON
     */
    public static String toJson(Object object) {
        if (object == null) return "";
        try {
            return gson.toJson(object);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 格式化 JSON (用于日志展示)
     */
    public static String formatJson(String jsonStr) {
        if (TextUtils.isEmpty(jsonStr)) return "";
        try {
            // 使用静态方法代替已过时的 JsonParser 构造函数
            JsonElement jsonElement = JsonParser.parseString(jsonStr);
            return prettyGson.toJson(jsonElement);
        } catch (Exception e) {
            return jsonStr;
        }
    }

    /**
     * JSON 转对象
     */
    public static <T> T fromJson(String json, Class<T> classOfT) {
        if (TextUtils.isEmpty(json)) return null;
        try {
            return gson.fromJson(json, classOfT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 支持通过 Type 解析 (处理 List<Map> 等复杂结构)
     */
    public static <T> T fromJson(String json, Type typeOfT) {
        if (TextUtils.isEmpty(json)) return null;
        try {
            return gson.fromJson(json, typeOfT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 解析 String 列表
     * 示例：List<String> list = JsonUtil.fromJsonToList(json, String.class);
     */
    public static <T> List<T> fromJsonToList(String json, Class<T> clazz) {
        if (TextUtils.isEmpty(json)) return new ArrayList<>(); // 返回空集合比返回 null 更安全
        try {
            Type type = TypeToken.getParameterized(List.class, clazz).getType();
            return gson.fromJson(json, type);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * JSON 转 Map
     */
    public static Map<String, Object> fromJsonToMap(String json) {
        if (TextUtils.isEmpty(json)) return new HashMap<>();
        try {
            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            return gson.fromJson(json, type);
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }
}