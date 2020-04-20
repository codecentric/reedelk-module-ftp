package com.reedelk.ftp.internal;

import org.apache.commons.net.ftp.FTPFile;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class FTPFileMapper implements Function<FTPFile, Map<String, Serializable>> {

    @Override
    public Map<String, Serializable> apply(FTPFile file) {
        int type = file.getType();

        long size = file.getSize();
        long timestamp = file.getTimestamp().toInstant().toEpochMilli();

        String name = file.getName();
        String link = file.getLink();
        String user = file.getUser();
        String group = file.getGroup();
        String rawListing = file.getRawListing();

        boolean valid = file.isValid();
        boolean unknown = file.isUnknown();
        boolean directory = file.isDirectory();
        boolean symbolicLink = file.isSymbolicLink();

        Map<String, Serializable> fileEntry = new HashMap<>();
        fileEntry.put("timestamp", timestamp);
        fileEntry.put("size", size);
        fileEntry.put("group", group);
        fileEntry.put("type", type);
        fileEntry.put("name", name);
        fileEntry.put("rawListing", rawListing);
        fileEntry.put("link", link);
        fileEntry.put("user", user);
        fileEntry.put("isDirectory", directory);
        fileEntry.put("isSymbolicLink", symbolicLink);
        fileEntry.put("isUnknown", unknown);
        fileEntry.put("isValid", valid);
        return fileEntry;
    }
}
