package com.yzplan.lanbase.app.ui.web;

import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.yzplan.lanbase.app.arouter.ARouterPath;
import com.yzplan.lanbase.app.base.AppBaseActivity;
import com.yzplan.lanbase.app.databinding.ActivityWebViewBinding;
import com.yzplan.lanbase.base.BasePresenter;

@Route(path = ARouterPath.WebViewActivity)
public class WebViewActivity extends AppBaseActivity<ActivityWebViewBinding, BasePresenter> {

    @Autowired
    String url;

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    @Override
    protected String initTitle() {
        return "文章详情";
    }

    @Override
    protected ActivityWebViewBinding getViewBinding(LayoutInflater inflater) {
        return ActivityWebViewBinding.inflate(inflater);
    }

    @Override
    protected void initData() {
        WebView webView = binding.webView;
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true); // 开启 JS 支持
        settings.setDomStorageEnabled(true); // 开启缓存，防止某些网页显示空白
        settings.setUseWideViewPort(true);   // 自适应屏幕
        settings.setLoadWithOverviewMode(true);
        webView.setWebViewClient(new WebViewClient()); // 在应用内打开链接，不跳浏览器
        webView.loadUrl(url);

        ProgressBar progressBar = binding.pbWeb;
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE); // 加载完成隐藏
                } else {
                    progressBar.setVisibility(View.VISIBLE); // 加载中显示
                    progressBar.setProgress(newProgress); // 更新进度
                }
                super.onProgressChanged(view, newProgress);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
            }
        });
    }
}
