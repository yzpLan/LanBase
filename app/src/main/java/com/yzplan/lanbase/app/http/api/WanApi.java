package com.yzplan.lanbase.app.http.api;

import com.yzplan.lanbase.app.bean.base.WanBaseResponse;
import com.yzplan.lanbase.app.bean.response.ArticleListResponse;
import com.yzplan.lanbase.app.bean.response.BannerBean;
import com.yzplan.lanbase.app.bean.response.LoginResponse;
import com.yzplan.lanbase.http.api.BaseApi;

import java.util.List;
import java.util.Map;

import io.reactivex.Single;
import okhttp3.Interceptor;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

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
        Single<WanBaseResponse<LoginResponse>> login(@FieldMap Map<String, Object> request);

        /**
         * 首页文章列表
         */
        @GET("article/list/{page}/json")
        Single<WanBaseResponse<ArticleListResponse>> getArticleList(@Path("page") int page, @Query("page_size") int pageSize);

        /**
         * 首页banner
         */
        @GET("banner/json")
        Single<WanBaseResponse<List<BannerBean>>> getBanner();
    }
}