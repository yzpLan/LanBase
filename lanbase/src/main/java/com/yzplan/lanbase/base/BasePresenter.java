package com.yzplan.lanbase.base;


import com.google.gson.JsonSyntaxException;
import com.yzplan.lanbase.http.exception.ApiException;
import com.yzplan.lanbase.http.utils.RxUtils;
import com.yzplan.lanbase.utils.data.StringUtils;
import com.yzplan.lanbase.utils.log.L;

import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import retrofit2.HttpException;

/**
 * Presenter 网络请求的基类
 */
public abstract class BasePresenter<V extends IBaseView> {
    private static final String TAG = "BasePresenter";
    private WeakReference<V> mViewRef;
    protected CompositeDisposable mCompositeDisposable;

    public BasePresenter() {
        createApi();
    }

    public void attachView(V view) {
        mViewRef = new WeakReference<>(view);
        mCompositeDisposable = new CompositeDisposable();
    }

    public void detachView() {
        if (mViewRef != null) {
            mViewRef.clear();
            mViewRef = null;
        }
        if (mCompositeDisposable != null) {
            mCompositeDisposable.clear();
        }
    }

    protected V getView() {
        if (mViewRef != null) {
            return mViewRef.get();
        }
        return null;
    }

    protected boolean isViewAttached() {
        return mViewRef != null && mViewRef.get() != null;
    }

    protected abstract void createApi();

    /**
     * 发起网络请求
     *
     * @param single   网络请求
     * @param callback 接口请求的回调
     * @param <T>      泛型
     */
    protected <T, R extends BaseResponse<T>> void send(Single<R> single, final ApiCall<T> callback) {
        if (!isViewAttached()) {
            return;
        }
        // 这里的 RxUtils.handleRequest() 会自动推断出 R 和 T
        single.compose(RxUtils.handleRequest())
                .subscribe(createSafeObserver(callback));
    }

    /**
     * 创建一个“安全”的订阅者，它自动处理 isViewAttached()
     */
    protected <T> SafeObserver<T> createSafeObserver(final ApiCall<T> callback) {
        return new SafeObserver<>(callback);
    }

    protected void addDisposable(Disposable disposable) {
        if (mCompositeDisposable != null) {
            mCompositeDisposable.add(disposable);
        }
    }

    /**
     * SafeObserver 封装了 isViewAttached 判空
     */
    protected class SafeObserver<T> implements SingleObserver<T> {

        private final ApiCall<T> mCallback;

        SafeObserver(ApiCall<T> callback) {
            this.mCallback = callback;
        }

        @Override
        public void onSubscribe(Disposable d) {
            addDisposable(d);
            if (mCallback != null) {
                mCallback.onSubscribe(d);
            }
        }

        @Override
        public void onSuccess(T t) {
            if (isViewAttached() && mCallback != null) {
                mCallback.onSuccess(t);
            }
        }

        @Override
        public void onError(Throwable e) {
            L.e(TAG, "请求报错:" + e.getMessage());
            String code = "-1";
            String msg;
            if (e instanceof ApiException) {
                ApiException apiEx = (ApiException) e;
                code = apiEx.getCode();
                msg = apiEx.getMessage();
            } else if (e instanceof SocketTimeoutException) {
                msg = "网络请求超时";
            } else if (e instanceof ConnectException) {
                msg = "网络连接失败";
            } else {
                msg = e.getMessage();
            }
            if (interceptError(code, msg)) {
                return; // 子类处理了，结束
            }
            if (isViewAttached() && mCallback != null) {
                mCallback.onError(e);
            }
        }
    }

    /**
     * 错误信息转换
     */
    protected String parseErrorMsg(Throwable e) {
        if (e instanceof SocketTimeoutException) {
            return "网络连接超时，请重试";
        } else if (e instanceof JsonSyntaxException) {
            return "数据解析错误";
        } else if (e instanceof ConnectException) {
            return "服务器连接失败";
        } else if (e instanceof HttpException) {
            return "网络异常(" + ((HttpException) e).code() + ")";
        } else {
            return !StringUtils.isNullOrEmpty(e.getMessage()) ? e.getMessage() : "未知错误";
        }
    }

    /**
     * 子类拦截特殊错误 进行特殊处理
     *
     * @param code 错误码
     * @param msg  错误信息
     * @return true: 表示子类已经处理了，Base 不用管了
     */
    protected boolean interceptError(String code, String msg) {
        return false;
    }

    /**
     * 实际接口请求的回调
     */
    public interface ApiCall<T> {
        void onSubscribe(Disposable d);

        void onSuccess(T data);

        void onError(Throwable e);
    }
}