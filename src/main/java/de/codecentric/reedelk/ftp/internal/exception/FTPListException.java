package de.codecentric.reedelk.ftp.internal.exception;

import de.codecentric.reedelk.runtime.api.exception.PlatformException;

public class FTPListException extends PlatformException {

    public FTPListException(String message) {
        super(message);
    }

    public FTPListException(String message, Throwable exception) {
        super(message, exception);
    }
}
