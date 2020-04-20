package com.reedelk.ftp.internal.commons;

import com.reedelk.ftp.internal.ExceptionMapper;
import com.reedelk.ftp.internal.exception.FTPDeleteException;
import com.reedelk.runtime.api.commons.StringUtils;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.content.StringContent;
import com.reedelk.runtime.api.message.content.TypedContent;

import static com.reedelk.ftp.internal.commons.Messages.Commons.TYPE_NOT_SUPPORTED;

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

    public static String pathFromPayloadOrThrow(Message message, ExceptionMapper exceptionMapper) {
        TypedContent<?, ?> content = message.content();
        if (content instanceof StringContent) {
            return ((StringContent) content).data();
        } else {
            String error = TYPE_NOT_SUPPORTED.format(classNameOrNull(content));
            throw exceptionMapper.from(error);
        }
    }
}
