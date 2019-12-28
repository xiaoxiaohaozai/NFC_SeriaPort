package com.hc.tools.lib_nfc.utils;

/**
 * author : Oliver
 * date   : 2018/9/30
 * desc   :
 */

public class ByteUtils {

    public static String bytes2String(byte[] data) {
        if (data == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            String s = Integer.toHexString(b & 0xFF).toUpperCase();
            if (s.length() == 1) {
                sb.append("0");
            }
            sb.append(s);
            sb.append(" ");
        }
        return sb.toString();
    }

    public static String bytes2String(byte[] data, int length) {
        if (data == null || length == 0 || length > data.length) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            String s = Integer.toHexString(data[i] & 0xFF).toUpperCase();
            if (s.length() == 1) {
                sb.append("0");
            }
            sb.append(s);
            sb.append(" ");
        }
        return sb.toString();
    }

    public static String bytes2AscllString(byte[] data, int length) {
        if (data == null || length == 0 || length > data.length) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(((char) data[i]));
        }
        return sb.toString();
    }


    public static String bytes2AscllString(byte[] data) {
        if (data == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            sb.append(((char) data[i]));
        }
        return sb.toString();
    }


    public static byte[] string2Hex(String msg) {
        return msg.getBytes();
    }
}
