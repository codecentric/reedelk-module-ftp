package com.reedelk.ftp.internal;

import com.reedelk.ftp.component.ConnectionConfiguration;
import com.reedelk.ftp.internal.commons.Default;
import com.reedelk.runtime.api.component.Implementor;
import com.reedelk.runtime.api.exception.PlatformException;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;

import static com.reedelk.runtime.api.commons.ConfigurationPreconditions.requireNotBlank;
import static com.reedelk.runtime.api.commons.ConfigurationPreconditions.requireNotNull;
import static java.util.Optional.ofNullable;

public class FTPClientProvider {

    private final int port;
    private final String host;
    private final String username;
    private final String password;

    private FTPClient ftp;

    public FTPClientProvider(Class<? extends Implementor> implementor, ConnectionConfiguration connection) {
        requireNotNull(implementor, connection, "FTP Connection Configuration must be provided.");
        requireNotBlank(implementor, connection.getHost(), "FTP Connection host must not be empty.");
        requireNotBlank(implementor, connection.getUsername(), "FTP Connection username must not be empty.");
        requireNotBlank(implementor, connection.getPassword(), "FTP Connection password must not be empty.");

        port = ofNullable(connection.getPort()).orElse(Default.FTP_PORT);
        host = connection.getHost();
        username = connection.getUsername();
        password = connection.getPassword();
        ftp = new FTPClient();
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
