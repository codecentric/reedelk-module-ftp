package de.codecentric.reedelk.ftp.component;

import de.codecentric.reedelk.ftp.internal.CommandList;
import de.codecentric.reedelk.ftp.internal.ExceptionMapper;
import de.codecentric.reedelk.ftp.internal.FTPClientProvider;
import de.codecentric.reedelk.ftp.internal.attribute.FTPAttribute;
import de.codecentric.reedelk.ftp.internal.commons.Utils;
import de.codecentric.reedelk.ftp.internal.exception.FTPListException;
import de.codecentric.reedelk.ftp.internal.type.FTPFile;
import de.codecentric.reedelk.ftp.internal.type.ListOfFTPFile;
import de.codecentric.reedelk.runtime.api.annotation.*;
import de.codecentric.reedelk.runtime.api.component.ProcessorSync;
import de.codecentric.reedelk.runtime.api.converter.ConverterService;
import de.codecentric.reedelk.runtime.api.exception.PlatformException;
import de.codecentric.reedelk.runtime.api.flow.FlowContext;
import de.codecentric.reedelk.runtime.api.message.Message;
import de.codecentric.reedelk.runtime.api.message.MessageBuilder;
import de.codecentric.reedelk.runtime.api.script.ScriptEngineService;
import de.codecentric.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import java.util.List;

import static de.codecentric.reedelk.ftp.internal.commons.Messages.FTPList.ERROR_GENERIC;
import static de.codecentric.reedelk.ftp.internal.commons.Messages.FTPList.PATH_EMPTY;
import static de.codecentric.reedelk.runtime.api.commons.DynamicValueUtils.isNullOrBlank;

@ModuleComponent("FTP List Files")
@ComponentOutput(
        attributes = FTPAttribute.class,
        payload = ListOfFTPFile.class,
        description = "The list of files on the FTP server from the given path.")
@ComponentInput(
        payload = Object.class,
        description = "The input payload is used to evaluate the path expression to determine the path to list files from on the remote FTP server.")
@Description("The FTP List Files component allows to list all the files from a remote FTP server directory. " +
        "If the property recursive is true, the listing of the files is recursive starting from the configured" +
        "working directory in the Connection configuration. The component allows to also only list files or directories.")
@Component(service = FTPList.class, scope = ServiceScope.PROTOTYPE)
public class FTPList implements ProcessorSync {

    @DialogTitle("FTP Configuration")
    @Property("Connection")
    @Description("FTP connection configuration to be used to list files from.")
    private ConnectionConfiguration connection;

    @Property("Path")
    @DefaultValue("")
    @Hint("/documents")
    @Example("/documents")
    @Description("The path from which files will be listed from the remote FTP server. " +
            "The path can be a static or a dynamic value. " +
            "If both path and working directory from the connection configuration are present they are " +
            "appended to each other and the final path is computed as follow: /{WORKING_DIRECTORY}/{PATH}.")
    private DynamicString path;

    @Property("Recursive")
    @Example("true")
    @DefaultValue("false")
    @Description("If true files are listed recursively starting from the working directory taken from the connection configuration.")
    private Boolean recursive;

    @Property("Files only")
    @Example("true")
    @DefaultValue("false")
    @Description("If true only files are listed from the working directory taken from the connection configuration.")
    private Boolean filesOnly;

    @Property("Directories only")
    @Example("true")
    @DefaultValue("false")
    @Description("If true only directories are listed from the working directory taken from the connection configuration.")
    private Boolean directoriesOnly;

    @Reference
    ScriptEngineService scriptEngine;
    @Reference
    ConverterService converterService;

    private FTPClientProvider provider;
    private ExceptionMapper exceptionMapper;

    @Override
    public void initialize() {
        provider = new FTPClientProvider(FTPList.class, connection);
        exceptionMapper = new FTPListExceptionMapper();
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
            // We take the path from the payload if the path is not given.
            Object content = message.payload();
            pathToAdd = converterService.convert(content, String.class);
        } else {
            pathToAdd = scriptEngine.evaluate(path, flowContext, message)
                    .orElseThrow(() -> new FTPListException(PATH_EMPTY.format(path)));
        }

        String remotePath = Utils.joinPath(connection.getWorkingDir(), pathToAdd);

        CommandList commandList =
                new CommandList(remotePath, recursive, filesOnly, directoriesOnly);

        List<FTPFile> files = provider.execute(commandList, exceptionMapper);

        FTPAttribute attribute = new FTPAttribute(remotePath);

        return MessageBuilder.get(FTPList.class)
                .withList(files, FTPFile.class)
                .attributes(attribute)
                .build();
    }

    public void setConnection(ConnectionConfiguration connection) {
        this.connection = connection;
    }

    public void setDirectoriesOnly(Boolean directoriesOnly) {
        this.directoriesOnly = directoriesOnly;
    }

    public void setRecursive(Boolean recursive) {
        this.recursive = recursive;
    }

    public void setFilesOnly(Boolean filesOnly) {
        this.filesOnly = filesOnly;
    }

    public void setPath(DynamicString path) {
        this.path = path;
    }

    private static class FTPListExceptionMapper implements ExceptionMapper {

        @Override
        public PlatformException from(Exception exception) {
            String error = ERROR_GENERIC.format(exception.getMessage());
            return new FTPListException(error, exception);
        }

        @Override
        public PlatformException from(String error) {
            String message = ERROR_GENERIC.format(error);
            return new FTPListException(message);
        }
    }
}
