package com.yzplan.lanbase.manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener;
import com.yzplan.lanbase.databinding.LibLayoutCommonListBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView 列表助手
 * <p>
 * 功能：封装了 SmartRefreshLayout + RecyclerView 的常用逻辑：
 * 1. 分页管理
 * 2. 自动刷新/加载更多
 * 3. 缺省页切换
 * 4. Adapter 快速构建
 * 5. 防止点击崩溃的安全处理
 * </p>
 *
 * @param <T> 列表数据的实体类型
 */
public class RecycleViewHelper<T> {

    private final SmartRefreshLayout refreshLayout;
    private final RecyclerView recyclerView;
    private InnerAdapter adapter;
    private final List<T> mData = new ArrayList<>();

    // --- 配置参数 ---
    private int pageNo = 1;
    private int startPageNo = 1;
    private int pageSize = 10;
    private boolean mEnableRefresh = true; // 默认开启刷新
    private View emptyView;

    // --- 回调接口 ---
    private ViewBinder<T> binder;
    private DataLoader loader;
    private OnItemClickListener<T> itemClickListener;

    /**
     * 快捷构造函数：支持 ViewBinding
     * 自动关联 refreshLayout, recyclerView 和 tvEmpty
     */
    public RecycleViewHelper(LibLayoutCommonListBinding binding) {
        this(binding.refreshLayout, binding.recyclerView);
        setEmptyView(binding.tvEmpty);
    }

    /**
     * 基础构造函数
     */
    public RecycleViewHelper(SmartRefreshLayout refreshLayout, RecyclerView recyclerView) {
        this.refreshLayout = refreshLayout;
        this.recyclerView = recyclerView;
        initViews();
    }

