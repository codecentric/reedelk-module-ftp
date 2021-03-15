package de.codecentric.reedelk.ftp.internal.exception;

import de.codecentric.reedelk.runtime.api.exception.PlatformException;

public class FTPDeleteException extends PlatformException {

    public FTPDeleteException(String message) {
        super(message);
    }

    public FTPDeleteException(String message, Throwable exception) {
        super(message, exception);
    }
}
