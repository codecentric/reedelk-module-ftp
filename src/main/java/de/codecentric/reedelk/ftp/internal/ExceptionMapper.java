package de.codecentric.reedelk.ftp.internal;

import de.codecentric.reedelk.runtime.api.exception.PlatformException;

public interface ExceptionMapper {

    default PlatformException from(Exception exception) {
        throw new UnsupportedOperationException("Mapper does not support method");
    }

    PlatformException from(String error);
}
