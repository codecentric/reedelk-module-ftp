package com.reedelk.ftp.internal;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;

import java.io.IOException;

public class CommandList implements Command<FTPFile[]> {

    // TODO: Add filters and so on...
    private final String path;
    private final Boolean recursive;
    private final Boolean filesOnly;
    private final Boolean directoriesOnly;

    public CommandList(String path, Boolean recursive, Boolean filesOnly, Boolean directoriesOnly) {
        this.path = path;
        this.recursive = recursive;
        this.filesOnly = filesOnly;
        this.directoriesOnly = directoriesOnly;
    }

    @Override
    public FTPFile[] execute(FTPClient client) throws Exception {
        return client.listFiles(path, new FTPFileFilter() {
            @Override
            public boolean accept(FTPFile file) {
                return true; // // TODO: Add file filter! Name dir or not and so on..
            }
        });
    }

    void listDirectory(FTPClient client, String dirToList) throws IOException {
        listDirectory(client, dirToList, "", 0);
    }

    void listDirectory(FTPClient client, String parentDir, String currentDir, int level) throws IOException {
        String dirToList = parentDir;
        if (!currentDir.equals("")) {
            dirToList += "/" + currentDir;
        }
        FTPFile[] subFiles = client.listFiles(dirToList);
        if (subFiles != null && subFiles.length > 0) {
            for (FTPFile aFile : subFiles) {
                String currentFileName = aFile.getName();
                if (currentFileName.equals(".") || currentFileName.equals("..")) {
                    // skip parent directory and directory itself
                    continue;
                }
                for (int i = 0; i < level; i++) {
                    System.out.print("\t");
                }
                if (aFile.isDirectory()) {
                    System.out.println("[" + currentFileName + "]");
                    listDirectory(client, dirToList, currentFileName, level + 1);
                } else {
                    System.out.println(currentFileName);
                }
            }
        }
    }
}
