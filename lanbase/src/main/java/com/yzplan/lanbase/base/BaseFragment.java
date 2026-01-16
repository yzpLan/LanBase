package com.yzplan.lanbase.base;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

import com.yzplan.lanbase.databinding.LibFragmentBaseBinding;
import com.yzplan.lanbase.manager.LoadingHelper;


/**
 * 基础 Fragment 基类 (集成 MVP + ViewBinding)
 * <p>
 * 职责：
 * 1. 集成 ViewBinding (父布局+子布局双重绑定)
 * 2. 集成 MVP 架构 (生命周期自动绑定与解绑)
 * 3. 统一 Loading/Success/Error 弹窗封装
 * 4. 针对 Fragment 视图销毁时的内存泄露优化
 *
 * @param <VB> 子类布局对应的 ViewBinding 类型
 * @param <P>  Presenter 类型，若无业务逻辑可传 BasePresenter
 */
public abstract class BaseFragment<VB extends ViewBinding, P extends BasePresenter> extends Fragment implements IBaseView {

    // 子类布局的 Binding 对象
    protected VB binding;
    // 基础壳布局的 Binding 对象
    protected LibFragmentBaseBinding baseBinding;
    // MVP 控制层
    protected P presenter;
    // 加载等待框助手
    private LoadingHelper mLoadingHelper;
    // 通讯接口
    protected OnActionCallback mActionCallback;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // 自动绑定：判断宿主 Activity 是否实现了此内部接口
        if (context instanceof OnActionCallback) {
            mActionCallback = (OnActionCallback) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 1. 初始化基础壳布局
        baseBinding = LibFragmentBaseBinding.inflate(inflater, container, false);

        // 2. 加载子类布局并添加到父容器
        binding = getViewBinding(inflater, container);
        if (binding != null) {
            baseBinding.layoutContent.addView(binding.getRoot());
        }
        return baseBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 3. [MVP核心] 初始化 Presenter 并绑定 View
        presenter = createPresenter();
        if (presenter != null) {
            presenter.attachView(this);
        }

        // 4. 初始化业务组件
        if (getActivity() != null) {
            mLoadingHelper = new LoadingHelper(getActivity());
        }

        // 5. 业务初始化
        initData();
    }

    /**
     * 提供给 Activity 主动调用的刷新入口
     */
    public void onRefresh() {}


    // ==================== 子类必须实现的方法 ====================

    /**
     * 初始化 Presenter
     *
     * @return 返回具体的 Presenter 实例；若无需 MVP 模式则返回 null
     */
    protected abstract P createPresenter();

    /**
     * 子类必须重写此方法，返回对应的 ViewBinding 实例
     */
    protected abstract VB getViewBinding(LayoutInflater inflater, ViewGroup container);

    /**
     * 初始化数据与业务逻辑
     */
    protected abstract void initData();

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
        if (mLoadingHelper != null) mLoadingHelper.dismiss();
    }

    // ==================== 生命周期管理与内存优化 ====================


    /**
     * Fragment 与 Activity 通信的动作回调
     */
    public interface OnActionCallback {
        /**
         * @param action 动作类型
         * @param data   携带数据
         */
        void onFragmentAction(String action, Object data);
    }

    /**
     * Fragment 的视图销毁时调用
     * 注意：Fragment 的生命周期长于其视图，必须在此处置空 Binding 避免内存泄漏
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 置空 Binding 引用
        binding = null;
        baseBinding = null;
        // 销毁弹窗
        if (mLoadingHelper != null) {
            mLoadingHelper.dismiss();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // 释放接口和 Presenter
        mActionCallback = null;
        if (presenter != null) {
            presenter.detachView();
            presenter = null;
        }
    }
}