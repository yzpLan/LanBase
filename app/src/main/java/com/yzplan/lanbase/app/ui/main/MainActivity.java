package com.yzplan.lanbase.app.ui.main;

import android.view.LayoutInflater;
import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.yzplan.lanbase.app.arouter.ARouterPath;
import com.yzplan.lanbase.app.base.AppBaseActivity;
import com.yzplan.lanbase.app.databinding.ActivityMainBinding;
import com.yzplan.lanbase.app.manager.LoginManager;
import com.yzplan.lanbase.base.BasePresenter;
import com.yzplan.lanbase.manager.AlertDialogHelper;
import com.yzplan.lanbase.manager.SingleClickListener;

@Route(path = ARouterPath.MainActivity)
public class MainActivity extends AppBaseActivity<ActivityMainBinding, BasePresenter> {
    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    @Override
    protected String initTitle() {
        baseBinding.titleBar.setBackVisible(false);
        return "首页";
    }

    @Override
    protected ActivityMainBinding getViewBinding(LayoutInflater inflater) {
        return ActivityMainBinding.inflate(inflater);
    }

    @Override
    protected void initData() {
        binding.btnArticleList.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                ARouter.getInstance().build(ARouterPath.ArticleActivity).navigation();
            }
        });
        binding.btnLogout.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                AlertDialogHelper.showConfirm(MainActivity.this, "提示", "是都确定退出登录?", v1 -> {
                    LoginManager.getInstance().logout(true);
                });
            }
        });

        binding.btnFunTest.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                toast("测试功能");
            }
        });

        binding.setJifen.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {

            }
        });

    }
}