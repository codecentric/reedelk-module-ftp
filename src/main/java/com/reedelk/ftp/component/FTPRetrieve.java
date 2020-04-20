package com.reedelk.ftp.component;

import com.reedelk.ftp.internal.CommandRetrieve;
import com.reedelk.ftp.internal.FTPClientProvider;
import com.reedelk.ftp.internal.exception.FTPDownloadException;
import com.reedelk.runtime.api.annotation.ModuleComponent;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.flow.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.script.ScriptEngineService;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import org.osgi.service.component.annotations.Reference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.reedelk.runtime.api.commons.ConfigurationPreconditions.requireNotNull;

@ModuleComponent("FTP Retrieve")
public class FTPRetrieve implements ProcessorSync {

    @Property("Connection Configuration")
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

        String downloadFileName = scriptEngine.evaluate(fileName, flowContext, message)
                .orElseThrow(() -> new FTPDownloadException("File name was null"));

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            CommandRetrieve command = new CommandRetrieve(downloadFileName, outputStream);
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

    public void setConfiguration(ConnectionConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setFileName(DynamicString fileName) {
        this.fileName = fileName;
    }
}
