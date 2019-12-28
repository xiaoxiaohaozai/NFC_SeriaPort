package com.hc.tools.lib_nfc.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegUtils {
   // private static final String HEX_REG_EX = "^[0][x][0-9a-fA-F]+$";

    private static final String HEX_REG_EX = "^[0-9a-fA-F]+$";
    private static final Pattern sPattern = Pattern.compile(HEX_REG_EX);

    public static boolean isRegEx(String text) {
        Matcher matcher = sPattern.matcher(text);
        return matcher.matches();
    }
}
