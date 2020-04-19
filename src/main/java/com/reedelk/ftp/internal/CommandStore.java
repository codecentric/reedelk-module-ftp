package com.reedelk.ftp.internal;

import org.apache.commons.net.ftp.FTPClient;

import java.io.InputStream;

public class CommandStore implements Command<Boolean> {

    private final String remoteFileName;
    private final InputStream inputStream;

    public CommandStore(String remoteFileName, InputStream inputStream) {
        this.remoteFileName = remoteFileName;
        this.inputStream = inputStream;
    }

    @Override
    public Boolean execute(FTPClient client) throws Exception {
        return client.storeFile(remoteFileName, inputStream);
    }
}
