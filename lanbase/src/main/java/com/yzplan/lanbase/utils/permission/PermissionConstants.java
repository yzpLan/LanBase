package com.yzplan.lanbase.utils.permission;

import android.Manifest;

/**
 * App 权限常量类
 */
public class PermissionConstants {

    /**
     * 文件读写权限
     */
    public static final String[] GROUP_STORAGE = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * 相机权限
     */
    public static final String[] GROUP_CAMERA = new String[]{
            Manifest.permission.CAMERA
    };

    /**
     * 精准定位权限
     */
    public static final String[] GROUP_LOCATION = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    /**
     * 设备信息权限 (读取手机状态)
     */
    public static final String[] GROUP_PHONE_STATE = new String[]{
            Manifest.permission.READ_PHONE_STATE
    };

    /**
     * 联系人权限组 (读取/写入)
     */
    public static final String[] GROUP_CONTACTS = new String[]{
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS
    };
}
