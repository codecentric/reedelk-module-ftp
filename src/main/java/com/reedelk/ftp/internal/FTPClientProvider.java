package com.reedelk.ftp.internal;

import com.reedelk.ftp.component.ConnectionConfiguration;
import com.reedelk.runtime.api.exception.PlatformException;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FTPClientProvider {

    private final int port;
    private final String host;
    private final String username;
    private final String password;

    private FTPClient ftp;

    // TODO: ARgument checking
    public FTPClientProvider(ConnectionConfiguration configuration) {
        port = configuration.getPort();
        host = configuration.getHost();
        username = configuration.getUsername();
        password = configuration.getPassword();
    }

    public void open() {
        ftp = new FTPClient(); // Maybe init only once?
        try {
            ftp.connect(host, port);
            int reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                throw new PlatformException("Could not complete connection");
            }
            ftp.login(username, password);
        } catch (IOException exception) {
            throw new PlatformException(exception);
        }
    }

    // List with path
    public FTPFile[] list(String path) {
        try {
            return ftp.listFiles(path);
        } catch (IOException exception) {
            throw new PlatformException(exception);
        }
    }

    // List
    public FTPFile[] list() {
        try {
            return ftp.listFiles();
        } catch (IOException exception) {
            throw new PlatformException(exception);
        }
    }

    // Upload
    public boolean upload(String remoteFileName, InputStream inputStream) {
        try {
            return ftp.storeFile(remoteFileName, inputStream);
        } catch (IOException exception) {
            throw new PlatformException(exception);
        }
    }

    // Download
    public boolean download(String remoteFileName, OutputStream outputStream) {
        try {
            return ftp.retrieveFile(remoteFileName, outputStream);
        } catch (IOException exception) {
            throw new PlatformException(exception);
        }
    }

    public void close() {
        try {
            ftp.disconnect();
        } catch (IOException exception) {
            // Nothing we can do?
            // TODO: Log here!
        }
    }
}
