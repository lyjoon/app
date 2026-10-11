package org.llgs.global.util;

import org.springframework.util.NumberUtils;

import java.text.NumberFormat;
import java.util.Locale;

public class NumberUtil {

    public static int absInt(String string) {
        try {
            int v = NumberUtils.parseNumber(string, Integer.class);
            return Math.abs(v);
        } catch (Throwable ignored) {
            return 0;
        }
    }

    public static int toInt(String string) {
        try {
            return NumberUtils.parseNumber(string, Integer.class);
        } catch (Throwable ignored) {
            return 0;
        }
    }

    public static long toLong(String string) {
        try {
            return NumberUtils.parseNumber(string, Long.class);
        } catch (Throwable ignored) {
            return 0L;
        }
    }

    public static String formatComma(long number) {
        return NumberFormat.getInstance(Locale.KOREA).format(number);
    }
}
