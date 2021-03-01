package de.codecentric.reedelk.ftp.internal.type;

import de.codecentric.reedelk.runtime.api.annotation.Type;

import java.util.ArrayList;

@Type(listItemType = FTPFile.class)
public class ListOfFTPFile extends ArrayList<FTPFile> {
}
