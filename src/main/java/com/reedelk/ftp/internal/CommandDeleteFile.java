package com.reedelk.ftp.internal;

import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;

import static com.reedelk.runtime.api.commons.Preconditions.checkNotNull;

public class CommandDeleteFile implements Command<Boolean> {

    private final String remoteFileName;

    public CommandDeleteFile(String remoteFileName) {
        checkNotNull(remoteFileName, "Remote file name");
        this.remoteFileName = remoteFileName;
    }

    @Override
    public Boolean execute(FTPClient client) throws IOException {
        return client.deleteFile(remoteFileName);
    }
}
