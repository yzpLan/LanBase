package com.yzplan.lanbase.app.ui.login;

import android.view.LayoutInflater;
import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.yzplan.lanbase.app.arouter.ARouterPath;
import com.yzplan.lanbase.app.base.AppBaseActivity;
import com.yzplan.lanbase.app.bean.response.LoginResponse;
import com.yzplan.lanbase.app.databinding.ActivityLoginBinding;
import com.yzplan.lanbase.app.manager.LoginManager;
import com.yzplan.lanbase.manager.SingleClickListener;

/**
 * 登录页面Activity
 */
@Route(path = ARouterPath.LoginActivity)
public class LoginActivity extends AppBaseActivity<ActivityLoginBinding, LoginPresenter> implements LoginContract.View {


    @Override
    protected LoginPresenter createPresenter() {
        return new LoginPresenter();
    }

    @Override
    protected String initTitle() {
        baseBinding.titleBar.setBackVisible(false);
        return "登录";
    }

    @Override
    protected ActivityLoginBinding getViewBinding(LayoutInflater inflater) {
        return ActivityLoginBinding.inflate(inflater);
    }

    @Override
    protected void initData() {
        binding.btnLogin.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                presenter.login(binding.etUsername.getText().toString(), binding.etPassword.getText().toString());
            }
        });
    }

    @Override
    public void loginSuccess(LoginResponse loginResponse) {
        showSuccess("登录成功", () -> LoginManager.getInstance().login(loginResponse));
    }

    @Override
    public void loginFailure(String msg) {
        showError("登录失败:" + msg);
    }
}
