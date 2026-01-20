package com.yzplan.lanbase.manager;

import android.content.Context;
import android.view.View;

import com.yzplan.lanbase.utils.data.StringUtils;
import com.yzplan.lanbase.view.CommonAlertDialog;

/**
 * 通用对话框构建工具类
 * 逻辑层级：[双按钮标准] -> [双按钮带颜色] -> [单按钮] -> [倒计时系列] -> [全自定义]
 */
public class AlertDialogHelper {

    // ==========================================
    // 1. [双按钮] 标准询问系列
    // ==========================================

    /**
     * 默认询问：取消/确定 (默认颜色)
     */
    public static void showConfirm(Context context, String title, String content, View.OnClickListener onConfirm) {
        showBuilder(context, title, content, null, 0, "取消", 0, "确定", 0, null, onConfirm, false, 0, true);
    }

    /**
     * 警告询问：取消/确定 (右侧按钮变红)
     */
    public static void showConfirm(Context context, String title, String content, int redColor, View.OnClickListener onConfirm) {
        showBuilder(context, title, content, null, 0, "取消", 0, "确定", redColor, null, onConfirm, false, 0, true);
    }

    // ==========================================
    // 2. [单按钮] 提示系列
    // ==========================================

    /**
     * 单按钮：知道了 (默认颜色)
     */
    public static void showSingleAlert(Context context, String title, String content, String btnText, View.OnClickListener onConfirm) {
        showBuilder(context, title, content, null, 0, null, 0, btnText, 0, null, onConfirm, true, 0, true);
    }

    /**
     * 单按钮：带颜色
     */
    public static void showSingleAlert(Context context, String title, String content, String btnText, int btnColor, View.OnClickListener onConfirm) {
        showBuilder(context, title, content, null, 0, null, 0, btnText, btnColor, null, onConfirm, true, 0, true);
    }

    /**
     * 单按钮：倒计时自动消失/确认
     */
    public static void showSingleCountDown(Context context, String title, String content, String btnText, int btnColor, int seconds, View.OnClickListener onConfirm) {
        showBuilder(context, title, content, null, 0, null, 0, btnText, btnColor, null, onConfirm, true, seconds, true);
    }

    // ==========================================
    // 3. [倒计时] 左右联动系列
    // ==========================================

    /**
     * 倒计时在[右侧]：常用于“确定”按钮倒计时，到期自动执行确认
     */
    public static void showCountDownConfirm(Context context, String title, String content, int seconds, View.OnClickListener onConfirm) {
        showBuilder(context, title, content, null, 0, "取消", 0, "确定", 0, null, onConfirm, false, seconds, true);
    }

    /**
     * 倒计时在[左侧]：常用于“取消”按钮倒计时，到期自动关闭
     */
    public static void showCountDownCancel(Context context, String title, String content, int seconds, View.OnClickListener onConfirm) {
        showBuilder(context, title, content, null, 0, "取消", 0, "确定", 0, null, onConfirm, false, seconds, false);
    }

    // ==========================================
    // 4. [全场景] 完全自定义组合
    // ==========================================

    /**
     * 完全自定义：包含 Tip、按钮文本、按钮颜色 (不带倒计时)
     */
    public static void showCustom(Context context, String title, String content, String tip, int tipColor,
                                  String leftText, int leftColor, String rightText, int rightColor,
                                  View.OnClickListener onCancel, View.OnClickListener onConfirm) {
        showBuilder(context, title, content, tip, tipColor, leftText, leftColor, rightText, rightColor, onCancel, onConfirm, false, 0, true);
    }

    /**
     * 完全自定义：带倒计时的终极版本
     */
    public static void showFullCustom(Context context, String title, String content, String tip, int tipColor,
                                      String leftText, int leftColor, String rightText, int rightColor,
                                      int seconds, boolean isRightCountDown,
                                      View.OnClickListener onCancel, View.OnClickListener onConfirm) {
        showBuilder(context, title, content, tip, tipColor, leftText, leftColor, rightText, rightColor, onCancel, onConfirm, false, seconds, isRightCountDown);
    }

    // ==========================================
    // 5. 核心构建 Builder (修复文字覆盖逻辑)
    // ==========================================

    private static void showBuilder(Context context, String title, String content, String tip, int tipColor,
                                    String leftText, int leftColor, String rightText, int rightColor,
                                    View.OnClickListener leftListener, View.OnClickListener rightListener,
                                    boolean isSingleMode, int countDownSeconds, boolean isCountDownOnRight) {
        if (context == null) return;
        CommonAlertDialog dialog = new CommonAlertDialog(context);
        dialog.setTitle(title).setContent(content).setTip(tip, tipColor)
                .setSingleMode(isSingleMode).setCountDown(countDownSeconds, isCountDownOnRight);

        // 核心修正：
        // 1. 如果是单按钮模式，左侧文本不设置
        // 2. 如果右侧/左侧传入了文字，则优先使用传入值；否则才给“取消/确定”的兜底文案
        if (!isSingleMode) {
            String lText = StringUtils.isNullOrEmpty(leftText) ? "取消" : leftText;
            dialog.setLeftBtn(lText, leftColor, leftListener);
        }

        String rText = StringUtils.isNullOrEmpty(rightText) ? "确定" : rightText;
        dialog.setRightBtn(rText, rightColor, rightListener);

        try {
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}