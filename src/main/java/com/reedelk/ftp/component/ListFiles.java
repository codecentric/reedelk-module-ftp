package com.reedelk.ftp.component;

import com.reedelk.ftp.internal.FTPClientProvider;
import com.reedelk.ftp.internal.FTPFileMapper;
import com.reedelk.runtime.api.annotation.ModuleComponent;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.flow.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.apache.commons.net.ftp.FTPFile;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.reedelk.runtime.api.commons.ConfigurationPreconditions.requireNotNull;
import static java.util.stream.Collectors.toList;

@ModuleComponent("FTP List Files")
public class ListFiles implements ProcessorSync {

    @Property("Connection Configuration")
    private ConnectionConfiguration configuration;

    private FTPClientProvider provider;

    @Override
    public void initialize() {
        requireNotNull(ListFiles.class, configuration, "Configuration");
        provider = new FTPClientProvider(configuration);
    }

    @Override
    public Message apply(FlowContext flowContext, Message message) {

        provider.open();

        FTPFile[] files = provider.list();
        List<Map<String, Serializable>> allFiles =
                Arrays.stream(files)
                        .map(new FTPFileMapper())
                        .collect(toList());

        provider.close();

        return MessageBuilder.get(ListFiles.class)
                .withJavaObject(allFiles)
                .build();
    }

    public void setConfiguration(ConnectionConfiguration configuration) {
        this.configuration = configuration;
    }
}
