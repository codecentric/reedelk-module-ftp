package com.reedelk.ftp.internal.exception;

import com.reedelk.runtime.api.exception.PlatformException;

public class FTPDownloadException extends PlatformException {

    public FTPDownloadException(String message) {
        super(message);
    }
}
