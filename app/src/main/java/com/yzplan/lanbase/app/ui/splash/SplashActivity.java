package com.yzplan.lanbase.app.ui.splash;

import android.view.LayoutInflater;
import android.view.View;

import androidx.viewbinding.ViewBinding;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.yzplan.lanbase.app.arouter.ARouterPath;
import com.yzplan.lanbase.app.base.AppBaseActivity;
import com.yzplan.lanbase.app.databinding.ActivitySplashBinding;
import com.yzplan.lanbase.base.BasePresenter;
import com.yzplan.lanbase.manager.AlertDialogHelper;
import com.yzplan.lanbase.manager.SingleClickListener;
import com.yzplan.lanbase.utils.permission.PermissionConstants;
import com.yzplan.lanbase.utils.permission.PermissionUtils;
import com.yzplan.lanbase.utils.view.StatusBarUtils;

/**
 * 启动页
 */
@Route(path = ARouterPath.SplashActivity)
public class SplashActivity extends AppBaseActivity<ActivitySplashBinding, BasePresenter> {

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    @Override
    protected String initTitle() {
        baseBinding.titleBar.setBackVisible(false);
        return "";
    }

    @Override
    protected ActivitySplashBinding getViewBinding(LayoutInflater inflater) {
        return ActivitySplashBinding.inflate(inflater);
    }

    @Override
    protected void initData() {
        StatusBarUtils.setFullScreen(this);
        checkPermissions();
    }

    private void checkPermissions() {
        PermissionUtils.request(this, new PermissionUtils.PermissionCallback() {
            @Override
            public void onGranted() {
                initApp();
            }

            @Override
            public void onDenied(java.util.List<String> deniedList) {
                AlertDialogHelper.showSingleAlert(SplashActivity.this, "提示", "权限拒绝,退出应用", "确定", new SingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        finish();
                    }
                });
            }
        }, PermissionConstants.GROUP_STORAGE);
    }

    private void initApp() {
        ARouter.getInstance().build(ARouterPath.MainActivity).navigation(this);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}