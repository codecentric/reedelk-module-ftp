package de.codecentric.reedelk.ftp.internal.commons;

import de.codecentric.reedelk.ftp.component.ConnectionMode;

public class Default {

    private Default() {
    }

    public static final ConnectionMode FTP_DEFAULT_MODE = ConnectionMode.PASSIVE;
    public static final int FTP_PORT = 21;
    public static final boolean RECURSIVE = false;
    public static final boolean FILES_ONLY = false;
    public static final boolean DIRECTORIES_ONLY = false;
}
