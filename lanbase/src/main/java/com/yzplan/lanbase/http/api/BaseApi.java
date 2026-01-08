package com.yzplan.lanbase.http.api;


import com.yzplan.lanbase.http.RetrofitClient;
import com.yzplan.lanbase.http.interceptor.LogInterceptor;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Interceptor;

/**
 * 所有 API 的基类
 */
public abstract class BaseApi<T> {

    private volatile T mService;

    protected abstract String getBaseUrl();

    // 建议：默认返回 0，明确代表“使用全局默认值15S”
    protected int getTimeoutSeconds() {
        return 0;
    }

    // 默认提供内置日志拦截器，子类可通过重写关闭或替换
    protected Interceptor getLogInterceptor() {
        return new LogInterceptor();
    }

    protected abstract Class<T> getServiceClass();

    /**
     * 子类按需重写 添加请求需要的业务拦截器
     */
    protected abstract void registerInterceptors(List<Interceptor> interceptors);

    public T get() {
        if (mService == null) {
            synchronized (this) {
                if (mService == null) {
                    List<Interceptor> list = new ArrayList<>();
                    // 添加自定义拦截器
                    registerInterceptors(list);
                    // 添加日志拦截器
                    Interceptor log = getLogInterceptor();
                    if (log != null) {
                        list.add(log);
                    }
                    mService = RetrofitClient.getInstance().create(
                            getServiceClass(),
                            getBaseUrl(),
                            getTimeoutSeconds(),
                            list.toArray(new Interceptor[0])
                    );
                }
            }
        }
        return mService;
    }
}