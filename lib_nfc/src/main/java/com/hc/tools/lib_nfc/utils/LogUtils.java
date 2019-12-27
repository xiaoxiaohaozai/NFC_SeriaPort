package com.hc.tools.lib_nfc.utils;

import android.text.TextUtils;
import android.util.Log;

/**
 * author : Oliver
 * date   : 2019/8/12
 * desc   :
 */

public class LogUtils {

    private static final String TAG = "LogUtils";

    public static boolean ENABLE = true;

    public static void enable(boolean enable){
        ENABLE =enable;
    }

    public static void d(String message) {
        d(TAG, message);
    }

    public static void d(String tag, String message) {
        if (!ENABLE) {
            return;
        }
        if (TextUtils.isEmpty(tag)) {
            tag = TAG;
        }
        if (TextUtils.isEmpty(message)) {
            e(tag, message);
            return;
        }
        Log.d(tag, message);
    }

    public static void i(String message) {
        i(TAG, message);
    }

    public static void i(String tag, String message) {
        if (!ENABLE) {
            return;
        }
        if (TextUtils.isEmpty(tag)) {
            tag = TAG;
        }
        if (TextUtils.isEmpty(message)) {
            e(tag, message);
            return;
        }
        Log.i(tag, message);
    }

    public static void w(String tag, String message) {
        Log.w(tag, message);
    }

    public static void e(String message) {
        e(TAG, message);
    }

    public static void e(String tag, String message) {
        if (!ENABLE) {
            return;
        }
        if (TextUtils.isEmpty(tag)) {
            tag = TAG;
        }
        if (TextUtils.isEmpty(message)) {
            message = "请传递有效的打印信息";
        }
        Log.e(tag, message);
    }
}
