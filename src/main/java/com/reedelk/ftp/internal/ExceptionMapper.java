package com.reedelk.ftp.internal;

import com.reedelk.runtime.api.exception.PlatformException;

public interface ExceptionMapper {

    PlatformException from(Exception exception);

    PlatformException from(String error);
}
