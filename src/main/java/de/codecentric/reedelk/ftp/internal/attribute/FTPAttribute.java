package de.codecentric.reedelk.ftp.internal.attribute;

import de.codecentric.reedelk.runtime.api.annotation.Type;
import de.codecentric.reedelk.runtime.api.annotation.TypeProperty;
import de.codecentric.reedelk.runtime.api.message.MessageAttributes;

@Type
@TypeProperty(name = FTPAttribute.REMOTE_PATH, type = String.class)
public class FTPAttribute extends MessageAttributes {

    static final String REMOTE_PATH =  "remotePath";

    public FTPAttribute(String remotePath) {
        put(REMOTE_PATH, remotePath);
    }
}
