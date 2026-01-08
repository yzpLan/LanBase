package com.yzplan.lanbase.http.interceptor;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.yzplan.lanbase.utils.log.L;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

/**
 * 流式错误码拦截器（最终版）
 * 核心职责：
 * 1. 流式解析响应 JSON，快速扫描指定字段（通常是 code）
 * - 遇到目标字段立即返回，剩余 JSON 不解析
 * - 避免创建大字符串，减少内存消耗
 * 2. 安全防护：
 * - 最大解析长度 1MB，防止 Chunked / 大文件导致 OOM
 * - 克隆 Buffer 读取，不消费原 Response 流，Retrofit 正常工作
 * 3. 兼容性：
 * - lenient 模式容忍非标准 JSON
 * - 支持 code 字段为字符串或数字
 * 使用场景：
 * - 用于统一拦截后端返回码，处理 token 失效、权限异常等
 */
public abstract class BaseResponseCodeInterceptor implements Interceptor {
    private static final String TAG = "ResponseCodeInterceptor";
    // 最大解析长度 (1MB)。如果 JSON 超过 1MB，我们认为这可能不是一个常规的 API 响应，或者是大列表数据，直接跳过检查。
    private static final long MAX_CONTENT_LENGTH = 1024 * 1024;

    @NonNull
    @Override
    public Response intercept(Chain chain) throws java.io.IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        ResponseBody body = response.body();
        if (body == null) return response;
        MediaType mediaType = body.contentType();
        if (mediaType == null || !mediaType.subtype().contains("json")) {
            return response;
        }
        long contentLength = body.contentLength();
        if (contentLength > MAX_CONTENT_LENGTH) {
            return response;
        }
        try {
            BufferedSource source = body.source();
            source.request(MAX_CONTENT_LENGTH);
            Buffer buffer = source.buffer();
            if (buffer.size() > MAX_CONTENT_LENGTH) {
                return response;
            }
            Charset charset = StandardCharsets.UTF_8;
            Charset temp = mediaType.charset(StandardCharsets.UTF_8);
            if (temp != null) {
                charset = temp;
            }
            // 克隆 buffer 进行流式解析
            InputStream inputStream = buffer.clone().inputStream();
            try (JsonReader reader = new JsonReader(new InputStreamReader(inputStream, charset))) {
                reader.setLenient(true);
                String code = scanForCode(reader, getCodeFieldName());
                if (!TextUtils.isEmpty(code)) {
                    handleResponseCode(code);
                }
            } catch (Exception ignored) {
                // 忽略异常
            }
        } catch (Exception e) {
            L.e(TAG, "parse error: " + e.getMessage());
        }
        return response;
    }

    /**
     * 流式扫描 JSON，遇到 code 立即返回
     */
    private String scanForCode(JsonReader reader, String targetKey) throws Exception {
        if (reader.peek() != JsonToken.BEGIN_OBJECT) {
            return null;
        }
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (TextUtils.equals(name, targetKey)) {
                JsonToken token = reader.peek();
                if (token == JsonToken.STRING) {
                    return reader.nextString();
                } else if (token == JsonToken.NUMBER) {
                    return String.valueOf(reader.nextLong());
                } else {
                    reader.skipValue();
                    return null;
                }
            } else {
                reader.skipValue(); // 极速跳过
            }
        }
        return null;
    }

    protected abstract String getCodeFieldName();

    protected abstract void handleResponseCode(String code);
}