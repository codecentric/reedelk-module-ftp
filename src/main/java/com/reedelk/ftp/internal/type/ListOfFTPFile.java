package com.reedelk.ftp.internal.type;

import com.reedelk.runtime.api.annotation.Type;

import java.util.ArrayList;

@Type(listItemType = FTPFile.class)
public class ListOfFTPFile extends ArrayList<FTPFile> {
}
