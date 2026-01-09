package com.yzplan.lanbase.app.manager;

import android.content.Intent;

import com.alibaba.android.arouter.launcher.ARouter;
import com.yzplan.lanbase.app.arouter.ARouterPath;
import com.yzplan.lanbase.app.bean.response.LoginResponse;
import com.yzplan.lanbase.app.constant.SpKey;
import com.yzplan.lanbase.manager.ILoginManager;
import com.yzplan.lanbase.utils.data.SpUtils;

/**
 * 登录/登出 业务管理类
 */
public class LoginManager implements ILoginManager<LoginResponse> {
    private static volatile LoginManager instance;

    private LoginManager() {
    }

    public static LoginManager getInstance() {
        if (instance == null) {
            synchronized (LoginManager.class) {
                if (instance == null) {
                    instance = new LoginManager();
                }
            }
        }
        return instance;
    }

    @Override
    public void login(LoginResponse userInfo) {
        saveUserInfo(userInfo);
        ARouter.getInstance().build(ARouterPath.MainActivity)
                .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK)
                .navigation();
    }

    @Override
    public void logout(boolean isForce) {
        clearAllUserInfo();
        ARouter.getInstance().build(ARouterPath.LoginActivity)
                .withBoolean("is_logout", isForce)
                .withFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .navigation();
    }

    @Override
    public boolean isLogin() {
        return SpUtils.getInstance().getBoolean(SpKey.IS_LOGIN, false);
    }

    @Override
    public void saveUserInfo(LoginResponse loginResponse) {
        SpUtils.getInstance().putBoolean(SpKey.IS_LOGIN, true);
    }

    @Override
    public void clearAllUserInfo() {
        SpUtils.getInstance().remove(SpKey.IS_LOGIN);
    }
}
