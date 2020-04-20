package com.reedelk.ftp.internal;

import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;
import java.io.OutputStream;

public class CommandRetrieve implements Command<Boolean> {

    private final String remoteFileName;
    private final OutputStream outputStream;

    public CommandRetrieve(String remoteFileName, OutputStream outputStream) {
        this.remoteFileName = remoteFileName;
        this.outputStream = outputStream;
    }

    @Override
    public Boolean execute(FTPClient client) throws IOException {
        return client.retrieveFile(remoteFileName, outputStream);
    }
}
