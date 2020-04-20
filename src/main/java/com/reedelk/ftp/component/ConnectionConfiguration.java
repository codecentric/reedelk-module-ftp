package com.reedelk.ftp.component;

import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.commons.StringUtils;
import com.reedelk.runtime.api.component.Implementor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

import java.util.Optional;

import static com.reedelk.ftp.internal.commons.Utils.FTP_PATH_SEPARATOR;

@Shared
@Component(service = ConnectionConfiguration.class, scope = ServiceScope.PROTOTYPE)
public class ConnectionConfiguration implements Implementor {

    @Property("Connection type")
    @Example("FTP")
    @DefaultValue("FTPS")
    @Description("Sets the type of connection to be established with the remote server. " +
            "FTPs uses TLS, FTP is plain connection.")
    private ConnectionType type;

    @Property("Connection mode")
    @Example("ACTIVE")
    @DefaultValue("PASSIVE")
    @Description("Sets the connection mode to be used for the FTP connection (Active or Passive). " +
            "If you are behind firewall use Passive which is the default connection mode.")
    private ConnectionMode mode;

    @Property("FTP Host")
    @Hint("ftp.my.domain.com")
    @Example("ftp.my.domain.com")
    @Description("Sets the connection host the FTP client should connect to.")
    private String host;

    @Property("FTP Port")
    @Hint("21")
    @Example("21")
    @Description("Sets the connection port the FTP client should use for the connection.")
    private Integer port;

    @Property("Username")
    @Hint("myFTPUser")
    @Example("myFTPUser")
    @Description("The username to be used to create the FTP connection.")
    private String username;

    @Property("Password")
    @Password
    @Hint("myFTPUser")
    @Example("myFTPUser")
    @Description("The username to be used to create the FTP connection.")
    private String password;

    @Property("Working Directory")
    @Hint("/documents")
    @Example("/documents")
    @Description("The path to a directory that is treated as the root of every relative path used with this connector.")
    private String workingDir;

    public ConnectionType getType() {
        return type;
    }

    public void setType(ConnectionType type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
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

    public String getWorkingDir() {
        return Optional.ofNullable(workingDir)
                .map(workingDirectory -> workingDirectory + FTP_PATH_SEPARATOR)
                .orElse(StringUtils.EMPTY);
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public ConnectionMode getMode() {
        return mode;
    }

    public void setMode(ConnectionMode mode) {
        this.mode = mode;
    }
}