    /**
     * 初始化基础视图配置
     */
    private void initViews() {
        Context context = recyclerView.getContext();
        // 默认垂直线性布局 (可通过 setLayoutManager 修改)
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        // 设置默认的 Header 和 Footer 样式
        refreshLayout.setRefreshHeader(new ClassicsHeader(context));
        refreshLayout.setRefreshFooter(new ClassicsFooter(context));
        // 设置内容不满一页时，是否开启上拉加载 (建议开启，否则只有一条数据时无法上拉)
        refreshLayout.setEnableLoadMoreWhenContentNotFull(true);
        // 设置刷新监听
        refreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                pageNo = startPageNo; // 重置页码
                refreshLayout.setNoMoreData(false); // 重置"没有更多数据"状态
                if (loader != null) loader.load(pageNo, pageSize);
            }

            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                pageNo++; // 页码自增
                if (loader != null) loader.load(pageNo, pageSize);
            }
        });
    }

    // ================== 链式配置方法 ==================

    /**
     * 1. [必选] 设置布局和绑定逻辑
     *
     * @param layoutId item 布局资源 ID
     * @param binder   数据绑定回调 (onBindViewHolder)
     */
    public RecycleViewHelper<T> setLayout(@LayoutRes int layoutId, @NonNull ViewBinder<T> binder) {
        this.binder = binder;
        this.adapter = new InnerAdapter(layoutId);
        this.recyclerView.setAdapter(this.adapter);
        return this;
    }

    /**
     * 2. [必选] 设置加载数据的逻辑
     *
     * @param loader 网络请求回调
     */
    public RecycleViewHelper<T> loadFrom(@NonNull DataLoader loader) {
        this.loader = loader;
        return this;
    }

    /**
     * [可选] 设置点击事件
     */
    public RecycleViewHelper<T> setOnItemClickListener(OnItemClickListener<T> listener) {
        this.itemClickListener = listener;
        return this;
    }

    /**
     * [可选] 禁用下拉刷新和上拉加载，仅作为普通列表使用
     */
    public RecycleViewHelper<T> setPureListMode() {
        this.mEnableRefresh = false;
        this.refreshLayout.setEnableRefresh(false);      // 禁用下拉手势
        this.refreshLayout.setEnableLoadMore(false);     // 禁用上拉手势
        this.refreshLayout.setEnableOverScrollDrag(false); // 禁用越界回弹效果
        return this;
    }

    /**
     * [可选] 设置缺省页 (空数据时显示的 View)
     */
    public RecycleViewHelper<T> setEmptyView(View emptyView) {
        this.emptyView = emptyView;
        if (this.emptyView != null) this.emptyView.setVisibility(View.GONE);
        return this;
    }

    /**
     * [可选] 设置分页配置
     *
     * @param startPageNo 起始页码 (通常为 1)
     * @param pageSize    每页条数 (通常为 10 或 20)
     */
    public RecycleViewHelper<T> setPageConfig(int startPageNo, int pageSize) {
        this.startPageNo = startPageNo;
        this.pageNo = startPageNo;
        this.pageSize = pageSize;
        return this;
    }

    /**
     * [可选] 自定义 LayoutManager (支持网格、瀑布流等)
     * 必须在 start() 之前调用
     */
    public RecycleViewHelper<T> setLayoutManager(RecyclerView.LayoutManager manager) {
        this.recyclerView.setLayoutManager(manager);
        return this;
    }

    /**
     * 3. 启动！(包含安全检查)
     * 必须在配置完 setLayout 和 setData 后调用
     */
    public RecycleViewHelper<T> start() {
        if (adapter == null) {
            throw new RuntimeException("RecycleViewHelper 报错：请先调用 .setLayout() 设置布局！");
        }
        if (loader == null) {
            throw new RuntimeException("RecycleViewHelper 报错：请先调用 .setData() 设置加载逻辑！");
        }
        if (mEnableRefresh) {
            refreshLayout.autoRefresh(); // 正常模式：触发自动刷新动画
        } else {
            // 纯列表模式：不显示动画，直接静默加载第一页数据
            loader.load(startPageNo, pageSize);
        }
        return this;
    }

    /**
     * 手动触发刷新 (例如筛选条件改变后调用)
     */
    public void refresh() {
        refreshLayout.autoRefresh();
    }

    // ================== 本地数据操作  ==================

    /**
     * 获取当前列表数据源 (用于只读或遍历)
     */
    public List<T> getData() {
        return mData;
    }

    /**
     * 删除指定位置的 Item (例如删除订单)
     */
    public void remove(int position) {
        if (position >= 0 && position < mData.size()) {
            mData.remove(position);
            adapter.notifyItemRemoved(position);
            // 如果删光了，要显示空页面
            checkEmptyState();
        }
    }

    /**
     * 刷新指定位置的 Item (例如修改状态后刷新UI)
     */
    public void notifyItemChanged(int position) {
        if (position >= 0 && position < mData.size()) {
            adapter.notifyItemChanged(position);
        }
    }

    // ================== 数据处理与回调 ==================

    /**
     * 请求成功处理
     *
     * @param newData 新请求回来的数据列表
     */
    @SuppressLint("NotifyDataSetChanged")
    public void notifyData(List<T> newData) {
        refreshLayout.finishRefresh();
        refreshLayout.finishLoadMore();

        int size = (newData == null) ? 0 : newData.size();

        if (pageNo == startPageNo) {
            // --- 刷新逻辑 ---
            mData.clear();
            if (newData != null) mData.addAll(newData);
            adapter.notifyDataSetChanged();
            checkEmptyState(); // 只有刷新时才检查空页面
        } else {
            // --- 加载更多逻辑 ---
            if (size > 0) {
                int start = mData.size();
                mData.addAll(newData);
                adapter.notifyItemRangeInserted(start, size);
            } else {
                pageNo--; // 本次没有数据，页码回退，防止下次加载跳页
                refreshLayout.finishLoadMoreWithNoMoreData(); // 显示"没有更多数据"
            }
        }
    }

    /**
     * 请求失败处理
     */
    public void notifyError() {
        refreshLayout.finishRefresh(false);
        refreshLayout.finishLoadMore(false);
        // 如果不是第一页失败，页码要回退
        if (pageNo > startPageNo) pageNo--;
        checkEmptyState();
    }

    /**
     * 检查并切换 空页面/列表 的显示状态
     */
    private void checkEmptyState() {
        if (emptyView == null) return;
        boolean isEmpty = mData.isEmpty();
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    // ================== 接口定义 ==================

    public interface ViewBinder<T> {
        void bind(BaseViewHolder holder, T item, int position);
    }

    public interface DataLoader {
        void load(int page, int pageSize);
    }

    public interface OnItemClickListener<T> {
        void onItemClick(View view, T item, int position);
    }

    // ================== 内部 Adapter ==================

    private class InnerAdapter extends RecyclerView.Adapter<BaseViewHolder> {
        private final int layoutId;

        public InnerAdapter(int layoutId) {
            this.layoutId = layoutId;
        }

        @NonNull
        @Override
        public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            return new BaseViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
            T item = mData.get(position);
            binder.bind(holder, item, position);

            // 设置点击事件
            if (itemClickListener != null) {
                holder.itemView.setOnClickListener(v -> {
                    // 【安全优化】使用 getBindingAdapterPosition 获取位置
                    // getAdapterPosition() 在动画期间可能返回 -1 (NO_POSITION)，导致崩溃
                    int pos = holder.getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION && pos < mData.size()) {
                        itemClickListener.onItemClick(v, mData.get(pos), pos);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }
    }

    // ================== ViewHolder ==================

    public static class BaseViewHolder extends RecyclerView.ViewHolder {
        // 使用 SparseArray 缓存 View，比 Map 性能更好
        private final SparseArray<View> views = new SparseArray<>();

        public BaseViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public <V extends View> V getView(@IdRes int viewId) {
            View view = views.get(viewId);
            if (view == null) {
                view = itemView.findViewById(viewId);
                views.put(viewId, view);
            }
            return (V) view;
        }
    }
}