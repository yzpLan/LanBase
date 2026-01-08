package com.yzplan.lanbase.http.utils;


import com.yzplan.lanbase.base.BaseResponse;
import com.yzplan.lanbase.http.exception.ApiException;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class RxUtils {

    /**
     * 统一线程处理 + 数据剥离 + 异常处理
     *
     * @param <T> 核心数据类型
     * @param <R> 响应包装类型 (必须实现 BaseResponse<T>)
     */
    public static <T, R extends BaseResponse<T>> SingleTransformer<R, T> handleRequest() {
        return upstream -> upstream
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(response -> {
                    if (response.isSuccess()) {
                        T data = response.getData();
                        if (data == null) {
                            return Single.error(new ApiException("无应答数据", response.getCode()));
                        } else {
                            return Single.just(data);
                        }
                    } else {
                        return Single.error(new ApiException(response.getMessage(), response.getCode()));
                    }
                });
    }

    // ==================== 1. 延迟执行 (Timer) ====================

    /**
     * 默认：主线程执行 (最常用)
     */
    public static Disposable timer(long delay, TimeUnit unit, Consumer<Long> onNext) {
        return timer(delay, unit, AndroidSchedulers.mainThread(), onNext);
    }

    /**
     * 高级：自定义线程执行 (传入 Schedulers.io() 即可在后台执行)
     */
    public static Disposable timer(long delay, TimeUnit unit, Scheduler scheduler, Consumer<Long> onNext) {
        return Observable.timer(delay, unit)
                .observeOn(scheduler)
                .subscribe(onNext, Throwable::printStackTrace);
    }

    // ==================== 2. 轮询执行 (Interval) ====================

    /**
     * 默认：主线程执行
     */
    public static Disposable interval(long period, TimeUnit unit, Consumer<Long> onNext) {
        return interval(period, unit, AndroidSchedulers.mainThread(), onNext);
    }

    /**
     * 高级：自定义线程执行
     */
    public static Disposable interval(long period, TimeUnit unit, Scheduler scheduler, Consumer<Long> onNext) {
        return Observable.interval(0, period, unit)
                .observeOn(scheduler)
                .subscribe(onNext, Throwable::printStackTrace);
    }

    // ==================== 3. 倒计时 / 有限次执行 (IntervalRange) ====================

    /**
     * @param totalTime 总时长 (例如 3)
     * @param onNext    回调：直接给你剩余时间 (例如 3, 2, 1, 0)
     */
    public static Disposable countDown(long totalTime, Consumer<Long> onNext) {
        return intervalRange(0, totalTime + 1, 0, 1, TimeUnit.SECONDS, i -> {
            if (onNext != null) {
                onNext.accept(totalTime - i);
            }
        });
    }

    /**
     * [通用] 有限次数的轮询 / 倒计时
     *
     * @param start        起始数值 (通常是 0)
     * @param count        执行次数 (例如 5次)
     * @param initialDelay 首次执行的延迟时间
     * @param period       每次执行的间隔时间
     * @param unit         时间单位
     * @param onNext       回调 (主线程)
     */
    public static Disposable intervalRange(long start, long count, long initialDelay, long period, TimeUnit unit, Consumer<Long> onNext) {
        return Observable.intervalRange(start, count, initialDelay, period, unit)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onNext, Throwable::printStackTrace);
    }


    // ==================== 4. 通用子线程任务 (doTask) ====================

    /**
     * 执行耗时任务并切换回主线程
     *
     * @param backgroundWork 在子线程执行的逻辑 (返回一个结果 T)
     * @param onNext         在主线程接收结果 T
     * @param <T>            泛型
     * @return Disposable (用于取消)
     */
    public static <T> Disposable doTask(Callable<T> backgroundWork, Consumer<T> onNext) {
        return doTask(backgroundWork, onNext, Throwable::printStackTrace);
    }

    /**
     * 执行耗时任务 (带错误处理,优先使用)
     *
     * @param backgroundWork 在子线程执行
     * @param onNext         在主线程接收结果
     * @param onError        在主线程处理错误
     */
    public static <T> Disposable doTask(Callable<T> backgroundWork, Consumer<T> onNext, Consumer<Throwable> onError) {
        return Observable.fromCallable(backgroundWork)
                .subscribeOn(Schedulers.io())           // 1. 子线程执行 backgroundWork
                .observeOn(AndroidSchedulers.mainThread()) // 2. 切换回主线程
                .subscribe(onNext, onError);            // 3. 执行 UI 逻辑或错误处理
    }

    /**
     * 执行耗时任务 (不需要返回值)
     *
     * @param backgroundAction 在子线程运行的代码
     * @param mainAction       回到主线程运行的代码
     */
    public static Disposable run(Action backgroundAction, Action mainAction) {
        return Observable.create(emitter -> {
                    try {
                        backgroundAction.run();
                        emitter.onNext(1); // 发送一个空事件触发切换
                        emitter.onComplete();
                    } catch (Exception e) {
                        emitter.onError(e);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> mainAction.run(), Throwable::printStackTrace);
    }

    // ==================== 5. 切换到主线程执行任务 ====================

    /**
     * 切换到主线程执行任务
     *
     * @param action 逻辑内容
     * @return Disposable
     */
    public static Disposable runOnUiThread(Action action) {
        return Observable.empty()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(action)
                .subscribe();
    }
}