package com.reedelk.ftp.internal.exception;

import com.reedelk.runtime.api.exception.PlatformException;

public class FTPUploadException extends PlatformException {

    public FTPUploadException(String message) {
        super(message);
    }
}
