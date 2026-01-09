package com.yzplan.lanbase.app.ui.article;

import android.text.Html;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.youth.banner.adapter.BannerImageAdapter;
import com.youth.banner.holder.BannerImageHolder;
import com.youth.banner.indicator.CircleIndicator;
import com.youth.banner.listener.OnBannerListener;
import com.yzplan.lanbase.app.R;
import com.yzplan.lanbase.app.arouter.ARouterPath;
import com.yzplan.lanbase.app.base.AppBaseActivity;
import com.yzplan.lanbase.app.bean.response.Article;
import com.yzplan.lanbase.app.bean.response.ArticleListResponse;
import com.yzplan.lanbase.app.bean.response.BannerBean;
import com.yzplan.lanbase.app.databinding.ActivityArticleBinding;
import com.yzplan.lanbase.manager.RecycleViewHelper;
import com.yzplan.lanbase.utils.view.GlideUtils;

import java.util.List;

@Route(path = ARouterPath.ArticleActivity)
public class ArticleActivity extends AppBaseActivity<ActivityArticleBinding, ArticlePresenter> implements ArticleContract.View {
    private RecycleViewHelper<Article> recycleViewHelper;

    @Override
    protected String initTitle() {
        return "文章列表";
    }

    @Override
    protected ActivityArticleBinding getViewBinding(LayoutInflater inflater) {
        return ActivityArticleBinding.inflate(inflater);
    }

    @Override
    protected ArticlePresenter createPresenter() {
        return new ArticlePresenter();
    }

    @Override
    protected void initData() {
        initBanner();
        initRecycleView();
    }

    private void initBanner() {
        presenter.getBanner();
    }

    private void initRecycleView() {
        recycleViewHelper = new RecycleViewHelper<Article>(binding.listView)
                .setPageConfig(0, 10)
                .setLayout(R.layout.item_article, (holder, item, position) -> {
                    TextView tv_title = holder.getView(R.id.tv_title);
                    TextView tv_author = holder.getView(R.id.tv_author);
                    TextView tv_date = holder.getView(R.id.tv_date);
                    TextView tv_chapter = holder.getView(R.id.tv_chapter);
                    // 设置标题 (处理可能存在的 HTML 转义字符)
                    tv_title.setText(Html.fromHtml(item.getTitle(), Html.FROM_HTML_MODE_LEGACY));
                    // 设置作者
                    String author = item.getAuthor().isEmpty() ? item.getShareUser() : item.getAuthor();
                    tv_author.setText("作者: " + author);
                    // 设置时间与分类
                    tv_date.setText(item.getNiceDate());
                    tv_chapter.setText(item.getSuperChapterName() + " / " + item.getChapterName());
                }).loadFrom((page, pageSize) -> {
                    presenter.getArticleList(page, pageSize);
                }).setOnItemClickListener((view, item, position) -> {
                    ARouter.getInstance().build(ARouterPath.WebViewActivity).withString("url", item.getLink()).navigation();
                }).start();
    }

    @Override
    public void getArticleListSuccess(ArticleListResponse articleListResponse) {
        recycleViewHelper.notifyData(articleListResponse.getDatas());
    }

    @Override
    public void getArticleListFail(String errorMsg) {
        recycleViewHelper.notifyError();
    }

    @Override
    public void getBannerSuccess(List<BannerBean> beanList) {
        binding.banner
                .setAdapter(new BannerImageAdapter<>(beanList) {
                    @Override
                    public void onBindView(BannerImageHolder holder, BannerBean data, int position, int size) {
                        GlideUtils.load(ArticleActivity.this, data.getImagePath(), holder.imageView);
                    }
                })
                .addBannerLifecycleObserver(this)
                .setIndicator(new CircleIndicator(this))
                .setOnBannerListener(new OnBannerListener<BannerBean>() {
                    @Override
                    public void OnBannerClick(BannerBean data, int position) {
                        ARouter.getInstance()
                                .build(ARouterPath.WebViewActivity)
                                .withString("url", data.getUrl())
                                .navigation();
                    }
                });
    }

    @Override
    public void getBannerFail(String errorMsg) {

    }
}