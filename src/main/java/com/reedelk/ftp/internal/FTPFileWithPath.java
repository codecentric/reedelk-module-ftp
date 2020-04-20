package com.reedelk.ftp.internal;

import org.apache.commons.net.ftp.FTPFile;

public class FTPFileWithPath {

    final String path;
    final FTPFile file;

    private FTPFileWithPath(String path, FTPFile file) {
        this.path = path;
        this.file = file;
    }

    public static FTPFileWithPath from(String path, FTPFile file) {
        return new FTPFileWithPath(path, file);
    }
}
