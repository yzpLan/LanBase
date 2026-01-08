package com.yzplan.lanbase.base;

/**
 * 所有 View 接口的顶层父接口
 */
public interface IBaseView {
    void showLoading(String message);

    void showLoading(String message, Runnable onDismiss);

    void hideLoading();

    void showError(String errorMsg);

    void showError(String errorMsg, Runnable onDismiss);

    void showSuccess(String message);

    void showSuccess(String message, Runnable onDismiss);
}