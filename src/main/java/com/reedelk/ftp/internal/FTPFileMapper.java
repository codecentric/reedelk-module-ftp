package com.reedelk.ftp.internal;

import com.reedelk.runtime.api.commons.StringUtils;
import org.apache.commons.net.ftp.FTPFile;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class FTPFileMapper implements Function<FTPFileWithPath, Map<String, Serializable>> {

    @Override
    public Map<String, Serializable> apply(FTPFileWithPath fileWithPath) {

        FTPFile file = fileWithPath.file;

        int type = file.getType();

        long size = file.getSize();
        long timestamp = file.getTimestamp().toInstant().toEpochMilli();

        String name = file.getName();
        String link = file.getLink();
        String user = file.getUser();
        String group = file.getGroup();
        String rawListing = file.getRawListing();

        boolean isFile = file.isFile();
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
        fileEntry.put("isSymbolicLink", symbolicLink);
        fileEntry.put("isUnknown", unknown);
        fileEntry.put("isValid", valid);
        fileEntry.put("path", fileWithPath.path);

        fileEntry.put(PROPERTY_IS_FILE, isFile);
        fileEntry.put(PROPERTY_IS_DIRECTORY, directory);

        return fileEntry;
    }

    private static final String PROPERTY_IS_FILE = "isFile";
    private static final String PROPERTY_IS_DIRECTORY = "isDirectory";

    public static final Predicate<Map<String, Serializable>> FILES_ONLY =
            map -> (boolean) map.get(PROPERTY_IS_FILE);

    public static final Predicate<Map<String, Serializable>> DIRECTORIES_ONLY =
            map -> (boolean) map.get(PROPERTY_IS_DIRECTORY);

    // Exclude current and parent directories.
    public static final Predicate<Map<String, Serializable>> CURRENT_DIRECTORY =
            map -> !".".equals(map.get("name"));

    public static final Predicate<Map<String, Serializable>> PARENT_DIRECTORY =
            map -> !"..".equals(map.get("name"));
}
