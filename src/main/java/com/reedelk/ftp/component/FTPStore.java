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
import org.osgi.service.component.annotations.Reference;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static com.reedelk.runtime.api.commons.ConfigurationPreconditions.requireNotNull;
import static com.reedelk.runtime.api.commons.DynamicValueUtils.isNullOrBlank;

@ModuleComponent("FTP Store")
public class FTPStore implements ProcessorSync {

    @Property("Connection Configuration")
    private ConnectionConfiguration configuration;

    @Property("Path")
    @Hint("/documents/my-document.pdf")
    @Example("/documents/my-document.pdf")
    @Description("The path ")
    private DynamicString path;

    @Property("Content") // Comes from the body (payload)
    @DefaultValue("#[message.payload()]")
    private DynamicByteArray content;

    @Reference
    ScriptEngineService scriptEngine;
    @Reference
    ConverterService converterService;

    private FTPClientProvider provider;

    @Override
    public void initialize() {
        requireNotNull(FTPStore.class, configuration, "Configuration");
        provider = new FTPClientProvider(configuration);
    }

    @Override
    public Message apply(FlowContext flowContext, Message message) {

        String uploadFileName = scriptEngine.evaluate(path, flowContext, message)
                .orElseThrow(() -> new FTPUploadException("File name was null"));

        if (isNullOrBlank(content)) {
            // We must convert the payload to byte array. Here should consider if it is a stream or not.
            Object payload = message.payload();
            byte[] data = converterService.convert(payload, byte[].class);
            return upload(uploadFileName, data);

        } else {
            byte[] evaluatedUploadData = scriptEngine.evaluate(content, flowContext, message)
                    .orElseThrow(() -> new FTPUploadException("Upload data was null")); // Should this one write an empty file?
            return upload(uploadFileName, evaluatedUploadData);
        }
    }

    public void setConfiguration(ConnectionConfiguration configuration) {
        this.configuration = configuration;
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
