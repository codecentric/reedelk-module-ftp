package com.reedelk.ftp.internal.commons;

import com.reedelk.runtime.api.commons.FormattedMessage;

public class Messages {

    private Messages() {
    }

    public enum FTPDelete implements FormattedMessage {

        NOT_SUCCESS("The file from path=[%s] could not be successfully deleted."),
        PATH_EMPTY("The path and name of the file to delete from the remote FTP server was empty or null."),
        TYPE_NOT_SUPPORTED("The component only support payload input with String type, however type=[%s] was found.");

        private String message;

        FTPDelete(String message) {
            this.message = message;
        }

        @Override
        public String template() {
            return message;
        }
    }
}
