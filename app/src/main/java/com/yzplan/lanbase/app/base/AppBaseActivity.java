package com.yzplan.lanbase.app.base;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

import com.alibaba.android.arouter.launcher.ARouter;
import com.yzplan.lanbase.base.BaseActivity;
import com.yzplan.lanbase.base.BasePresenter;

public abstract class AppBaseActivity<VB extends ViewBinding, P extends BasePresenter> extends BaseActivity<VB, P> {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ARouter.getInstance().inject(this);
        super.onCreate(savedInstanceState);
    }
}
