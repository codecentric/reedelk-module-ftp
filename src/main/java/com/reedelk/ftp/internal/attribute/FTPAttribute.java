package com.reedelk.ftp.internal.attribute;

import com.reedelk.runtime.api.annotation.Type;
import com.reedelk.runtime.api.annotation.TypeProperty;
import com.reedelk.runtime.api.message.MessageAttributes;

@Type
@TypeProperty(name = FTPAttribute.REMOTE_PATH, type = String.class)
public class FTPAttribute extends MessageAttributes {

    static final String REMOTE_PATH =  "remotePath";

    public FTPAttribute(String remotePath) {
        put(REMOTE_PATH, remotePath);
    }
}
