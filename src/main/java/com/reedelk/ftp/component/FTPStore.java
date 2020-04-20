package com.reedelk.ftp.component;

import com.reedelk.ftp.internal.CommandStore;
import com.reedelk.ftp.internal.FTPClientProvider;
import com.reedelk.ftp.internal.exception.FTPDownloadException;
import com.reedelk.ftp.internal.exception.FTPUploadException;
import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.converter.ConverterService;
import com.reedelk.runtime.api.exception.PlatformException;
import com.reedelk.runtime.api.flow.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.script.ScriptEngineService;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicByteArray;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static com.reedelk.runtime.api.commons.DynamicValueUtils.isNullOrBlank;

@ModuleComponent("FTP Store")
@Description("The FTP Retrieve component allows to store a file to a remote FTP server. " +
        "The path of the file to store might be a static or dynamic value. " +
        "The path of the file is mandatory and if not present an error will be thrown by the component." +
        "The content of the file to store can be a static or dynamic value. If not present, the content " +
        "of the file is implicitly taken from the message payload. ")
@Component(service = FTPStore.class, scope = ServiceScope.PROTOTYPE)
public class FTPStore implements ProcessorSync {

    @Property("Connection")
    @Description("FTP connection configuration to be used to execute the store operation.")
    private ConnectionConfiguration connection;

    @Property("Path")
    @Hint("/documents/my-document.pdf")
    @Example("/documents/my-document.pdf")
    @Description("The path and name of the document to store to the remote FTP server.")
    private DynamicString path;

    @Property("Content") // Comes from the body (payload)
    @DefaultValue("#[message.payload()]")
    @Example("#[context.myContentVar")
    @Description("The content of the file to store to the remote FTP server. " +
            "If not present, the message payload is taken as content of the file.")
    private DynamicByteArray content;

    @Reference
    ScriptEngineService scriptEngine;
    @Reference
    ConverterService converterService;

    private FTPClientProvider provider;

    @Override
    public void initialize() {
        provider = new FTPClientProvider(FTPStore.class, connection);
    }

    @Override
    public Message apply(FlowContext flowContext, Message message) {

        String remoteFilePath = scriptEngine.evaluate(path, flowContext, message)
                .orElseThrow(() -> new FTPUploadException("File name was null"));

        if (isNullOrBlank(content)) {
            // We must convert the payload to byte array. Here should consider if it is a stream or not.
            Object payload = message.payload();
            byte[] data = converterService.convert(payload, byte[].class);
            return upload(remoteFilePath, data);

        } else {
            byte[] evaluatedUploadData = scriptEngine.evaluate(content, flowContext, message)
                    .orElseThrow(() -> new FTPUploadException("Upload data was null")); // Should this one write an empty file?
            return upload(remoteFilePath, evaluatedUploadData);
        }
    }

    public void setConnection(ConnectionConfiguration connection) {
        this.connection = connection;
    }

    public void setContent(DynamicByteArray content) {
        this.content = content;
    }

    public void setPath(DynamicString path) {
        this.path = path;
    }

    private Message upload(String uploadFileName, byte[] data) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {

            CommandStore command = new CommandStore(uploadFileName, inputStream);
            boolean success = provider.execute(command);
            if (!success) {
                throw new FTPDownloadException("Error could not be uploaded");
            }

            return MessageBuilder.get(FTPRetrieve.class)
                    .withJavaObject(true)
                    .build();
        } catch (IOException exception) {
            throw new PlatformException(exception);
        }
    }
}
