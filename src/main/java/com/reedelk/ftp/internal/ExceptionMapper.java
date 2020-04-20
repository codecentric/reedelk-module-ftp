package com.reedelk.ftp.internal;

import com.reedelk.runtime.api.exception.PlatformException;

public interface ExceptionMapper {

    default PlatformException from(Exception exception) {
        throw new UnsupportedOperationException("Mapper does not support method");
    }

    PlatformException from(String error);
}
