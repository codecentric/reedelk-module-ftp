package de.codecentric.reedelk.ftp.internal;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;
import java.io.InputStream;

public class CommandStore implements Command<Boolean> {

    private final String remoteFilePath;
    private final InputStream inputStream;

    public CommandStore(String remoteFilePath, InputStream inputStream) {
        this.remoteFilePath = remoteFilePath;
        this.inputStream = inputStream;
    }

    @Override
    public Boolean execute(FTPClient client) throws IOException {
        client.setFileType(FTP.BINARY_FILE_TYPE);
        return client.storeFile(remoteFilePath, inputStream);
    }
}
