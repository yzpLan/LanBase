package com.yzplan.lanbase.ui;


import com.yzplan.lanbase.base.IBaseView;

public interface ActivityMainContract {
    interface View extends IBaseView {
        void sendSuccess(String data);

        void sendFailure(String errorMsg);
    }

    // 定义 presenter 接口
    interface IPresenter {
        void doSend();
    }
}
