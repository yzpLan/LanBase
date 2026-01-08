package com.yzplan.lanbase.utils.permission;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.permissionx.guolindev.PermissionX;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 动态权限申请助手
 * <p>
 * 基于 PermissionX 封装，统一了被拒绝后的弹窗提示逻辑。
 * </p>
 */
public class PermissionUtils {

    /**
     * 在 Activity 中申请权限
     */
    public static void request(FragmentActivity activity, PermissionCallback callback, String... permissions) {
        if (activity == null || activity.isFinishing()) return;
        startRequest(PermissionX.init(activity), callback, permissions);
    }

    /**
     * 在 Fragment 中申请权限
     */
    public static void request(Fragment fragment, PermissionCallback callback, String... permissions) {
        if (fragment == null || fragment.isDetached()) return;
        startRequest(PermissionX.init(fragment), callback, permissions);
    }

    /**
     * 申请权限组 (支持传入多个数组，自动去重合并)
     * 例如：request(activity, cb, PermissionConstants.CALENDAR, PermissionConstants.CAMERA)
     */
    public static void requestGroups(FragmentActivity activity, PermissionCallback callback, String[]... groups) {
        request(activity, callback, mergePermissions(groups));
    }

    // ==================== 核心逻辑 ====================

    private static void startRequest(com.permissionx.guolindev.PermissionMediator mediator,
                                     PermissionCallback callback,
                                     String... permissions) {
        if (permissions == null || permissions.length == 0) return;

        mediator.permissions(permissions)
                // 1. (可选) 当用户拒绝过一次后，再次申请时显示的解释弹窗
                .onExplainRequestReason((scope, deniedList) ->
                        scope.showRequestReasonDialog(deniedList, "为了保证功能正常使用，请允许以下权限", "确定", "取消"))

                // 2. 当用户选择了"拒绝且不再询问"后，引导去设置页的弹窗
                .onForwardToSettings((scope, deniedList) ->
                        scope.showForwardToSettingsDialog(deniedList, "您需要去设置中手动开启权限才能使用此功能", "去设置", "取消"))

                // 3. 发起请求
                .request((allGranted, grantedList, deniedList) -> {
                    if (callback == null) return;
                    if (allGranted) {
                        callback.onGranted();
                    } else {
                        callback.onDenied(deniedList);
                    }
                });
    }

    /**
     * 工具方法：合并多个权限组数组
     */
    private static String[] mergePermissions(String[]... groups) {
        Set<String> permissionSet = new LinkedHashSet<>();
        for (String[] group : groups) {
            if (group != null) {
                permissionSet.addAll(Arrays.asList(group));
            }
        }
        return permissionSet.toArray(new String[0]);
    }

    // ==================== 回调接口 ====================

    public interface PermissionCallback {
        /**
         * 所有权限都已通过
         */
        void onGranted();

        /**
         * 至少有一个权限被拒绝
         *
         * @param deniedList 被拒绝的权限列表
         */
        void onDenied(List<String> deniedList);
    }
}