package com.reedelk.ftp.internal;

import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;

public class CommandDeleteFile implements Command<Boolean> {

    private final String remoteFileName;

    public CommandDeleteFile(String remoteFileName) {
        this.remoteFileName = remoteFileName;
    }

    @Override
    public Boolean execute(FTPClient client) throws IOException {
        return client.deleteFile(remoteFileName);
    }
}
