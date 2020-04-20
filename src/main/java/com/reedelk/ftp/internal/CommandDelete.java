package com.reedelk.ftp.internal;

import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;
import java.util.List;

import static com.reedelk.runtime.api.commons.Preconditions.checkState;

public class CommandDelete implements Command<Boolean> {

    private final String remoteFileName;
    private final List<String> remoteFiles;

    public CommandDelete(String remoteFileName) {
        this.remoteFileName = remoteFileName;
        this.remoteFiles = null;
    }

    public CommandDelete(List<String> remoteFiles) {
        checkState(remoteFiles != null, "Remote files list");
        this.remoteFiles = remoteFiles;
        this.remoteFileName = null;
    }

    @Override
    public Boolean execute(FTPClient client) throws Exception {
        if (remoteFileName != null) {
            return client.deleteFile(remoteFileName);
        } else {
            Result result = new Result();
            remoteFiles.forEach(remoteFileName -> {
                try {
                    result.successful = client.deleteFile(remoteFileName);
                } catch (IOException e) {
                    result.successful = false;
                }
            });
            return result.successful;
        }
    }

    static class Result {
        boolean successful = true;
    }
}
