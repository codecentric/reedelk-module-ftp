package com.reedelk.ftp.component;

import com.reedelk.ftp.internal.Command;
import com.reedelk.ftp.internal.CommandDeleteFile;
import com.reedelk.ftp.internal.FTPClientProvider;
import com.reedelk.ftp.internal.exception.FTPDeleteException;
import com.reedelk.runtime.api.annotation.Description;
import com.reedelk.runtime.api.annotation.ModuleComponent;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.flow.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.content.StringContent;
import com.reedelk.runtime.api.message.content.TypedContent;
import com.reedelk.runtime.api.script.ScriptEngineService;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.List;

import static com.reedelk.runtime.api.commons.ConfigurationPreconditions.requireNotNull;

@ModuleComponent("FTP Delete")
@Description("The FTP delete component allows to remove one or many files from a remote FTP server.")
public class FTPDelete implements ProcessorSync {

    @Property("Connection")
    @Description("FTP connection configuration to be used by this delete operation.")
    private ConnectionConfiguration configuration;

    @Property("File name")
    private DynamicString fileName;

    private FTPClientProvider provider;

    @Reference
    ScriptEngineService scriptEngine;

    @Override
    public void initialize() {
        requireNotNull(FTPList.class, configuration, "Configuration");
        provider = new FTPClientProvider(configuration);
    }

    @Override
    public Message apply(FlowContext flowContext, Message message) {

        TypedContent<?, ?> content = message.content();

        Command<Boolean> command;
        if (content instanceof StringContent) {
            String remoteFileName = ((StringContent) content).data();
            command = new CommandDeleteFile(remoteFileName);
        } else {
            throw new FTPDeleteException("Type not supported");
        }

        boolean success = provider.execute(command);
        if (!success)  {
            throw new FTPDeleteException("Error could not be downloaded");
        }

        return MessageBuilder.get(FTPDelete.class)
                .withJavaObject(success)
                .build();
    }

    public void setConfiguration(ConnectionConfiguration configuration) {
        this.configuration = configuration;
    }
}
