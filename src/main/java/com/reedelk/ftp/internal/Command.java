package com.reedelk.ftp.internal;

import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;

public interface Command<T> {

    T execute(FTPClient client) throws IOException;

}
