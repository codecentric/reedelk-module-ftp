package de.codecentric.reedelk.ftp.internal.exception;

import de.codecentric.reedelk.runtime.api.exception.PlatformException;

public class FTPRetrieveException extends PlatformException {

    public FTPRetrieveException(String message) {
        super(message);
    }

    public FTPRetrieveException(String message, Throwable exception) {
        super(message, exception);
    }
}
