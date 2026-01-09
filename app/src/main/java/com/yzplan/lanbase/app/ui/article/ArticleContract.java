package com.yzplan.lanbase.app.ui.article;


import com.yzplan.lanbase.app.bean.response.ArticleListResponse;
import com.yzplan.lanbase.base.IBaseView;

public interface ArticleContract {
    interface View extends IBaseView {
        void getArticleListSuccess(ArticleListResponse articleListResponse);

        void getArticleListFail(String errorMsg);
    }

    // 定义 presenter 接口
    interface IPresenter {
        void getArticleList(int pageNo, int pageSize);
    }
}
