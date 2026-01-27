package com.yzplan.lanbase.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.yzplan.lanbase.R;

/**
 * 通用加载/状态弹窗
 * 修改点：由 DialogFragment 改为 Dialog 方案，移除对 FragmentManager 的依赖
 */
public class CommonLoadingDialog extends Dialog {
    //状态枚举
    public enum Status {
        LOADING,
        SUCCESS,
        ERROR
    }

    // 成功/失败后的停留时间 (1秒)
    private static final long AUTO_DISMISS_DELAY_MS = 1000;
    // 加载超时时间 (120秒)，防止一直转圈无法关闭
    private static final long FAILSAFE_TIMEOUT_MS = 120000;
    private ImageView mStatusImage;
    private TextView mMessageText;
    private Animation mRotateAnimation;
    private Status mCurrentStatus = Status.LOADING;
    private String mMessage = "加载中...";
    // 消失后的回调
    private Runnable mDismissCallback;

    // 超时任务
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Runnable mFailsafeRunnable = () -> {
        if (isShowing() && mCurrentStatus == Status.LOADING) {
            dismiss();
            if (mDismissCallback != null) {
                mDismissCallback.run();
            }
        }
    };

    /**
     * 构造函数
     *
     * @param context 建议传入 Activity 上下文
     */
    public CommonLoadingDialog(@NonNull Context context) {
        // 使用自定义样式
        super(context, R.style.CommonLoadingDialog);

        // 初始化布局
        View view = LayoutInflater.from(context).inflate(R.layout.lib_common_loading_dialog, null);
        setContentView(view);

        mStatusImage = view.findViewById(R.id.iv_status);
        mMessageText = view.findViewById(R.id.tv_message);
        mRotateAnimation = AnimationUtils.loadAnimation(context, R.anim.lib_loading_rotate);

        setCancelable(false);
        updateUI();
    }

    /**
     * 设置状态 (带回调)
     *
     * @param status    状态 (LOADING, SUCCESS, ERROR)
     * @param message   提示文本
     * @param onDismiss 弹窗消失后执行的逻辑 (如跳转页面)
     */
    public void setState(Status status, String message, Runnable onDismiss) {
        this.mCurrentStatus = status;
        this.mMessage = message;
        this.mDismissCallback = onDismiss;
        if (mStatusImage != null) {
            updateUI();
        }
    }

    /**
     * 设置状态 (不带回调)
     */
    public void setState(Status status, String message) {
        setState(status, message, null);
    }

    private void updateUI() {
        // 清除之前的定时任务
        mHandler.removeCallbacksAndMessages(null);
        if (mMessageText != null) {
            mMessageText.setText(mMessage);
        }
        switch (mCurrentStatus) {
            case LOADING:
                if (mStatusImage != null) {
                    mStatusImage.setVisibility(View.VISIBLE);
                    // 加载图标
                    mStatusImage.setImageResource(R.drawable.lib_icon_processing);
                    mStatusImage.startAnimation(mRotateAnimation);
                }
                // 启动超时保护
                mHandler.postDelayed(mFailsafeRunnable, FAILSAFE_TIMEOUT_MS);
                break;
            case SUCCESS:
                stopAnimation();
                if (mStatusImage != null) {
                    // 成功图标
                    mStatusImage.setImageResource(R.drawable.lib_icon_success);
                }
                autoDismiss();
                break;
            case ERROR:
                stopAnimation();
                if (mStatusImage != null) {
                    // 失败图标
                    mStatusImage.setImageResource(R.drawable.lib_icon_error);
                }
                autoDismiss();
                break;
        }
    }

    private void stopAnimation() {
        if (mStatusImage != null) {
            mStatusImage.clearAnimation();
            mStatusImage.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 延时关闭并执行回调
     */
    private void autoDismiss() {
        mHandler.postDelayed(() -> {
            if (isShowing()) {
                dismiss();
            }
            // 执行回调 (例如跳转)
            if (mDismissCallback != null) {
                mDismissCallback.run();
            }
        }, AUTO_DISMISS_DELAY_MS);
    }

    @Override
    public void dismiss() {
        // 移除所有未执行的回调，防止内存泄漏
        mHandler.removeCallbacksAndMessages(null);
        if (mStatusImage != null) {
            mStatusImage.clearAnimation();
        }
        super.dismiss();
    }
}