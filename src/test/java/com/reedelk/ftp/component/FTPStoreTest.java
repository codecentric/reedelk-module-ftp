package com.reedelk.ftp.component;

import com.reedelk.ftp.internal.exception.FTPDeleteException;
import com.reedelk.ftp.internal.exception.FTPStoreException;
import com.reedelk.runtime.api.commons.ByteArrayUtils;
import com.reedelk.runtime.api.converter.ConverterService;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicByteArray;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.FileSystemEntry;
import org.mockito.Mock;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

public class FTPStoreTest extends AbstractTest {

    private FTPStore component;

    @Mock
    private ConverterService converterService;

    @BeforeEach
    void setUp() {
        super.setUp();
        component = new FTPStore();
        component.scriptEngine = scriptEngine;
        component.converterService = converterService;
        component.setConnection(connection);
    }

    @Test
    void shouldCorrectlyStoreTextDataFromPayload() throws IOException {
        // Given
        component.setPath(DynamicString.from("/data/myFile.txt"));
        component.initialize();
        String textData = "My data";

        doReturn(textData.getBytes())
                .when(converterService)
                .convert(textData, byte[].class);

        Message message = MessageBuilder.get(TestComponent.class)
                .withText(textData)
                .build();

        // When
        Message actual = component.apply(context, message);

        // Then
        boolean success = actual.payload();
        assertThat(success).isTrue();
        assertExistFileEntryWith("/data/myFile.txt", "myFile.txt", textData);
    }

    @Test
    void shouldCorrectlyStoreTextDataFromContentProperty() throws IOException {
        // Given
        String textData = "My data";
        component.setContent(DynamicByteArray.from(textData));
        component.setPath(DynamicString.from("/data/myFile.txt"));
        component.initialize();

        Message message = MessageBuilder.get(TestComponent.class)
                .withText(textData)
                .build();

        // When
        Message actual = component.apply(context, message);

        // Then
        boolean success = actual.payload();
        assertThat(success).isTrue();
        assertExistFileEntryWith("/data/myFile.txt", "myFile.txt", textData);
    }

    @Test
    void shouldCorrectlyStoreByteArrayDataFromPayload() throws IOException {
        // Given
        component.setPath(DynamicString.from("/data/myFile.txt"));
        component.initialize();
        byte[] dataAsBytes = "one".getBytes();

        doReturn(dataAsBytes).when(converterService).convert(dataAsBytes, byte[].class);

        Message message = MessageBuilder.get(TestComponent.class)
                .withBinary(dataAsBytes)
                .build();

        // When
        Message actual = component.apply(context, message);

        // Then
        boolean success = actual.payload();
        assertThat(success).isTrue();
        assertExistFileEntryWith("/data/myFile.txt", "myFile.txt", new String(dataAsBytes));
    }

    @Test
    void shouldStoreFileByConcatenatingPathWithWorkingDirectory() throws IOException {
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

        byte[] dataAsBytes = "one".getBytes();

        doReturn(dataAsBytes).when(converterService).convert(dataAsBytes, byte[].class);

        Message message = MessageBuilder.get(TestComponent.class)
                .withBinary(dataAsBytes)
                .build();

        // When
        Message actual = component.apply(context, message);

        // Then
        boolean success = actual.payload();
        assertThat(success).isTrue();
        assertExistFileEntryWith("/data/foobar.txt", "foobar.txt", new String(dataAsBytes));
    }

    @Test
    void shouldReturnFalseWhenDataCouldNotBeStored() {
        // Given
        component.setPath(DynamicString.from("/data"));
        component.initialize();
        byte[] dataAsBytes = "one".getBytes();

        doReturn(dataAsBytes).when(converterService).convert(dataAsBytes, byte[].class);

        Message message = MessageBuilder.get(TestComponent.class)
                .withBinary(dataAsBytes)
                .build();

        // When
        Message actual = component.apply(context, message);

        // Then
        boolean success = actual.payload();
        assertThat(success).isFalse();
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
        FTPStoreException type =
                assertThrows(FTPStoreException.class, () -> component.apply(context, message));

        // Then
        assertThat(type).hasMessage("An error occurred while executing FTP Store operation, " +
                "cause=[Could not login! Username and password wrong?]");
    }

    @Override
    protected void configure(FileSystem fileSystem) {
        fileSystem.add(new DirectoryEntry("/data"));
    }

    @Override
    protected void clean(FileSystem fileSystem) {
        fileSystem.delete("/data/myFile.txt");
        fileSystem.delete("/data/foobar.txt");
        fileSystem.delete("/data");
    }

    private void assertExistFileEntryWith(String path, String entryName, String content) throws IOException {
        FileSystem fileSystem = getFileSystem();
        FileSystemEntry entry = fileSystem.getEntry(path);
        assertThat(entry).isNotNull();
        assertThat(entry.getName()).isEqualTo(entryName);

        FileEntry myFile = (FileEntry) entry;
        assertFileEntryContentIs(myFile, content);
    }

    private void assertFileEntryContentIs(FileEntry myFile, String expectedContent) throws IOException {
        try (InputStream inputStream = myFile.createInputStream()) {
            byte[] data = ByteArrayUtils.from(inputStream);
            assertThat(new String(data)).isEqualTo(expectedContent);
        }
    }
}
