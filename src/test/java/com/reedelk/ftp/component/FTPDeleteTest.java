package com.reedelk.ftp.component;

import com.reedelk.ftp.internal.exception.FTPDeleteException;
import com.reedelk.runtime.api.commons.ModuleContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class FTPDeleteTest extends AbstractTest {

    private FTPDelete component;

    @BeforeEach
    void setUp() {
        super.setUp();
        component = new FTPDelete();
        component.scriptEngine = scriptEngine;
        component.setConnection(connection);
    }

    @Test
    void shouldSuccessfullyDeleteFile() {
        // Given
        String path = "/data/foobar.txt";
        component.setPath(DynamicString.from(path));
        component.initialize();

        Message message = MessageBuilder.get(TestComponent.class).empty().build();

        // When
        Message actual = component.apply(context, message);

        // Then
        boolean isSuccess = actual.payload();
        assertThat(isSuccess).isTrue();

        boolean exists = getFileSystem().exists(path);
        assertThat(exists).isFalse();
    }

    @Test
    void shouldSuccessfullyReturnFalseWhenTryToDeleteDirectory() {
        // Given
        String path = "/data";
        component.setPath(DynamicString.from(path));
        component.initialize();

        Message message = MessageBuilder.get(TestComponent.class).empty().build();

        // When
        Message actual = component.apply(context, message);

        // Then
        boolean isSuccess = actual.payload();
        assertThat(isSuccess).isFalse();

        boolean exists = getFileSystem().exists(path);
        assertThat(exists).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenPathEvaluatesEmpty() {
        // Given
        String path = "#[context.varWhichDoesNotExist]";
        component.setPath(DynamicString.from(path, new ModuleContext(10L)));
        component.initialize();

        Message message = MessageBuilder.get(TestComponent.class).empty().build();

        doAnswer(invocation -> Optional.empty())
                .when(scriptEngine)
                .evaluate(any(DynamicValue.class), eq(context), eq(message));

        // When
        FTPDeleteException thrown = assertThrows(FTPDeleteException.class,
                () -> component.apply(context, message));

        // Then
        assertThat(thrown)
                .hasMessage("The path and name of the file to delete from the " +
                        "remote FTP server was null (DynamicValue=[#[context.varWhichDoesNotExist]]).");
    }

    @Test
    void shouldDeleteFileByConcatenatingPathWithWorkingDirectory() {
        // Given
        ConnectionConfiguration connection = new ConnectionConfiguration();
        connection.setPort(getServerPort());
        connection.setType(ConnectionType.FTP);
        connection.setHost(TEST_HOST);
        connection.setWorkingDir("/data");
        connection.setUsername(TEST_USERNAME);
        connection.setPassword(TEST_PASSWORD);

        String path = "/foobar.txt";
        component.setPath(DynamicString.from(path));
        component.setConnection(connection);
        component.initialize();

        Message message = MessageBuilder.get(TestComponent.class).empty().build();

        // When
        Message actual = component.apply(context, message);

        // Then
        boolean isSuccess = actual.payload();
        assertThat(isSuccess).isTrue();

        boolean exists = getFileSystem().exists(path);
        assertThat(exists).isFalse();
    }

    @Test
    void shouldTakePathFromPayloadWhenPathPropertyIsNull() {
        // Given
        String path = "/data/foobar.txt";
        component.initialize();

        Message message = MessageBuilder.get(TestComponent.class)
                .withText(path)
                .build();

        // When
        Message actual = component.apply(context, message);

        // Then
        boolean isSuccess = actual.payload();
        assertThat(isSuccess).isTrue();

        boolean exists = getFileSystem().exists(path);
        assertThat(exists).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenPayloadIsNotStringType() {
        // Given
        List<String> notSupportedType = Collections.emptyList();
        component.initialize();

        Message message = MessageBuilder.get(TestComponent.class)
                .withJavaObject(notSupportedType)
                .build();

        // When
        FTPDeleteException type =
                assertThrows(FTPDeleteException.class, () -> component.apply(context, message));

        // Then
        assertThat(type).hasMessage("The component only support payload input with String type, " +
                "however type=[com.reedelk.runtime.api.message.content.ListContent] was found.");
    }

    @Test
    void shouldThrowExceptionWhenConnectionParametersAreNotCorrect() {
        // Given
        ConnectionConfiguration connection = new ConnectionConfiguration();
        connection.setPort(getServerPort());
        connection.setType(ConnectionType.FTP);
        connection.setHost(TEST_HOST);
        connection.setWorkingDir("/data");
        connection.setUsername("wrongUsername");
        connection.setPassword("wrongPassword");

        String path = "/foobar.txt";
        component.setPath(DynamicString.from(path));
        component.setConnection(connection);
        component.initialize();

        Message message = MessageBuilder.get(TestComponent.class)
                .empty()
                .build();

        // When
        FTPDeleteException type =
                assertThrows(FTPDeleteException.class, () -> component.apply(context, message));

        // Then
        assertThat(type).hasMessage("An error occurred while executing FTP Delete operation, " +
                "cause=[Could not login! Username and password wrong?]");
    }

    @Override
    protected void configure(FileSystem fileSystem) {
        fileSystem.add(new DirectoryEntry("/data"));
        fileSystem.add(new FileEntry("/data/foobar.txt", "abcdef 1234567890"));
    }

    @Override
    protected void clean(FileSystem fileSystem) {
        fileSystem.delete("/data/foobar.txt");
        fileSystem.delete("/data");
    }
}