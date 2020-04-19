package com.reedelk.ftp.internal;

import com.reedelk.ftp.component.ConnectionConfiguration;
import com.reedelk.runtime.api.commons.ConfigurationPreconditions;
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

    // TODO: Argument Checking
    public FTPClientProvider(ConnectionConfiguration configuration) {
        port = configuration.getPort();
        host = configuration.getHost();
        username = configuration.getUsername();
        password = configuration.getPassword();
        ftp = new FTPClient(); // Maybe init only once?
    }

    // List with path
    public FTPFile[] list(String path) {
        try {
            open();
            return ftp.listFiles(path);
        } catch (IOException exception) {
            throw new PlatformException(exception);
        } finally {
            close();
        }
    }

    // List
    public FTPFile[] list() {
        try {
            open();
            return ftp.listFiles();
        } catch (IOException exception) {
            throw new PlatformException(exception);
        } finally {
            close();
        }
    }

    // Upload
    public boolean upload(String remoteFileName, InputStream inputStream) {
        try {
            open();
            return ftp.storeFile(remoteFileName, inputStream);
        } catch (IOException exception) {
            throw new PlatformException(exception);
        } finally {
            close();
        }
    }

    // Download
    public boolean download(String remoteFileName, OutputStream outputStream) {
        try {
            open();
            return ftp.retrieveFile(remoteFileName, outputStream);
        } catch (IOException exception) {
            throw new PlatformException(exception);
        } finally {
            close();
        }
    }

    private void close() {
        try {
            ftp.logout();
        } catch (IOException e) {
            // Log Here ?
        }
        try {
            ftp.disconnect();
        } catch (IOException exception) {
            // Nothing we can do?
            // TODO: Log here!
        }
    }

    private void open() {
        try {
            ftp.connect(host, port);
            int reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                throw new PlatformException("Could not complete connection");
            }
            boolean login = ftp.login(username, password);
            if (!login) {
                throw new PlatformException("Could not login! Username and password wrong?");
            }
        } catch (IOException exception) {
            throw new PlatformException(exception);
        }
    }
}
