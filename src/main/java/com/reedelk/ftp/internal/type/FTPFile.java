package com.reedelk.ftp.internal.type;

import com.reedelk.ftp.internal.FTPFileWithPath;
import com.reedelk.runtime.api.annotation.Type;
import com.reedelk.runtime.api.annotation.TypeProperty;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import static com.reedelk.ftp.internal.type.FTPFile.*;

@Type(displayName = "FTPFile", mapKeyType = String.class, mapValueType = Serializable.class)
@TypeProperty(name = SIZE, type = long.class)
@TypeProperty(name = TYPE, type = int.class)
@TypeProperty(name = NAME, type = String.class)
@TypeProperty(name = LINK, type = String.class)
@TypeProperty(name = USER, type = String.class)
@TypeProperty(name = PATH, type = String.class)
@TypeProperty(name = GROUP, type = String.class)
@TypeProperty(name = IS_VALID, type = boolean.class)
@TypeProperty(name = TIMESTAMP, type = long.class)
@TypeProperty(name = IS_UNKNOWN, type = boolean.class)
@TypeProperty(name = RAW_LISTING, type = String.class)
@TypeProperty(name = PROPERTY_IS_FILE, type = boolean.class)
@TypeProperty(name = IS_SYMBOLIC_LINK, type = boolean.class)
@TypeProperty(name = PROPERTY_IS_DIRECTORY, type = boolean.class)
public class FTPFile extends HashMap<String, Serializable> {

    static final String SIZE = "size";
    static final String TYPE = "type";
    static final String NAME = "name";
    static final String LINK = "link";
    static final String USER = "user";
    static final String PATH = "path";
    static final String GROUP = "group";
    static final String IS_VALID = "isValid";
    static final String TIMESTAMP = "timestamp";
    static final String IS_UNKNOWN = "isUnknown";
    static final String RAW_LISTING = "rawListing";
    static final String PROPERTY_IS_FILE = "isFile";
    static final String IS_SYMBOLIC_LINK = "isSymbolicLink";
    static final String PROPERTY_IS_DIRECTORY = "isDirectory";

    public FTPFile(FTPFileWithPath fileWithPath) {
        org.apache.commons.net.ftp.FTPFile file = fileWithPath.file;

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

        put("timestamp", timestamp);
        put("size", size);
        put("group", group);
        put("type", type);
        put("name", name);
        put("rawListing", rawListing);
        put("link", link);
        put("user", user);
        put("isSymbolicLink", symbolicLink);
        put("isUnknown", unknown);
        put("isValid", valid);
        put("path", fileWithPath.path);

        put(PROPERTY_IS_FILE, isFile);
        put(PROPERTY_IS_DIRECTORY, directory);
    }

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


