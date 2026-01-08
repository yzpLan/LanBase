package com.yzplan.lanbase.manager;


/**
 * 登录业务管理接口
 */
public interface ILoginManager<T> {
    /**
     * 用户登录成功处理
     *
     * @param userInfo 用户信息
     */
    void login(T userInfo);

    /**
     * 用户登出处理
     *
     * @param isForce 是否强制登出
     */
    void logout(boolean isForce);

    /**
     * 判断用户是否已登录
     *
     * @return 是否已登录
     */
    boolean isLogin();

    /**
     * 保存用户信息
     *
     * @param userInfo 用户信息
     */
    void saveUserInfo(T userInfo);


    /**
     * 清除所有用户相关信息
     */
    void clearAllUserInfo();
}

