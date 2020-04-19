package com.reedelk.ftp.component;

import com.reedelk.ftp.internal.FTPClientProvider;
import com.reedelk.ftp.internal.exception.FTPDownloadException;
import com.reedelk.ftp.internal.exception.FTPUploadException;
import com.reedelk.runtime.api.annotation.ModuleComponent;
import com.reedelk.runtime.api.annotation.Property;
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
public class FTPUpload implements ProcessorSync {

    @Property("Connection Configuration")
    private ConnectionConfiguration configuration;

    @Property("File name")
    private DynamicString fileName;

    @Property("Upload data")
    private DynamicByteArray uploadData;

    private FTPClientProvider provider;

    @Reference
    private ScriptEngineService scriptEngine;
    @Reference
    private ConverterService converterService;

    @Override
    public void initialize() {
        requireNotNull(FTPList.class, configuration, "Configuration");
        provider = new FTPClientProvider(configuration);
    }

    @Override
    public Message apply(FlowContext flowContext, Message message) {

        String uploadFileName = scriptEngine.evaluate(fileName, flowContext, message)
                .orElseThrow(() -> new FTPUploadException("File name was null"));

        if (isNullOrBlank(uploadData)) {
            // We must convert the payload to byte array. Here should consider if it is a stream or not.
            Object payload = message.payload();
            byte[] data = converterService.convert(payload, byte[].class);
            return upload(uploadFileName, data);

        } else {
            byte[] evaluatedUploadData = scriptEngine.evaluate(uploadData, flowContext, message)
                    .orElseThrow(() -> new FTPUploadException("Upload data was null")); // Should this one write an empty file?
            return upload(uploadFileName, evaluatedUploadData);
        }
    }

    public void setConfiguration(ConnectionConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setUploadData(DynamicByteArray uploadData) {
        this.uploadData = uploadData;
    }

    public void setFileName(DynamicString fileName) {
        this.fileName = fileName;
    }

    private Message upload(String uploadFileName, byte[] data) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {

            boolean success = provider.upload(uploadFileName, inputStream);
            if (!success) {
                throw new FTPDownloadException("Error could not be uploaded");
            }

            return MessageBuilder.get(FTPRetrieve.class)
                    .withBinary(data)
                    .build();
        } catch (IOException e) {
            throw new PlatformException(e);
        }
    }
}
