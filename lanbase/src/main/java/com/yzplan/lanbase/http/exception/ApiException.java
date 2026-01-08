package com.yzplan.lanbase.http.exception;

public class ApiException extends Exception {
    private String code;

    public ApiException(String message, String code) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
