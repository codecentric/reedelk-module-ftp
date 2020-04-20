package com.reedelk.ftp.component;

import com.reedelk.ftp.internal.Command;
import com.reedelk.ftp.internal.CommandDeleteFile;
import com.reedelk.ftp.internal.FTPClientProvider;
import com.reedelk.ftp.internal.exception.FTPDeleteException;
import com.reedelk.ftp.internal.exception.FTPUploadException;
import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.flow.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.content.StringContent;
import com.reedelk.runtime.api.message.content.TypedContent;
import com.reedelk.runtime.api.script.ScriptEngineService;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import org.osgi.service.component.annotations.Reference;

import static com.reedelk.runtime.api.commons.DynamicValueUtils.isNullOrBlank;

@ModuleComponent("FTP Delete")
@Description("The FTP delete component allows to delete a file from a remote FTP server.")
public class FTPDelete implements ProcessorSync {

    @Property("Connection")
    @Description("FTP connection configuration to be used to execute the delete operation.")
    private ConnectionConfiguration connection;

    @Property("Path")
    @Hint("/documents/my-document.pdf")
    @Example("/documents/my-document.pdf")
    @Description("The path and name of the document to delete from the remote FTP server. " +
            "If not present, the message payload is taken as path.")
    private DynamicString path;

    private FTPClientProvider provider;

    @Reference
    ScriptEngineService scriptEngine;

    @Override
    public void initialize() {
        provider = new FTPClientProvider(FTPDelete.class, connection);
    }

    @Override
    public Message apply(FlowContext flowContext, Message message) {

        // We use the payload if the path is not given.
        String remoteFileName;
        if (isNullOrBlank(path)) {
            TypedContent<?, ?> content = message.content();
            if (content instanceof StringContent) {
                remoteFileName = ((StringContent) content).data();
            } else {
                throw new FTPDeleteException("Type not supported");
            }
        } else {
            remoteFileName = scriptEngine.evaluate(path, flowContext, message)
                    .orElseThrow(() -> new FTPUploadException("File name was null"));

        }

        Command<Boolean> command = new CommandDeleteFile(remoteFileName);

        boolean success = provider.execute(command);
        if (!success)  {
            throw new FTPDeleteException("Error could not be downloaded");
        }

        return MessageBuilder.get(FTPDelete.class)
                .withJavaObject(success)
                .build();
    }

    public void setConnection(ConnectionConfiguration connection) {
        this.connection = connection;
    }

    public void setPath(DynamicString path) {
        this.path = path;
    }
}
