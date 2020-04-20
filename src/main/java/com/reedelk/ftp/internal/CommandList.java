package com.reedelk.ftp.internal;

import com.reedelk.ftp.internal.commons.Default;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CommandList implements Command<List<FTPFile>> {

    private final String path;
    private final boolean recursive;
    private final boolean filesOnly;
    private final boolean directoriesOnly;

    public CommandList(String path, Boolean recursive, Boolean filesOnly, Boolean directoriesOnly) {
        this.path = path;
        this.recursive = Optional.ofNullable(recursive).orElse(Default.RECURSIVE);
        this.filesOnly = Optional.ofNullable(filesOnly).orElse(Default.FILES_ONLY);
        this.directoriesOnly = Optional.ofNullable(directoriesOnly).orElse(Default.DIRECTORIES_ONLY);
    }

    @Override
    public List<FTPFile> execute(FTPClient client) throws IOException {

        List<FTPFile> ftpFiles = recursive ?
                listDirectory(client, path) :
                Arrays.asList(client.listFiles(path));

        List<Predicate<FTPFile>> allPredicates = new ArrayList<>();
        applyPredicates(allPredicates);

        return ftpFiles
                .stream()
                .filter(allPredicates.stream().reduce(x->true, Predicate::and))
                .collect(Collectors.toList());
    }

    private void applyPredicates(List<Predicate<FTPFile>> allPredicates) {
        if (filesOnly) allPredicates.add(FILES_ONLY);
        if (directoriesOnly) allPredicates.add(DIRECTORIES_ONLY);
    }

    private static final Predicate<FTPFile> FILES_ONLY = FTPFile::isFile;

    private static final Predicate<FTPFile> DIRECTORIES_ONLY = FTPFile::isDirectory;

    List<FTPFile> listDirectory(FTPClient client, String dirToList) throws IOException {
        return listDirectory(client, dirToList, "", 0);
    }

    List<FTPFile> listDirectory(FTPClient client, String parentDir, String currentDir, int level) throws IOException {
        String dirToList = parentDir;
        if (!currentDir.equals("")) {
            dirToList += "/" + currentDir;
        }
        FTPFile[] subFiles = client.listFiles(dirToList);
        List<FTPFile> allFiles = new ArrayList<>();

        if (subFiles != null && subFiles.length > 0) {
            for (FTPFile aFile : subFiles) {
                String currentFileName = aFile.getName();
                if (currentFileName.equals(".") || currentFileName.equals("..")) {
                    // skip parent directory and directory itself
                    continue;
                }
                if (aFile.isDirectory()) {
                    List<FTPFile> ftpFiles = listDirectory(client, dirToList, currentFileName, level + 1);
                    allFiles.addAll(ftpFiles);
                } else {
                    allFiles.add(aFile);
                }
            }
        }
        return allFiles;
    }
}
