package com.reedelk.ftp.internal;

import org.apache.commons.net.ftp.FTPClient;

public class CommandDelete implements Command<Boolean> {

    private final String remoteFileName;

    public CommandDelete(String remoteFileName) {
        this.remoteFileName = remoteFileName;
    }

    @Override
    public Boolean execute(FTPClient client) throws Exception {
        return client.deleteFile(remoteFileName);
    }
}
