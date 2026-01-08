package com.yzplan.lanbase.base;

/**
 * 通用的 API 响应接口
 */
public interface BaseResponse<T> {
    /**
     * 请求是否成功
     * (由子类实现具体的判断逻辑，比如 code.equals("00") 或 status == 200)
     */
    boolean isSuccess();

    /**
     * 获取业务数据
     */
    T getData();

    /**
     * 获取错误信息
     */
    String getMessage();

    /**
     * 获取错误码 (可选，用于特定错误处理)
     */
    String getCode();
}
