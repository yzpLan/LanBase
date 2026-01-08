package com.yzplan.lanbase.ui;

import com.google.gson.JsonObject;
import com.yzplan.lanbase.base.BasePresenter;
import com.yzplan.lanbase.http.WanApi;
import com.yzplan.lanbase.utils.data.JsonUtil;

import java.util.HashMap;

import io.reactivex.disposables.Disposable;

public class ActivityMainPresenter extends BasePresenter<ActivityMainContract.View> implements ActivityMainContract.IPresenter {

    private WanApi.Service mApi;

    @Override
    protected void createApi() {
        mApi = WanApi.getInstance().get();
    }

    @Override
    public void doSend() {
        HashMap<String, Object> request = new HashMap<>();
        request.put("username", "yzp0625");
        request.put("password", "yzp0625");
        send(mApi.login(request), new ApiCall<>() {
            @Override
            public void onSubscribe(Disposable d) {
                getView().showLoading("登录中");
            }

            @Override
            public void onSuccess(JsonObject data) {
                getView().sendSuccess(JsonUtil.toJson(data));
            }

            @Override
            public void onError(Throwable e) {
                getView().sendFailure(parseErrorMsg(e));
            }
        });

    }
}
