package com.yzplan.lanbase.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.yzplan.lanbase.R;


/**
 * 通用加载/状态弹窗
 */
public class CommonLoadingDialog extends DialogFragment {
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
    private boolean mIsShowing = false;
    // 超时任务
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Runnable mFailsafeRunnable = () -> {
        if (isAdded() && mCurrentStatus == Status.LOADING) {
            dismissAllowingStateLoss();
            if (mDismissCallback != null) {
                mDismissCallback.run();
            }
        }
    };

    public CommonLoadingDialog() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.CommonLoadingDialog);
        mRotateAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.lib_loading_rotate);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.lib_common_loading_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mStatusImage = view.findViewById(R.id.iv_status);
        mMessageText = view.findViewById(R.id.tv_message);
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
                    mStatusImage.setImageResource(R.drawable.lib_icon_error); // 失败图标
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
            if (isAdded() || isVisible()) {
                dismissAllowingStateLoss();
            }
            // 执行回调 (例如跳转)
            if (mDismissCallback != null) {
                mDismissCallback.run();
            }
        }, AUTO_DISMISS_DELAY_MS);
    }

    public void show(@NonNull FragmentManager manager, String tag) {
        if (mIsShowing || isAdded()) {
            return;
        }
        try {
            mIsShowing = true;
            super.show(manager, tag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dismiss() {
        mIsShowing = false;
        // 移除所有未执行的回调，防止内存泄漏
        mHandler.removeCallbacksAndMessages(null);
        super.dismiss();
    }

    @Override
    public void onDestroyView() {
        if (mStatusImage != null) {
            mStatusImage.clearAnimation();
        }
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroyView();
    }
}