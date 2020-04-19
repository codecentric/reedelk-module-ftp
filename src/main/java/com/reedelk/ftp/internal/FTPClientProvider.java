package com.reedelk.ftp.internal;

import com.reedelk.ftp.component.ConnectionConfiguration;
import com.reedelk.runtime.api.exception.PlatformException;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;

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

    public <T> T execute(Command<T> command) {
        try {
            open();
            return command.execute(ftp);
        } catch (Exception exception) {
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
