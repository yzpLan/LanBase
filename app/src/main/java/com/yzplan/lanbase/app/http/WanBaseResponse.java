package com.yzplan.lanbase.app.http;


import com.yzplan.lanbase.base.BaseResponse;

import java.io.Serializable;


public class WanBaseResponse<T> implements BaseResponse<T>, Serializable {
    private int errorCode;
    private String errorMsg;
    private T data;

    @Override
    public boolean isSuccess() {
        return errorCode == 0;
    }

    @Override
    public T getData() {
        return data;
    }

    @Override
    public String getMessage() {
        return errorMsg;
    }

    @Override
    public String getCode() {
        return String.valueOf(errorCode);
    }
}
