package com.reedelk.ftp.component;

import com.reedelk.ftp.internal.Command;
import com.reedelk.ftp.internal.CommandDeleteFile;
import com.reedelk.ftp.internal.ExceptionMapper;
import com.reedelk.ftp.internal.FTPClientProvider;
import com.reedelk.ftp.internal.exception.FTPDeleteException;
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

import static com.reedelk.ftp.internal.commons.Messages.FTPDelete.*;
import static com.reedelk.ftp.internal.commons.Utils.classNameOrNull;
import static com.reedelk.runtime.api.commons.DynamicValueUtils.isNullOrBlank;

@ModuleComponent("FTP Delete")
@Description("The FTP Delete component allows to delete a file from a remote FTP server. " +
        "The path of the file to be deleted might be a static or dynamic value. If the path is not given, " +
        "the name of the file to be deleted is taken from the message payload. " +
        "An error is thrown if the payload is not of type String or if the evaluated path is empty or null.")
@Component(service = FTPDelete.class, scope = ServiceScope.PROTOTYPE)
public class FTPDelete implements ProcessorSync {

    @Property("Connection")
    @Description("FTP connection configuration to be used to execute the delete operation.")
    private ConnectionConfiguration connection;

    @Property("Path")
    @Hint("/documents/my-document.pdf")
    @Example("/documents/my-document.pdf")
    @Description("The path and name of the file to delete from the remote FTP server. " +
            "If not present, the message payload is taken as path.")
    private DynamicString path;

    @Reference
    ScriptEngineService scriptEngine;

    private FTPClientProvider provider;
    private ExceptionMapper exceptionMapper;

    @Override
    public void initialize() {
        provider = new FTPClientProvider(FTPDelete.class, connection);
        exceptionMapper = new FTPDeleteExceptionMapper();
    }

    @Override
    public void dispose() {
        provider.dispose();
    }

    @Override
    public Message apply(FlowContext flowContext, Message message) {

        // We use the payload if the path is not given.
        String remotePath = connection.getWorkingDir();
        if (isNullOrBlank(path)) {
            remotePath += pathFromPayloadOrThrow(message);
        } else {
            remotePath += scriptEngine.evaluate(path, flowContext, message)
                            .orElseThrow(() -> new FTPDeleteException(PATH_EMPTY.format(path.value())));
        }

        Command<Boolean> command = new CommandDeleteFile(remotePath);

        boolean success = provider.execute(command, exceptionMapper);

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

    private String pathFromPayloadOrThrow(Message message) {
        TypedContent<?, ?> content = message.content();
        if (content instanceof StringContent) {
            return ((StringContent) content).data();
        } else {
            String error = TYPE_NOT_SUPPORTED.format(classNameOrNull(content));
            throw new FTPDeleteException(error);
        }
    }

    private static class FTPDeleteExceptionMapper implements ExceptionMapper {

        @Override
        public PlatformException from(Exception exception) {
            String error = ERROR_GENERIC.format(exception.getMessage());
            return new FTPDeleteException(error, exception);
        }

        @Override
        public PlatformException from(String error) {
            String message = ERROR_GENERIC.format(error);
            return new FTPDeleteException(message);
        }
    }
}
