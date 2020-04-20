package com.reedelk.ftp.component;

import com.reedelk.ftp.internal.CommandRetrieve;
import com.reedelk.ftp.internal.FTPClientProvider;
import com.reedelk.ftp.internal.exception.FTPDeleteException;
import com.reedelk.ftp.internal.exception.FTPDownloadException;
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
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.reedelk.runtime.api.commons.DynamicValueUtils.isNullOrBlank;

@ModuleComponent("FTP Retrieve")
@Description("The FTP Retrieve component allows to download a file from a remote FTP server. " +
        "The path of the file to download might be a static or dynamic value. If the path is not given, " +
        "the name of the file to download is taken from the message payload. " +
        "An error is thrown if the payload is not of type String or if the evaluated path is empty or null.")
@Component(service = FTPRetrieve.class, scope = ServiceScope.PROTOTYPE)
public class FTPRetrieve implements ProcessorSync {

    @Property("Connection")
    @Description("FTP connection configuration to be used to retrieve files from.")
    private ConnectionConfiguration connection;

    @Property("Path")
    @Hint("/documents/my-document.pdf")
    @Example("/documents/my-document.pdf")
    @Description("The path and name of the document to retrieve from the remote FTP server. " +
            "If not present, the message payload is taken as path.")
    private DynamicString path;

    private FTPClientProvider provider;

    @Reference
    ScriptEngineService scriptEngine;

    @Override
    public void initialize() {
        provider = new FTPClientProvider(FTPRetrieve.class, connection);
    }

    @Override
    public Message apply(FlowContext flowContext, Message message) {

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

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            CommandRetrieve command = new CommandRetrieve(remoteFileName, outputStream);
            boolean success = provider.execute(command);
            if (!success)  {
                throw new FTPDownloadException("Error could not be downloaded");
            }

            byte[] data = outputStream.toByteArray();
            return MessageBuilder.get(FTPRetrieve.class)
                    .withBinary(data)
                    .build();

        } catch (IOException exception) {
            throw new FTPDownloadException("Error");
        }
    }

    public void setConnection(ConnectionConfiguration connection) {
        this.connection = connection;
    }

    public void setPath(DynamicString path) {
        this.path = path;
    }
}
