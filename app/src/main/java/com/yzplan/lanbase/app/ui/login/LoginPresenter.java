package com.yzplan.lanbase.app.ui.login;

import com.yzplan.lanbase.app.bean.response.LoginResponse;
import com.yzplan.lanbase.app.http.api.WanApi;
import com.yzplan.lanbase.app.manager.LoginManager;
import com.yzplan.lanbase.base.BasePresenter;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.disposables.Disposable;

/**
 * 登录页面的Presenter实现
 */
public class LoginPresenter extends BasePresenter<LoginContract.View> implements LoginContract.IPresenter {

    private WanApi.Service mApi;

    @Override
    protected void createApi() {
        // 创建WanApi服务实例
        mApi = WanApi.getInstance().get();
    }

    @Override
    public void login(String username, String password) {
        // 构建请求参数
        Map<String, Object> request = new HashMap<>();
        request.put("username", username);
        request.put("password", password);
        // 发送网络请求进行登录
        send(mApi.login(request), new ApiCall<>() {
            @Override
            public void onSubscribe(Disposable d) {
                getView().showLoading("登录中...");
            }

            @Override
            public void onSuccess(LoginResponse data) {
                // 请求成功，隐藏加载并回调View层
                if (data != null) {
                    LoginManager.getInstance().login(data);
                    getView().loginSuccess(data);
                } else {
                    getView().loginFailure("登录失败：数据为空");
                }
            }

            @Override
            public void onError(Throwable e) {
                // 请求失败，隐藏加载并回调View层，解析错误信息
                getView().loginFailure(parseErrorMsg(e));
            }
        });
    }
}
