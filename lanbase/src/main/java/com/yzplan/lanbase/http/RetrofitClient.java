package com.yzplan.lanbase.http;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit 工厂类
 * 职责：
 * 1. 维护全局唯一的 OkHttpClient (复用连接池)
 * 2. 负责组装 Retrofit 并创建 Service 实例
 */
public class RetrofitClient {
    // 全局通用的 Client (包含基础配置)
    private final OkHttpClient mGlobalOkHttpClient;
    private static volatile RetrofitClient sInstance;

    private RetrofitClient() {
        // 1. 初始化全局基础配置
        mGlobalOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();
    }

    public static RetrofitClient getInstance() {
        if (sInstance == null) {
            synchronized (RetrofitClient.class) {
                if (sInstance == null) {
                    sInstance = new RetrofitClient();
                }
            }
        }
        return sInstance;
    }

    /**
     * 核心创建方法
     *
     * @param serviceClass   接口 Class
     * @param baseUrl        域名
     * @param timeoutSeconds 超时时间
     * @param interceptors   该接口专用的拦截器数组
     */
    public <T> T create(Class<T> serviceClass, String baseUrl, int timeoutSeconds, Interceptor... interceptors) {
        // 2. 准备 Client
        OkHttpClient.Builder builder = mGlobalOkHttpClient.newBuilder();
        // 3.如果传入了非 0 的超时时间，则覆盖默认配置
        if (timeoutSeconds > 0) {
            builder.connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
                    .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
                    .writeTimeout(timeoutSeconds, TimeUnit.SECONDS);
        }
        // 4.添加自定义拦截器
        for (Interceptor interceptor : interceptors) {
            builder.addInterceptor(interceptor);
        }
        OkHttpClient client = builder.build();
        // 5. 构建 Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        // 6. 生成接口实例
        return retrofit.create(serviceClass);
    }
}
