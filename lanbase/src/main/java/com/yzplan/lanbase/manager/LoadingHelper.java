package com.yzplan.lanbase.manager;

import android.app.Activity;
import com.yzplan.lanbase.view.CommonLoadingDialog;

/**
 * Loading 弹窗管理助手
 * 负责 Loading 弹窗的显示、复用、状态更新和销毁
 */
public class LoadingHelper {

    private CommonLoadingDialog mLoadingDialog;
    private final Activity activity;

    public LoadingHelper(Activity activity) {
        this.activity = activity;
    }

    public void showLoading(String message, Runnable onDismiss) {
        showDialog(CommonLoadingDialog.Status.LOADING, message, onDismiss);
    }

    public void showSuccess(String message, Runnable onDismiss) {
        showDialog(CommonLoadingDialog.Status.SUCCESS, message, onDismiss);
    }

    public void showError(String message, Runnable onDismiss) {
        showDialog(CommonLoadingDialog.Status.ERROR, message, onDismiss);
    }

    public void dismiss() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
            mLoadingDialog = null;
        }
    }

    private void showDialog(CommonLoadingDialog.Status status, String message, Runnable onDismiss) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) return;

        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            // 复用并更新状态
            mLoadingDialog.setState(status, message, onDismiss);
        } else {
            // 新建并显示
            mLoadingDialog = new CommonLoadingDialog(activity);
            mLoadingDialog.setState(status, message, onDismiss);
            try {
                mLoadingDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}