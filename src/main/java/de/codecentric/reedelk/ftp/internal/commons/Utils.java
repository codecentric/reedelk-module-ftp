package de.codecentric.reedelk.ftp.internal.commons;

import de.codecentric.reedelk.ftp.internal.ExceptionMapper;
import de.codecentric.reedelk.runtime.api.commons.StringUtils;
import de.codecentric.reedelk.runtime.api.message.Message;
import de.codecentric.reedelk.runtime.api.message.content.StringContent;
import de.codecentric.reedelk.runtime.api.message.content.TypedContent;

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
            String error = Messages.Commons.TYPE_NOT_SUPPORTED.format(classNameOrNull(content));
            throw exceptionMapper.from(error);
        }
    }
}
