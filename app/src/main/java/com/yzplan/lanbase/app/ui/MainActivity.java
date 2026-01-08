package com.yzplan.lanbase.app.ui;

import android.view.LayoutInflater;
import android.view.View;

import com.yzplan.lanbase.app.databinding.ActivityMainBinding;
import com.yzplan.lanbase.base.BaseActivity;
import com.yzplan.lanbase.manager.AlertDialogHelper;
import com.yzplan.lanbase.manager.SingleClickListener;
import com.yzplan.lanbase.utils.permission.PermissionConstants;
import com.yzplan.lanbase.utils.permission.PermissionUtils;

import java.util.List;

public class MainActivity extends BaseActivity<ActivityMainBinding, ActivityMainPresenter> implements ActivityMainContract.View {
    private static final String TAG = "MainActivity";

    @Override
    protected String initTitle() {
        return "首页";
    }

    @Override
    protected ActivityMainBinding getViewBinding(LayoutInflater inflater) {
        return ActivityMainBinding.inflate(inflater);
    }

    @Override
    protected ActivityMainPresenter createPresenter() {
        return new ActivityMainPresenter();
    }

    @Override
    protected void initData() {
        initPermission();
        initListener();
    }

    private void initListener() {
        binding.btnFun1.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                presenter.doSend();
            }
        });
        binding.btnFun2.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {


            }
        });
        binding.btnFun3.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                toast("3");
            }
        });
        binding.btnFun4.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                toast("4");
            }
        });

    }

    private void initPermission() {
        PermissionUtils.request(this, new PermissionUtils.PermissionCallback() {
            @Override
            public void onGranted() {

            }

            @Override
            public void onDenied(List<String> deniedList) {
                toast("权限拒绝");
                AlertDialogHelper.showSingleAlert(MainActivity.this, "提示", "权限拒绝,退出应用", "确定", new SingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        finish();
                    }
                });
            }
        }, PermissionConstants.GROUP_STORAGE);
    }

    @Override
    public void sendSuccess(String data) {
        binding.tvMessage.setText(data);
        showSuccess("sendSuccess");

    }

    @Override
    public void sendFailure(String errorMsg) {
        showError(errorMsg);
    }
}