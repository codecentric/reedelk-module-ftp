package de.codecentric.reedelk.ftp.internal;

import org.apache.commons.net.ftp.FTPFile;

public class FTPFileWithPath {

    public final String path;
    public final FTPFile file;

    private FTPFileWithPath(String path, FTPFile file) {
        this.path = path;
        this.file = file;
    }

    public static FTPFileWithPath from(String path, FTPFile file) {
        return new FTPFileWithPath(path, file);
    }
}
