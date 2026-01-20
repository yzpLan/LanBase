package com.yzplan.lanbase.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.yzplan.lanbase.R;

/**
 * 通用提示/确认 Dialog
 */
public class CommonAlertDialog extends Dialog {

    private TextView txtTitle, txtContent, txtTip, txtCancel, txtSure;
    private View ivLine;

    // 数据与颜色
    private String title, content, tip;
    private String leftText, rightText;
    private int leftColor = 0, rightColor = 0, tipColor = 0;
    private View.OnClickListener leftListener, rightListener;
    private boolean isSingleMode = false;

    // 倒计时
    private int countDownSeconds = 0;
    private boolean isCountDownOnRight = true;
    private CountDownTimer timer;

    public CommonAlertDialog(@NonNull Context context) {
        super(context, R.style.CommonLoadingDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lib_common_alert_dialog);
        setCancelable(false); // 默认不响应点击外部取消

        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        initView();
        refreshUI();
        initEvent();
    }

    private void initView() {
        txtTitle = findViewById(R.id.txt_title);
        txtContent = findViewById(R.id.txt_content);
        txtTip = findViewById(R.id.txt_tip);
        txtCancel = findViewById(R.id.txt_cancel);
        txtSure = findViewById(R.id.txt_sure);
        ivLine = findViewById(R.id.iv_line);
    }

    private void refreshUI() {
        setTextOrHide(txtTitle, title);
        setTextOrHide(txtContent, content);

        // Tip 文本及颜色
        setTextOrHide(txtTip, tip);
        if (tipColor != 0 && txtTip != null) txtTip.setTextColor(tipColor);

        // 按钮文本及颜色处理
        if (txtCancel != null) {
            txtCancel.setText(leftText);
            if (leftColor != 0) txtCancel.setTextColor(leftColor);
        }
        if (txtSure != null) {
            txtSure.setText(rightText);
            if (rightColor != 0) txtSure.setTextColor(rightColor);
        }

        // 模式切换逻辑优化
        if (isSingleMode) {
            if (txtCancel != null) txtCancel.setVisibility(View.GONE);
            if (ivLine != null) ivLine.setVisibility(View.GONE);
            if (txtSure != null) {
                txtSure.setVisibility(View.VISIBLE);
                // 单按钮模式下，背景设为全底部圆角
                txtSure.setBackgroundResource(R.drawable.lib_selector_white_bottom_radius6);
            }
        } else {
            if (txtCancel != null) {
                txtCancel.setVisibility(View.VISIBLE);
                txtCancel.setBackgroundResource(R.drawable.lib_selector_white_bottom_left_radius6);
            }
            if (ivLine != null) ivLine.setVisibility(View.VISIBLE);
            if (txtSure != null) {
                txtSure.setVisibility(View.VISIBLE);
                txtSure.setBackgroundResource(R.drawable.lib_selector_white_bottom_right_radius6);
            }
        }

        // 启动倒计时
        if (countDownSeconds > 0) startCountDown();
    }

    private void initEvent() {
        if (txtCancel != null) {
            txtCancel.setOnClickListener(v -> {
                if (leftListener != null) leftListener.onClick(v);
                dismiss();
            });
        }
        if (txtSure != null) {
            txtSure.setOnClickListener(v -> {
                if (rightListener != null) rightListener.onClick(v);
                dismiss();
            });
        }
    }

    private void startCountDown() {
        if (timer != null) timer.cancel();
        final TextView targetBtn = isCountDownOnRight ? txtSure : txtCancel;
        final String originalText = isCountDownOnRight ? rightText : leftText;

        timer = new CountDownTimer(countDownSeconds * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (targetBtn != null) {
                    targetBtn.setText(String.format("%s(%ds)", originalText, millisUntilFinished / 1000));
                }
            }

            @Override
            public void onFinish() {
                if (targetBtn != null && targetBtn.isShown()) {
                    targetBtn.setText(originalText);
                    targetBtn.performClick(); // 到期自动点击按钮
                }
            }
        }.start();
    }

    @Override
    public void dismiss() {
        // 安全退出：防止 Handler 定时任务导致内存泄露
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        super.dismiss();
    }

    // --- 链式 Setters 保持 ---
    public CommonAlertDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public CommonAlertDialog setContent(String content) {
        this.content = content;
        return this;
    }

    public CommonAlertDialog setTip(String tip, int color) {
        this.tip = tip;
        this.tipColor = color;
        return this;
    }

    public CommonAlertDialog setSingleMode(boolean isSingle) {
        this.isSingleMode = isSingle;
        return this;
    }

    public void setLeftBtn(String text, int color, View.OnClickListener listener) {
        this.leftText = text;
        this.leftColor = color;
        this.leftListener = listener;
    }

    public void setRightBtn(String text, int color, View.OnClickListener listener) {
        this.rightText = text;
        this.rightColor = color;
        this.rightListener = listener;
    }

    public void setCountDown(int seconds, boolean isOnRight) {
        this.countDownSeconds = seconds;
        this.isCountDownOnRight = isOnRight;
    }

    /**
     * 防错文本展示：处理 "null" 字符串和空数据隐藏
     */
    private void setTextOrHide(TextView tv, String text) {
        if (tv == null) return;
        if (TextUtils.isEmpty(text) || "null".equalsIgnoreCase(text)) {
            tv.setVisibility(View.GONE);
        } else {
            tv.setVisibility(View.VISIBLE);
            tv.setText(text);
        }
    }
}