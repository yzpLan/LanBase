package com.yzplan.lanbase.app.ui.login;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;

import com.alibaba.android.arouter.launcher.ARouter;
import com.yzplan.lanbase.app.R;
import com.yzplan.lanbase.app.arouter.ARouterPath;
import com.yzplan.lanbase.app.bean.response.Article;
import com.yzplan.lanbase.app.databinding.FragmentMainBinding;
import com.yzplan.lanbase.base.BaseFragment;
import com.yzplan.lanbase.manager.RecycleViewHelper;

/**
 * Main 页面 Fragment
 * 布局文件: fragment_main.xml
 */
public class MainFragment extends BaseFragment<FragmentMainBinding, MainPresenter> implements MainContract.View {
    private RecycleViewHelper<String> recycleViewHelper;

    @Override
    protected MainPresenter createPresenter() {
        return new MainPresenter();
    }

    @Override
    protected FragmentMainBinding getViewBinding(LayoutInflater inflater, ViewGroup container) {
        // 注意：Fragment 的 Binding 需要传入 container
        return FragmentMainBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initData() {
        // 初始化逻辑
        // 如果要调 Activity：if (mActionCallback != null) mActionCallback.onFragmentAction("action", data);
        recycleViewHelper = new RecycleViewHelper<String>(binding.listView)
                .setPageConfig(0, 10)
                .setLayout(R.layout.item_article, (holder, item, position) -> {
                }).loadFrom((page, pageSize) -> {
                }).setOnItemClickListener((view, item, position) -> {
                }).start();
    }

    @Override
    public void onRefresh() {
        // Activity 调用时的刷新逻辑
    }
}