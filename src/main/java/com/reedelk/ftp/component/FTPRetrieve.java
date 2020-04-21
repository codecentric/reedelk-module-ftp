package com.reedelk.ftp.component;

import com.reedelk.ftp.internal.CommandRetrieve;
import com.reedelk.ftp.internal.ExceptionMapper;
import com.reedelk.ftp.internal.FTPClientProvider;
import com.reedelk.ftp.internal.commons.Utils;
import com.reedelk.ftp.internal.exception.FTPRetrieveException;
import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.exception.PlatformException;
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

import static com.reedelk.ftp.internal.commons.Messages.FTPRetrieve.*;
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
    @Description("The path and name of the file to retrieve from the remote FTP server. " +
            "If not present, the message payload is taken as path.")
    private DynamicString path;

    @Reference
    ScriptEngineService scriptEngine;

    private FTPClientProvider provider;
    private ExceptionMapper exceptionMapper;


    @Override
    public void initialize() {
        provider = new FTPClientProvider(FTPRetrieve.class, connection);
        exceptionMapper = new FTPRetrieveExceptionMapper();
    }

    @Override
    public void dispose() {
        if (provider != null) {
            provider.dispose();
        }
    }

    @Override
    public Message apply(FlowContext flowContext, Message message) {

        String pathToAdd;
        if (isNullOrBlank(path)) {
            pathToAdd = Utils.pathFromPayloadOrThrow(message, FTPRetrieveException::new);
        } else {
            pathToAdd = scriptEngine.evaluate(path, flowContext, message)
                    .orElseThrow(() -> new FTPRetrieveException(PATH_EMPTY.format(path.value())));
        }

        String remotePath = Utils.joinPath(connection.getWorkingDir(), pathToAdd);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            CommandRetrieve command = new CommandRetrieve(remotePath, outputStream);
            boolean success = provider.execute(command, exceptionMapper);
            if (success)  {
                byte[] data = outputStream.toByteArray();
                return MessageBuilder.get(FTPRetrieve.class)
                        .withBinary(data)
                        .build();
            } else {
                return MessageBuilder.get(FTPRetrieve.class)
                        .empty()
                        .build();
            }

        } catch (IOException exception) {
            String error = ERROR_GENERIC.format(exception.getMessage());
            throw new FTPRetrieveException(error, exception);
        }
    }

    public void setConnection(ConnectionConfiguration connection) {
        this.connection = connection;
    }

    public void setPath(DynamicString path) {
        this.path = path;
    }

    private String pathFromPayloadOrThrow(Message message) {
        TypedContent<?, ?> content = message.content();
        if (content instanceof StringContent) {
            return ((StringContent) content).data();
        } else {
            String error = TYPE_NOT_SUPPORTED.format(Utils.classNameOrNull(content));
            throw new FTPRetrieveException(error);
        }
    }

    private static class FTPRetrieveExceptionMapper implements ExceptionMapper {

        @Override
        public PlatformException from(Exception exception) {
            String error = ERROR_GENERIC.format(exception.getMessage());
            return new FTPRetrieveException(error, exception);
        }

        @Override
        public PlatformException from(String error) {
            String message = ERROR_GENERIC.format(error);
            return new FTPRetrieveException(message);
        }
    }
}
