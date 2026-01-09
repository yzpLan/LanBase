package com.yzplan.lanbase.app.ui.login;

import com.yzplan.lanbase.app.bean.response.LoginResponse;
import com.yzplan.lanbase.base.IBaseView;

/**
 * 登录页面的MVP契约接口
 */
public interface LoginContract {
    /**
     * 登录页面的View接口
     */
    interface View extends IBaseView {
        /**
         * 登录成功回调
         *
         * @param loginResponse 登录成功返回的用户信息
         */
        void loginSuccess(LoginResponse loginResponse);

        /**
         * 登录失败回调
         *
         * @param msg 错误信息
         */
        void loginFailure(String msg);
    }

    /**
     * 登录页面的Presenter接口
     */
    interface IPresenter {
        /**
         * 执行登录操作
         *
         * @param username 用户名
         * @param password 密码
         */
        void login(String username, String password);
    }
}
