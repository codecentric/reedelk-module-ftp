package com.reedelk.ftp.internal.commons;

public class Utils {

    private Utils() {
    }

    public static String classNameOrNull(Object object) {
        return object == null ? null : object.getClass().getName();
    }

}
