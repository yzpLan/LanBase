package com.yzplan.lanbase.utils.log;

import com.orhanobut.logger.Logger;

public class L {
    public static void i(String message) {
        Logger.i(message);
    }

    public static void i(String tag, String message) {
        Logger.t(tag).i(message);
    }

    public static void d(String message) {
        Logger.d(message);
    }

    public static void d(String tag, String message) {
        Logger.t(tag).d(message);
    }

    public static void v(String message) {
        Logger.v(message);
    }

    public static void v(String tag, String message) {
        Logger.t(tag).v(message);
    }

    public static void e(String message) {
        Logger.e(message);
    }

    public static void e(String tag, String message) {
        Logger.t(tag).e(message);
    }

    public static void e(String message, Throwable throwable) {
        Logger.e(throwable, message);
    }

    public static void e(String tag, String message, Throwable throwable) {
        Logger.t(tag).e(throwable, message);
    }

    public static void j(String message) {
        Logger.json(message);
    }

    public static void j(String tag, String message) {
        Logger.t(tag).json(message);
    }
}
