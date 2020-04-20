package com.reedelk.ftp.internal.commons;

import com.reedelk.runtime.api.commons.StringUtils;

public class Utils {

    private Utils() {
    }

    public static final String FTP_PATH_SEPARATOR = "/";

    public static String classNameOrNull(Object object) {
        return object == null ? null : object.getClass().getName();
    }

    public static String joinPath(String parent, String path) {
        if (path == null) return parent;
        if (StringUtils.isNotBlank(parent)) {
            // The parent is /documents/
            return path.startsWith("/") ?
                    parent + path.substring(1) : parent + path;
        } else {
            return path;
        }
    }
}
