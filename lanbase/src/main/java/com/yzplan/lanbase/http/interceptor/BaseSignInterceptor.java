package com.yzplan.lanbase.http.interceptor;

import androidx.annotation.NonNull;

import com.yzplan.lanbase.utils.data.JsonUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

/**
 * 通用签名拦截器基类（最终版）
 * 支持 GET / Form / JSON / Multipart
 * 核心职责：
 * 1. 解析请求参数
 * 2. 调用 processParams 添加/修改签名等公共参数
 * 3. 重建请求
 */
public abstract class BaseSignInterceptor implements Interceptor {

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String method = request.method();
        if ("GET".equalsIgnoreCase(method)) {
            request = handleGetRequest(request);
        } else if ("POST".equalsIgnoreCase(method)) {
            RequestBody body = request.body();
            if (body != null) {
                if (body instanceof FormBody) {
                    request = handleFormRequest(request, (FormBody) body);
                } else if (isJsonRequest(body)) {
                    request = handleJsonRequest(request, body);
                } else if (body instanceof MultipartBody) {
                    request = handleMultipartRequest(request, (MultipartBody) body);
                }
            }
        }
        return chain.proceed(request);
    }

    /**
     * 子类必须实现：处理签名逻辑
     * 可以增删改公共参数
     */
    protected abstract void prepareSignedParams(Map<String, Object> params);

    // ===========================================
    //  GET / Form / JSON / Multipart 处理逻辑
    // ===========================================

    private Request handleGetRequest(Request request) {
        Map<String, Object> params = new TreeMap<>();
        HttpUrl originalUrl = request.url();
        for (String name : originalUrl.queryParameterNames()) {
            params.put(name, originalUrl.queryParameter(name));
        }
        prepareSignedParams(params);
        // 重新拼接 URL
        HttpUrl.Builder urlBuilder = originalUrl.newBuilder().query(null);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() != null) {
                urlBuilder.addQueryParameter(entry.getKey(), entry.getValue().toString());
            }
        }
        return request.newBuilder().url(urlBuilder.build()).build();
    }

    private Request handleFormRequest(Request request, FormBody body) {
        Map<String, Object> params = new TreeMap<>();
        for (int i = 0; i < body.size(); i++) {
            params.put(body.name(i), body.value(i));
        }
        prepareSignedParams(params);
        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() != null) {
                builder.add(entry.getKey(), entry.getValue().toString());
            }
        }
        return request.newBuilder().post(builder.build()).build();
    }

    private Request handleJsonRequest(Request request, RequestBody body) throws IOException {
        String jsonString = bodyToString(body);
        Map<String, Object> params = JsonUtil.fromJsonToMap(jsonString);
        if (params == null) params = new TreeMap<>();
        prepareSignedParams(params);
        String newJson = JsonUtil.toJson(params);
        RequestBody newBody = RequestBody.create(body.contentType(), newJson);
        return request.newBuilder().post(newBody).build();
    }

    private Request handleMultipartRequest(Request request, MultipartBody body) {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(body.type());
        Map<String, Object> textParams = new TreeMap<>();
        List<MultipartBody.Part> fileParts = new ArrayList<>();
        // 分离文本参数和文件
        for (MultipartBody.Part part : body.parts()) {
            if (isFilePart(part)) {
                fileParts.add(part);
            } else {
                String name = getPartName(part);
                String value = readPartAsString(part.body());
                textParams.put(name, value);
            }
        }
        // 处理签名
        prepareSignedParams(textParams);
        // 合并文本参数
        for (Map.Entry<String, Object> entry : textParams.entrySet()) {
            if (entry.getValue() != null) {
                builder.addFormDataPart(entry.getKey(), entry.getValue().toString());
            }
        }
        // 添加文件
        for (MultipartBody.Part filePart : fileParts) {
            builder.addPart(filePart);
        }
        return request.newBuilder().post(builder.build()).build();
    }

    // ===========================================
    //  辅助方法
    // ===========================================

    private boolean isJsonRequest(RequestBody body) {
        MediaType type = body.contentType();
        return type != null && type.subtype().contains("json");
    }

    private boolean isFilePart(MultipartBody.Part part) {
        Headers headers = part.headers();
        if (headers != null) {
            String disposition = headers.get("Content-Disposition");
            if (disposition != null && disposition.contains("filename=")) return true;
        }
        RequestBody body = part.body();
        MediaType contentType = body.contentType();
        if (contentType != null) {
            String type = contentType.type();
            return !"text".equals(type);
        }
        return false;
    }

    private String getPartName(MultipartBody.Part part) {
        Headers headers = part.headers();
        if (headers != null) {
            String disposition = headers.get("Content-Disposition");
            if (disposition != null) {
                int start = disposition.indexOf("name=\"");
                if (start != -1) {
                    int end = disposition.indexOf("\"", start + 6);
                    if (end != -1) return disposition.substring(start + 6, end);
                }
            }
        }
        return "unknown";
    }

    private String readPartAsString(RequestBody body) {
        if (body == null) return "";
        try {
            Buffer buffer = new Buffer();
            body.writeTo(buffer);
            if (buffer.size() > 10 * 1024) return "";
            return buffer.readUtf8();
        } catch (IOException e) {
            return "";
        }
    }

    private String bodyToString(RequestBody body) throws IOException {
        if (body == null) return "{}";
        Buffer buffer = new Buffer();
        body.writeTo(buffer);
        return buffer.readUtf8();
    }

    /**
     * 供子类使用：在处理参数时，如果 key 不存在则添加
     */
    protected void putIfMissing(Map<String, Object> params, String key, Object value) {
        if (!params.containsKey(key)) {
            params.put(key, value);
        }
    }
}
