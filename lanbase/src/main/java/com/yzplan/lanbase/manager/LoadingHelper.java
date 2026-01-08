package com.yzplan.lanbase.manager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.yzplan.lanbase.view.CommonLoadingDialog;


/**
 * Loading 弹窗管理助手
 * 负责 Loading 弹窗的显示、复用、状态更新和销毁
 */
public class LoadingHelper {

    private CommonLoadingDialog mLoadingDialog;
    private final AppCompatActivity activity;

    public LoadingHelper(AppCompatActivity activity) {
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
        if (mLoadingDialog != null) {
            if (mLoadingDialog.isAdded() || mLoadingDialog.isVisible()) {
                mLoadingDialog.dismissAllowingStateLoss();
            }
            mLoadingDialog = null;
        }
    }

    private void showDialog(CommonLoadingDialog.Status status, String message, Runnable onDismiss) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) return;

        if (mLoadingDialog != null && mLoadingDialog.isVisible() && mLoadingDialog.isAdded()) {
            // 复用
            mLoadingDialog.setState(status, message, onDismiss);
        } else {
            // 新建
            mLoadingDialog = new CommonLoadingDialog();
            mLoadingDialog.setState(status, message, onDismiss);
            try {
                FragmentManager fm = activity.getSupportFragmentManager();
                mLoadingDialog.show(fm, "loading_helper_dialog");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}