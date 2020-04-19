package com.reedelk.ftp.internal;

import org.apache.commons.net.ftp.FTPClient;

public interface Command<T> {

    T execute(FTPClient client) throws Exception;

}
