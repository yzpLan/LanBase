package com.yzplan.lanbase.http.interceptor;

import androidx.annotation.NonNull;

import com.yzplan.lanbase.utils.data.JsonUtil;
import com.yzplan.lanbase.utils.log.L;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

/**
 * 日志拦截器（最终版）
 * 核心职责：
 * 1. 打印 HTTP 请求和响应日志
 * - 请求日志包含 URL、Method、参数
 * - 响应日志包含状态码、耗时、文本类型 Body
 * 2. 支持安全打印：
 * - 大文件、二进制流、图片/视频上传不会消耗内存或打印内容
 * - 对 JSON/文本类型响应体使用 clone 技术读取，避免 Retrofit 消耗原流
 * 3. 支持 Multipart 上传：
 * - 文件部分只打印文件名、类型、大小
 * - 文本部分读取并打印，但超过限制长度会忽略
 */
public class LogInterceptor implements Interceptor {
    private static final String TAG_REQUEST = "OkHttp-Request";
    private static final String TAG_RESPONSE = "OkHttp-Response";

    /**
     * 请求 ID 生成器，保证并发下日志可追踪
     */
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(1000);

    /**
     * 默认字符集
     */
    private static final Charset UTF8 = StandardCharsets.UTF_8;

    /**
     * 最大可打印响应体大小（1MB）
     */
    private static final long MAX_LOG_BODY_SIZE = 1024 * 1024;

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String id = "[" + ID_GENERATOR.getAndIncrement() + "]";
        // 请求日志
        printRequestLog(request, id);
        long startTime = System.nanoTime();
        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            // 网络异常日志
            L.e(TAG_RESPONSE, id + " 请求失败: " + e.getMessage());
            throw e;
        }
        // 响应日志
        printResponseLog(response, id, startTime);
        return response;
    }

    /**
     * 打印请求日志
     */
    private void printRequestLog(Request request, String id) {
        StringBuilder sb = new StringBuilder();
        sb.append("⬇️⬇️ ============ Request ").append(id).append(" ============ ⬇️⬇️\n");
        sb.append("URL    : ").append(request.url()).append("\n");
        sb.append("Method : ").append(request.method()).append("\n");
        RequestBody body = request.body();
        if (body != null) {
            try {
                // multipart/form-data（文件上传）
                if (body instanceof MultipartBody) {
                    printMultipartBody((MultipartBody) body, sb);
                } else {
                    long length = body.contentLength();
                    // 只有「明确知道长度且很小」的请求体才打印
                    // -1 代表未知长度（流式 body），一律跳过
                    if (!isFileUpload(body.contentType()) && length != -1 && length < 2048) {
                        Buffer buffer = new Buffer();
                        body.writeTo(buffer);
                        Charset charset = getCharset(body.contentType());
                        sb.append("Body   : \n").append(JsonUtil.formatJson(buffer.readString(charset))).append("\n");
                    } else {
                        sb.append("Body   : (Binary / Stream / LargeBody - Ignored)\n");
                    }
                }
            } catch (Exception e) {
                sb.append("Body   : (Log Error) ").append(e.getMessage()).append("\n");
            }
        }
        sb.append("⬆️⬆️ ============================================ ⬆️⬆️");
        L.i(TAG_REQUEST, sb.toString());
    }

    /**
     * 打印响应日志
     */
    private void printResponseLog(Response response, String id, long startTime) {
        StringBuilder sb = new StringBuilder();
        sb.append("⬇️⬇️ ============ Response ").append(id).append(" ============ ⬇️⬇️\n");
        sb.append("Code   : ").append(response.code()).append("\n");
        sb.append("Time   : ").append(getCostTime(startTime)).append(" ms\n");
        ResponseBody body = response.body();
        if (body != null && isPlaintext(body.contentType())) {
            try {
                BufferedSource source = body.source();
                // 预读最多 1MB + 1 字节
                source.request(MAX_LOG_BODY_SIZE + 1);
                Buffer buffer = source.buffer();
                if (buffer.size() <= MAX_LOG_BODY_SIZE) {
                    Charset charset = getCharset(body.contentType());
                    String content = buffer.clone().readString(charset);
                    sb.append("Body   : \n").append(JsonUtil.formatJson(content)).append("\n");
                } else {
                    sb.append("Body   : (Payload > 1MB - Ignored)\n");
                }
            } catch (Exception e) {
                sb.append("Body   : (Read Error) ").append(e.getMessage()).append("\n");
            }
        } else {
            sb.append("Body   : (Binary / Stream - Ignored)\n");
        }
        sb.append("⬆️⬆️ ============================================ ⬆️⬆️");
        L.i(TAG_RESPONSE, sb.toString());
    }

    /**
     * 打印 multipart/form-data 内容
     * 文件只打印元信息，绝不读文件内容
     */
    private void printMultipartBody(MultipartBody body, StringBuilder sb) {
        sb.append("Body   : (Multipart/Form-Data)\n");
        List<MultipartBody.Part> parts = body.parts();
        for (MultipartBody.Part part : parts) {
            Headers headers = part.headers();
            String disposition = headers != null ? headers.get("Content-Disposition") : "";
            String name = getNameFromDisposition(disposition);
            String filename = getFileNameFromDisposition(disposition);
            sb.append("    ▪ Key: \"").append(name).append("\"");
            if (filename != null) {
                sb.append("  [File] Name: ").append(filename).append("\n");
            } else {
                try {
                    RequestBody pb = part.body();
                    if (pb.contentLength() < 1024) {
                        Buffer buffer = new Buffer();
                        pb.writeTo(buffer);
                        sb.append("  Value: \"").append(buffer.readUtf8()).append("\"\n");
                    } else {
                        sb.append("  Value: (Too long)\n");
                    }
                } catch (IOException e) {
                    sb.append("  Value: (Error)\n");
                }
            }
        }
    }

    /**
     * 获取 charset，默认 UTF-8
     */
    private Charset getCharset(MediaType type) {
        return type != null ? type.charset(UTF8) : UTF8;
    }

    /**
     * 判断是否可打印的文本类型
     */
    private boolean isPlaintext(MediaType mediaType) {
        if (mediaType == null) return false;
        String type = mediaType.type();
        String subtype = mediaType.subtype();
        return "text".equals(type)
                || subtype.contains("json")
                || subtype.contains("xml")
                || subtype.contains("html");
    }

    /**
     * 判断是否文件/二进制上传
     */
    private boolean isFileUpload(MediaType mediaType) {
        if (mediaType == null) return false;
        String subtype = mediaType.subtype();
        return subtype.contains("stream") || subtype.contains("image") || subtype.contains("video");
    }

    /**
     * 计算耗时（毫秒，保留两位小数）
     */
    private String getCostTime(long startTime) {
        return String.format(Locale.getDefault(), "%.2f", (System.nanoTime() - startTime) / 1e6d);
    }

    /**
     * 从 Content-Disposition 解析 name
     */
    private String getNameFromDisposition(String disposition) {
        if (disposition == null) return "unknown";
        int start = disposition.indexOf("name=\"");
        if (start != -1) {
            int end = disposition.indexOf("\"", start + 6);
            if (end != -1) {
                return disposition.substring(start + 6, end);
            }
        }
        return "unknown";
    }

    /**
     * 从 Content-Disposition 解析 filename
     */
    private String getFileNameFromDisposition(String disposition) {
        if (disposition == null) return null;
        int start = disposition.indexOf("filename=\"");
        if (start != -1) {
            int end = disposition.indexOf("\"", start + 10);
            if (end != -1) {
                return disposition.substring(start + 10, end);
            }
        }
        return null;
    }
}
