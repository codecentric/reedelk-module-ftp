package de.codecentric.reedelk.ftp.component;

import de.codecentric.reedelk.ftp.internal.CommandStore;
import de.codecentric.reedelk.ftp.internal.ExceptionMapper;
import de.codecentric.reedelk.ftp.internal.FTPClientProvider;
import de.codecentric.reedelk.ftp.internal.attribute.FTPAttribute;
import de.codecentric.reedelk.ftp.internal.commons.Utils;
import de.codecentric.reedelk.ftp.internal.exception.FTPStoreException;
import de.codecentric.reedelk.runtime.api.annotation.*;
import de.codecentric.reedelk.runtime.api.component.ProcessorSync;
import de.codecentric.reedelk.runtime.api.converter.ConverterService;
import de.codecentric.reedelk.runtime.api.exception.PlatformException;
import de.codecentric.reedelk.runtime.api.flow.FlowContext;
import de.codecentric.reedelk.runtime.api.message.Message;
import de.codecentric.reedelk.runtime.api.message.MessageBuilder;
import de.codecentric.reedelk.runtime.api.script.ScriptEngineService;
import de.codecentric.reedelk.runtime.api.script.dynamicvalue.DynamicByteArray;
import de.codecentric.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static de.codecentric.reedelk.ftp.internal.commons.Messages.FTPStore.*;
import static de.codecentric.reedelk.runtime.api.commons.DynamicValueUtils.isNullOrBlank;

@ModuleComponent("FTP Store")
@ComponentOutput(
        attributes = FTPAttribute.class,
        payload = boolean.class,
        description = "True if the file was successfully stored on the remote FTP server, false otherwise.")
@ComponentInput(
        payload = { byte[].class, String.class },
        description = "The data to be stored on the remote FTP server. The expected input is byte array or string.")
@Description("The FTP Store component allows to store a file to a remote FTP server. " +
        "The path of the file to store might be a static or dynamic value. " +
        "The path of the file is mandatory and if not present an error will be thrown by the component." +
        "The content of the file to store can be a static or dynamic value. If not present, the content " +
        "of the file is implicitly taken from the message payload. ")
@Component(service = FTPStore.class, scope = ServiceScope.PROTOTYPE)
public class FTPStore implements ProcessorSync {

    @DialogTitle("FTP Configuration")
    @Property("Connection")
    @Description("FTP connection configuration to be used to execute the store operation.")
    private ConnectionConfiguration connection;

    @Property("Path")
    @Hint("/documents/my-document.pdf")
    @Example("/documents/my-document.pdf")
    @Description("The path and name of the file to store to the remote FTP server.")
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
    private ExceptionMapper exceptionMapper;

    @Override
    public void initialize() {
        provider = new FTPClientProvider(FTPStore.class, connection);
        exceptionMapper = new FTPStoreExceptionMapper();
    }

    @Override
    public void dispose() {
        if (provider != null) {
            provider.dispose();
        }
    }

    @Override
    public Message apply(FlowContext flowContext, Message message) {

        String pathToStore = scriptEngine.evaluate(path, flowContext, message)
                .orElseThrow(() -> new FTPStoreException(PATH_EMPTY.format(path.value())));
        String remotePath = Utils.joinPath(connection.getWorkingDir(), pathToStore);

        if (isNullOrBlank(content)) {
            // We must convert the payload to byte array because we have to upload
            // bytes to the remote FTP server.
            Object payload = message.payload();
            byte[] data = converterService.convert(payload, byte[].class);
            return upload(remotePath, data);

        } else {
            byte[] contentAsBytes = scriptEngine.evaluate(content, flowContext, message)
                    .orElseThrow(() -> new FTPStoreException(CONTENT_EMPTY.format(content)));
            return upload(remotePath, contentAsBytes);
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

    private Message upload(String remotePath, byte[] data) {
        // We upload an empty file if the payload is empty.
        if (data == null) data = new byte[0];

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {

            CommandStore command = new CommandStore(remotePath, inputStream);

            boolean success = provider.execute(command, exceptionMapper);

            FTPAttribute attribute = new FTPAttribute(remotePath);

            if (success) {
                return MessageBuilder.get(FTPStore.class)
                        .withJavaObject(success)
                        .attributes(attribute)
                        .build();
            } else {
                return MessageBuilder.get(FTPStore.class)
                        .withJavaObject(success)
                        .attributes(attribute)
                        .build();
            }
        } catch (IOException exception) {
            String error = ERROR_GENERIC.format(exception.getMessage());
            throw new FTPStoreException(error, exception);
        }
    }

    private static class FTPStoreExceptionMapper implements ExceptionMapper {

        @Override
        public PlatformException from(Exception exception) {
            String error = ERROR_GENERIC.format(exception.getMessage());
            return new FTPStoreException(error, exception);
        }

        @Override
        public PlatformException from(String error) {
            String message = ERROR_GENERIC.format(error);
            return new FTPStoreException(message);
        }
    }
}
