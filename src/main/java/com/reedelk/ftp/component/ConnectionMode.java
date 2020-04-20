package com.reedelk.ftp.component;

import com.reedelk.runtime.api.annotation.DisplayName;
import org.apache.commons.net.ftp.FTPClient;

public enum ConnectionMode {

    @DisplayName("Passive")
    PASSIVE {
        @Override
        public void set(FTPClient ftp) {
            ftp.enterLocalPassiveMode();
        }
    },

    @DisplayName("Active")
    ACTIVE {
        @Override
        public void set(FTPClient ftp) {
            ftp.enterLocalActiveMode();
        }
    };

    public abstract void set(FTPClient ftp);
}
