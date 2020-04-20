package com.reedelk.ftp.component;

import com.reedelk.ftp.internal.CommandList;
import com.reedelk.ftp.internal.FTPClientProvider;
import com.reedelk.ftp.internal.FTPFileMapper;
import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.flow.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.apache.commons.net.ftp.FTPFile;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@ModuleComponent("FTP List Files")
public class FTPList implements ProcessorSync {

    @Property("Connection")
    @Description("FTP connection configuration to be used to list files from.")
    private ConnectionConfiguration connection;

    @Property("Recursive")
    @Example("true")
    @DefaultValue("false")
    @Description("If true files are listed recursively starting from the working directory taken from the connection configuration.")
    private Boolean recursive;

    @Property("Files only")
    @Example("true")
    @DefaultValue("false")
    @Description("If true only files are listed from the working directory taken from the connection configuration.")
    private Boolean filesOnly;

    @Property("Directories only")
    @Example("true")
    @DefaultValue("false")
    @Description("If true only directories are listed from the working directory taken from the connection configuration.")
    private Boolean directoriesOnly;

    private FTPClientProvider provider;

    @Override
    public void initialize() {
        provider = new FTPClientProvider(FTPList.class, connection);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Message apply(FlowContext flowContext, Message message) {

        String workingDir = connection.getWorkingDir();

        CommandList commandList = new CommandList(workingDir, recursive, filesOnly, directoriesOnly);

        List<FTPFile> files = provider.execute(commandList);

        List allFiles = files
                .stream()
                .map(new FTPFileMapper())
                .collect(toList());

        return MessageBuilder.get(FTPList.class)
                .withList(allFiles, Map.class)
                .build();
    }

    public void setConnection(ConnectionConfiguration connection) {
        this.connection = connection;
    }

    public void setRecursive(Boolean recursive) {
        this.recursive = recursive;
    }

    public void setFilesOnly(Boolean filesOnly) {
        this.filesOnly = filesOnly;
    }

    public void setDirectoriesOnly(Boolean directoriesOnly) {
        this.directoriesOnly = directoriesOnly;
    }
}
