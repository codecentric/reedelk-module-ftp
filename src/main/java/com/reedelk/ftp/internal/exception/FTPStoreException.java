package com.reedelk.ftp.internal.exception;

import com.reedelk.runtime.api.exception.PlatformException;

public class FTPStoreException extends PlatformException {

    public FTPStoreException(String message) {
        super(message);
    }

    public FTPStoreException(String message, Throwable exception) {
        super(message, exception);
    }
}
