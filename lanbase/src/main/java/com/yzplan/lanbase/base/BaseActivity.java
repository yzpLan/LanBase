package com.yzplan.lanbase.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewbinding.ViewBinding;

import com.yzplan.lanbase.R;
import com.yzplan.lanbase.databinding.LibActivityBaseBinding;
import com.yzplan.lanbase.manager.ActivityManager;
import com.yzplan.lanbase.manager.LoadingHelper;
import com.yzplan.lanbase.utils.data.StringUtils;
import com.yzplan.lanbase.utils.system.KeyboardUtils;
import com.yzplan.lanbase.utils.view.StatusBarUtils;
import com.yzplan.lanbase.utils.view.ToastUtils;

/**
 * 基础 Activity 基类 (集成 MVP + ViewBinding)
 * <p>
 * 职责：
 * 1. 集成 ViewBinding (父布局+子布局双重绑定)
 * 2. 集成 MVP 架构 (生命周期自动绑定与解绑)
 * 3. 沉浸式状态栏与标题栏封装
 * 4. 通用 Loading/Success/Error 弹窗封装
 * 5. Activity 栈管理与软键盘自动隐藏
 *
 * @param <VB> 子类布局对应的 ViewBinding 类型
 * @param <P>  Presenter 类型，若无业务逻辑可传 BasePresenter
 */
public abstract class BaseActivity<VB extends ViewBinding, P extends BasePresenter> extends AppCompatActivity implements IBaseView {

    protected VB binding;
    protected LibActivityBaseBinding baseBinding;
    protected P presenter;
    private LoadingHelper mLoadingHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 1. 初始化基础壳布局 (ViewBinding)
        baseBinding = LibActivityBaseBinding.inflate(getLayoutInflater());
        // 2. 基础配置：入栈管理、状态栏初始化
        ActivityManager.getInstance().addActivity(this);
        initStatusBar();
        // 3. 加载子类布局并添加到父容器内容区
        binding = getViewBinding(getLayoutInflater());
        if (binding != null) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
            baseBinding.layoutContent.addView(binding.getRoot(), params);
        }
        setContentView(baseBinding.getRoot());
        // 4. [MVP核心] 初始化 Presenter 并绑定 View
        presenter = createPresenter();
        if (presenter != null) {
            presenter.attachView(this);
        }
        // 5. 设置标题栏与业务初始化
        initHeaderConfig();
        mLoadingHelper = new LoadingHelper(this);
        initData();
    }

    /**
     * 标题栏配置逻辑
     */
    private void initHeaderConfig() {
        if (!StringUtils.isNullOrEmpty(initTitle())) {
            baseBinding.titleBar.setTitle(initTitle());
        } else {
            baseBinding.titleBar.setVisibility(View.GONE);
        }
    }

    // ==================== 子类必须/选择实现的方法 ====================

    /**
     * 初始化 Presenter
     *
     * @return 返回子类具体的 Presenter 实例；若无需 MVP 模式则返回 null
     */
    protected abstract P createPresenter();

    /**
     * 初始化页面标题
     *
     * @return 标题文字，返回 null 或空字符串则隐藏标题栏
     */
    protected abstract String initTitle();

    /**
     * 获取 ViewBinding 实例
     */
    protected abstract VB getViewBinding(LayoutInflater inflater);

    /**
     * 业务逻辑初始化
     */
    protected abstract void initData();

    // ==================== 状态栏与工具封装 ====================

    protected void initStatusBar() {
        StatusBarUtils.setStatusBar(this, ContextCompat.getColor(this, R.color.lib_main_red), false);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        KeyboardUtils.autoHideKeyboard(this, ev);
        return super.dispatchTouchEvent(ev);
    }

    protected void toast(String message) {
        ToastUtils.showShort(message);
    }

    // ==================== IBaseView (Loading模块) 实现 ====================

    @Override
    public void showLoading(String message) {
        mLoadingHelper.showLoading(message, null);
    }

    @Override
    public void showLoading(String message, Runnable onDismiss) {
        mLoadingHelper.showLoading(message, onDismiss);
    }

    @Override
    public void showSuccess(String message) {
        mLoadingHelper.showSuccess(message, null);
    }

    @Override
    public void showSuccess(String message, Runnable onDismiss) {
        mLoadingHelper.showSuccess(message, onDismiss);
    }

    @Override
    public void showError(String errorMsg) {
        mLoadingHelper.showError(errorMsg, null);
    }

    @Override
    public void showError(String errorMsg, Runnable onDismiss) {
        mLoadingHelper.showError(errorMsg, onDismiss);
    }

    @Override
    public void hideLoading() {
        mLoadingHelper.dismiss();
    }

    // ==================== 生命周期销毁 ====================

    @Override
    protected void onDestroy() {
        // 1. MVP 解绑，防止内存泄漏
        if (presenter != null) {
            presenter.detachView();
            presenter = null;
        }
        // 2. 清理管理栈与弹窗
        ActivityManager.getInstance().removeActivity(this);
        if (mLoadingHelper != null) {
            mLoadingHelper.dismiss();
        }
        super.onDestroy();
    }
}