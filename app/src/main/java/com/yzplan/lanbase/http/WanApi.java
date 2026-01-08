package com.yzplan.lanbase.http;

import com.google.gson.JsonObject;
import com.yzplan.lanbase.http.api.BaseApi;

import java.util.List;
import java.util.Map;

import io.reactivex.Single;
import okhttp3.Interceptor;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * WanApi 系统接口管理
 */
public class WanApi extends BaseApi<WanApi.Service> {
    private static volatile WanApi instance;

    public static WanApi getInstance() {
        if (instance == null) {
            synchronized (WanApi.class) {
                if (instance == null) instance = new WanApi();
            }
        }
        return instance;
    }

    // 1. 配置域名
    @Override
    protected String getBaseUrl() {
        return "https://www.wanandroid.com/";
    }

    // 2. 绑定 Retrofit 接口
    @Override
    protected Class<Service> getServiceClass() {
        return Service.class;
    }

    // 3. 配置拦截器
    @Override
    protected void registerInterceptors(List<Interceptor> interceptors) {
    }

    //  接口定义区
    public interface Service {
        /**
         * 登录接口
         */
        @FormUrlEncoded
        @POST("user/login")
        Single<WanBaseResponse<JsonObject>> login(@FieldMap Map<String, Object> request);
    }
}