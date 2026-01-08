package com.yzplan.lanbase.utils.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.ToNumberPolicy;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * JSON 工具类 (基于 Gson)
 */
public class JsonUtil {

    private static final Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
            .create();

    private JsonUtil() {
    }

    /**
     * 供 Retrofit 等框架使用
     */
    public static Gson getGson() {
        return gson;
    }

    /**
     * 对象转 JSON 字符串
     */
    public static String toJson(Object object) {
        if (object == null) return "";
        return gson.toJson(object);
    }

    /**
     * 格式化 JSON 字符串 (用于日志打印，带缩进)
     *
     * @param jsonStr 原始 JSON 字符串
     * @return 格式化后的 JSON，如果解析失败则返回原字符串
     */
    public static String formatJson(String jsonStr) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            return "";
        }
        try {
            JsonElement jsonElement = new JsonParser().parse(jsonStr);
            return new GsonBuilder().setPrettyPrinting().create().toJson(jsonElement);
        } catch (Exception e) {
            return jsonStr;
        }
    }

    /**
     * JSON 字符串转 对象
     */
    public static <T> T fromJson(String json, Class<T> classOfT) {
        if (json == null) return null;
        try {
            return gson.fromJson(json, classOfT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * JSON 字符串转 List 集合
     * 示例: List<User> list = JsonUtil.fromJsonToList(json, User.class);
     */
    public static <T> List<T> fromJsonToList(String json, Class<T> clazz) {
        if (json == null) return null;
        Type type = TypeToken.getParameterized(List.class, clazz).getType();
        return gson.fromJson(json, type);
    }

    /**
     * JSON 字符串转 Map
     */
    public static Map<String, Object> fromJsonToMap(String json) {
        if (json == null) return null;
        Type type = new TypeToken<Map<String, Object>>() {
        }.getType();
        return gson.fromJson(json, type);
    }
}