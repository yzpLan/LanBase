package com.yzplan.lanbase.manager;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.yzplan.lanbase.utils.data.JsonUtil;
import com.yzplan.lanbase.utils.log.L;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息总线 (LiveData实现)
 */
public class LiveEventBus {
    private static final String TAG = "LiveEventBus";
    private final Map<String, BusLiveData<Object>> bus;

    private LiveEventBus() {
        bus = new HashMap<>();
    }

    private static class SingletonHolder {
        private static final LiveEventBus DEFAULT_BUS = new LiveEventBus();
    }

    public static LiveEventBus getInstance() {
        return SingletonHolder.DEFAULT_BUS;
    }

    public <T> BusLiveData<T> with(String key, Class<T> type) {
        if (!bus.containsKey(key)) {
            bus.put(key, new BusLiveData<>(key));
        }
        return (BusLiveData<T>) bus.get(key);
    }

    public static class BusLiveData<T> extends MutableLiveData<T> {
        private final String key;
        // 标志位：用于实现非粘性消息（可选）
        private boolean isSingleEvent = false;
        private boolean isHandled = false;

        public BusLiveData(String key) {
            this.key = key;
        }

        /**
         * 设置为非粘性模式（仅消费一次）
         */
        public BusLiveData<T> asSingleEvent() {
            this.isSingleEvent = true;
            return this;
        }

        /**
         * 主线程发送
         */
        public void set(T value) {
            String jsonValue = JsonUtil.toJson(value);
            L.i(TAG, "【发送消息】Key=" + key + "-> Value=" + jsonValue + "\n 等待页面活跃,进行消费...");
            super.setValue(value);
            // ⚠️ 执行完发送后，立即重置标志位，确保下一次发送默认恢复为粘性
            isSingleEvent = false;
            isHandled = false;
        }

        /**
         * 子线程发送
         */
        public void post(T value) {
            String jsonValue = JsonUtil.toJson(value);
            L.i(TAG, "【发送消息】Key=" + key + "-> Value=" + jsonValue + "\n 等待页面活跃,进行消费...");
            super.postValue(value);
            isSingleEvent = false;
            isHandled = false;
        }

        /**
         * 订阅消息
         */
        public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
            super.observe(owner, t -> {
                if (isSingleEvent) {
                    if (isHandled) return; // 如果是非粘性且已处理，直接拦截
                    isHandled = true;     // 标记为已处理
                }
                String jsonValue = JsonUtil.toJson(t);
                L.i(TAG, "【接收消息】Key=" + key + "-> Value=" + jsonValue + "\n 页面回归活跃，进行消费!!!");
                observer.onChanged(t);
            });
        }
    }
}