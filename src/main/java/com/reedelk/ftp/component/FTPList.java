package com.reedelk.ftp.component;

import com.reedelk.ftp.internal.CommandList;
import com.reedelk.ftp.internal.FTPClientProvider;
import com.reedelk.ftp.internal.FTPFileMapper;
import com.reedelk.runtime.api.annotation.ModuleComponent;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.flow.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.apache.commons.net.ftp.FTPFile;

import java.util.List;
import java.util.Map;

import static com.reedelk.runtime.api.commons.ConfigurationPreconditions.requireNotNull;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

@ModuleComponent("FTP List Files")
public class FTPList implements ProcessorSync {

    @Property("Connection Configuration")
    private ConnectionConfiguration configuration;

    @Property("Recursive")
    private Boolean recursive;

    @Property("Files only")
    private Boolean filesOnly;

    @Property("Directories only")
    private Boolean directoriesOnly;

    private FTPClientProvider provider;

    @Override
    public void initialize() {
        requireNotNull(FTPList.class, configuration, "Configuration");
        provider = new FTPClientProvider(configuration);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Message apply(FlowContext flowContext, Message message) {

        String workingDir = configuration.getWorkingDir();

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

    public void setConfiguration(ConnectionConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setDirectoriesOnly(Boolean directoriesOnly) {
        this.directoriesOnly = directoriesOnly;
    }

    public void setFilesOnly(Boolean filesOnly) {
        this.filesOnly = filesOnly;
    }

    public void setRecursive(Boolean recursive) {
        this.recursive = recursive;
    }
}
