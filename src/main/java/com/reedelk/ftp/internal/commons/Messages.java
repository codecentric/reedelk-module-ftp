package com.reedelk.ftp.internal.commons;

import com.reedelk.runtime.api.commons.FormattedMessage;

public class Messages {

    private Messages() {
    }

    public enum FTPDelete implements FormattedMessage {

        ERROR_GENERIC("An error occurred while executing FTP delete operation, cause=[%s]"),
        NOT_SUCCESS("The file from path=[%s] could not be successfully deleted."),
        PATH_EMPTY("The path and name of the file to delete from the remote FTP server was empty or null (DynamicValue=[%s])."),
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

    public enum FTPList implements FormattedMessage {

        ERROR_GENERIC("An error occurred while executing FTP list operation, cause=[%s]"),
        PATH_EMPTY("The path from which files will be listed from the remote FTP server was empty or null (DynamicValue=[%s]).");

        private final String message;

        FTPList(String message) {
            this.message = message;
        }

        @Override
        public String template() {
            return message;
        }
    }

    public enum FTPRetrieve implements FormattedMessage {

        ERROR_GENERIC("An error occurred while executing FTP Retrieve operation, cause=[%s]"),
        PATH_EMPTY("The path and name of the file to retrieve from the remote FTP server was empty or null (DynamicValue=[%s]).");

        private final String message;

        FTPRetrieve(String message) {
            this.message = message;
        }

        @Override
        public String template() {
            return message;
        }
    }
}
