package com.yzplan.lanbase.app.ui.article;

import com.yzplan.lanbase.app.bean.response.ArticleListResponse;
import com.yzplan.lanbase.app.bean.response.BannerBean;
import com.yzplan.lanbase.app.http.api.WanApi;
import com.yzplan.lanbase.base.BasePresenter;

import java.util.List;

import io.reactivex.disposables.Disposable;

public class ArticlePresenter extends BasePresenter<ArticleContract.View> implements ArticleContract.IPresenter {

    private WanApi.Service mApi;

    @Override
    protected void createApi() {
        mApi = WanApi.getInstance().get();
    }

    @Override
    public void getArticleList(int pageNo, int pageSize) {
        send(mApi.getArticleList(pageNo, pageSize), new ApiCall<>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onSuccess(ArticleListResponse data) {
                getView().getArticleListSuccess(data);
            }

            @Override
            public void onError(Throwable e) {
                getView().getArticleListFail(parseErrorMsg(e));
            }
        });
    }

    @Override
    public void getBanner() {
        send(mApi.getBanner(), new ApiCall<>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(List<BannerBean> data) {
                getView().getBannerSuccess(data);
            }

            @Override
            public void onError(Throwable e) {
                getView().getBannerFail(parseErrorMsg(e));
            }
        });
    }
}
