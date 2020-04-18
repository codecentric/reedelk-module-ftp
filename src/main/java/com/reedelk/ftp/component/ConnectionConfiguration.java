package com.reedelk.ftp.component;

import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.Implementor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

@Shared
@Component(service = ConnectionConfiguration.class, scope = ServiceScope.PROTOTYPE)
public class ConnectionConfiguration implements Implementor {

    @Property("FTP Host")
    @Hint("ftp.my.domain.com")
    @Example("ftp.my.domain.com")
    @Description("The connection URL is a string that a MongoDB driver uses to connect to a database. " +
            "It can contain information such as where to search for the database, " +
            "the name of the database to connect to, and configuration properties.")
    private String host;

    @Property("FTP Port")
    private int port;

    @Property("Username")
    @Hint("myFTPUser")
    @Example("myFTPUser")
    @Description("The username to be used to create the FTP connection.")
    private String username;

    @Property("Password")
    @Hint("myFTPUser")
    @Example("myFTPUser")
    @Description("The username to be used to create the FTP connection.")
    private String password;

    @Property("Working Directory")
    @Hint("assets")
    @Example("assets")
    @Description("The path to a directory that is treated as the root of every relative path used with this connector.")
    private String workingDir;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
