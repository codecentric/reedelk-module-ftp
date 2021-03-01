package de.codecentric.reedelk.ftp.component;

import de.codecentric.reedelk.ftp.internal.exception.FTPListException;
import de.codecentric.reedelk.runtime.api.converter.ConverterService;
import de.codecentric.reedelk.runtime.api.message.Message;
import de.codecentric.reedelk.runtime.api.message.MessageBuilder;
import de.codecentric.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockito.Mock;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;


class FTPListTest extends AbstractTest {

    private FTPList component;

    @Mock
    private ConverterService converterService;

    @BeforeEach
    void setUp() {
        super.setUp();
        component = new FTPList();
        component.scriptEngine = scriptEngine;
        component.converterService = converterService;
        component.setConnection(connection);
    }

    @Test
    void shouldListAllFiles() {
        // Given
        component.setPath(DynamicString.from("/data"));
        component.initialize();

        Message message = MessageBuilder.get(TestComponent.class)
                .empty()
                .build();

        // When
        Message actual = component.apply(context, message);

        // Then
        List<Map<String, Serializable>> payload = actual.payload();
        assertThat(payload).hasSize(3);

        assertContainsFileWithName(payload, "foobar1.txt");
        assertContainsFileWithName(payload, "foobar2.txt");
        assertContainsFileWithName(payload, "documents");
    }

    @Test
    void shouldListFilesOnly() {
        // Given
        component.setFilesOnly(true);
        component.setPath(DynamicString.from("/data"));
        component.initialize();

        Message message = MessageBuilder.get(TestComponent.class)
                .empty()
                .build();

        // When
        Message actual = component.apply(context, message);

        // Then
        List<Map<String, Serializable>> payload = actual.payload();
        assertThat(payload).hasSize(2);

        assertContainsFileWithName(payload, "foobar1.txt");
        assertContainsFileWithName(payload, "foobar2.txt");
    }

    @Test
    void shouldListDirectoriesOnly() {
        // Given
        component.setDirectoriesOnly(true);
        component.setPath(DynamicString.from("/data"));
        component.initialize();

        Message message = MessageBuilder.get(TestComponent.class)
                .empty()
                .build();

        // When
        Message actual = component.apply(context, message);

        // Then
        List<Map<String, Serializable>> payload = actual.payload();
        assertThat(payload).hasSize(1);

        assertContainsFileWithName(payload, "documents");
    }

    @Test
    void shouldListAllFilesRecursively() {
        // Given
        component.setPath(DynamicString.from("/data"));
        component.setRecursive(true);
        component.initialize();

        Message message = MessageBuilder.get(TestComponent.class)
                .empty()
                .build();

        // When
        Message actual = component.apply(context, message);

        // Then
        List<Map<String, Serializable>> payload = actual.payload();
        assertThat(payload).hasSize(6);

        assertContainsFileWithName(payload, "foobar1.txt");
        assertContainsFileWithName(payload, "foobar2.txt");
        assertContainsFileWithName(payload, "documents");
        assertContainsFileWithName(payload, "document1.txt");
        assertContainsFileWithName(payload, "document2.txt");
        assertContainsFileWithName(payload, "company");
    }

    @Test
    void shouldListFilesOnlyRecursively() {
        // Given
        component.setFilesOnly(true);
        component.setPath(DynamicString.from("/data"));
        component.setRecursive(true);
        component.initialize();

        Message message = MessageBuilder.get(TestComponent.class)
                .empty()
                .build();

        // When
        Message actual = component.apply(context, message);

        // Then
        List<Map<String, Serializable>> payload = actual.payload();
        assertThat(payload).hasSize(4);

        assertContainsFileWithName(payload, "foobar1.txt");
        assertContainsFileWithName(payload, "foobar2.txt");
        assertContainsFileWithName(payload, "document1.txt");
        assertContainsFileWithName(payload, "document2.txt");
    }

    @Test
    void shouldListDirectoriesOnlyRecursively() {
        // Given
        component.setDirectoriesOnly(true);
        component.setPath(DynamicString.from("/data"));
        component.setRecursive(true);
        component.initialize();

        Message message = MessageBuilder.get(TestComponent.class)
                .empty()
                .build();

        // When
        Message actual = component.apply(context, message);

        // Then
        List<Map<String, Serializable>> payload = actual.payload();
        assertThat(payload).hasSize(2);

        assertContainsFileWithName(payload, "documents");
        assertContainsFileWithName(payload, "company");
    }


    @Test
    void shouldListFilesByConcatenatingPathWithWorkingDirectory() {
        // Given
        ConnectionConfiguration connection = new ConnectionConfiguration();
        connection.setPort(getServerPort());
        connection.setType(ConnectionType.FTP);
        connection.setHost(TEST_HOST);
        connection.setWorkingDir("/data");
        connection.setUsername(TEST_USERNAME);
        connection.setPassword(TEST_PASSWORD);

        String path = "/documents";
        component.setPath(DynamicString.from(path));
        component.setConnection(connection);
        component.initialize();

        Message message = MessageBuilder.get(TestComponent.class)
                .empty()
                .build();

        // When
        Message actual = component.apply(context, message);

        // Then
        List<Map<String, Serializable>> payload = actual.payload();
        assertThat(payload).hasSize(3);

        assertContainsFileWithName(payload, "company", "/data/documents");
        assertContainsFileWithName(payload, "document1.txt", "/data/documents");
        assertContainsFileWithName(payload, "document2.txt", "/data/documents");
    }

    @Test
    void shouldTakePathFromPayloadWhenPathPropertyIsNull() {
        // Given
        String path = "/data/documents";
        component.initialize();

        Message message = MessageBuilder.get(TestComponent.class)
                .withText(path)
                .build();

        doReturn(path).when(converterService)
                .convert(path, String.class);

        // When
        Message actual = component.apply(context, message);

        // Then
        List<Map<String, Serializable>> payload = actual.payload();
        assertThat(payload).hasSize(3);

        assertContainsFileWithName(payload, "company", "/data/documents");
        assertContainsFileWithName(payload, "document1.txt", "/data/documents");
        assertContainsFileWithName(payload, "document2.txt", "/data/documents");
    }

    @Test
    void shouldReturnEmptyWhenPayloadNotStringType() {
        // Given
        List<String> notSupportedType = Collections.emptyList();
        component.initialize();

        Message message = MessageBuilder.get(TestComponent.class)
                .withJavaObject(notSupportedType)
                .build();

        doReturn(notSupportedType.toString())
                .when(converterService)
                .convert(notSupportedType, String.class);

        // When
        Message actual = component.apply(context, message);

        // Then
        List<Map> results = actual.payload();
        assertThat(results).isEmpty();
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

        String path = "/documents";
        component.setPath(DynamicString.from(path));
        component.setConnection(connection);
        component.initialize();

        Message message = MessageBuilder.get(TestComponent.class)
                .empty()
                .build();

        // When
        FTPListException type =
                assertThrows(FTPListException.class, () -> component.apply(context, message));

        // Then
        assertThat(type).hasMessage("An error occurred while executing FTP List operation, " +
                "cause=[Could not login! Username and password wrong?]");
    }

    @Override
    protected void configure(FileSystem fileSystem) {
        fileSystem.add(new DirectoryEntry("/data"));
        fileSystem.add(new FileEntry("/data/foobar1.txt", "foobar1 content"));
        fileSystem.add(new FileEntry("/data/foobar2.txt", "foobar2 content"));
        fileSystem.add(new DirectoryEntry("/data/documents"));
        fileSystem.add(new FileEntry("/data/documents/document1.txt", "document1 content"));
        fileSystem.add(new FileEntry("/data/documents/document2.txt", "document2 content"));
        fileSystem.add(new DirectoryEntry("/data/documents/company"));
    }

    @Override
    protected void clean(FileSystem fileSystem) {
        fileSystem.delete("/data/documents/company");
        fileSystem.delete("/data/documents/document1.txt");
        fileSystem.delete("/data/documents/document2.txt");
        fileSystem.delete("/data/documents");
        fileSystem.delete("/data/foobar1.txt");
        fileSystem.delete("/data/foobar2.txt");
        fileSystem.delete("/data");
    }

    private void assertContainsFileWithName(List<Map<String, Serializable>> files, String expectedFileName) {
        for (Map<String, Serializable> file : files) {
            if (file.get("name").equals(expectedFileName)) return;
        }
        fail("Could not find file with name=" + expectedFileName);
    }

    private void assertContainsFileWithName(List<Map<String, Serializable>> files, String expectedFileName, String expectedPath) {
        for (Map<String, Serializable> file : files) {
            if (file.get("name").equals(expectedFileName) &&
                    file.get("path").equals(expectedPath)) return;
        }
        fail("Could not find file with name=" + expectedFileName + ", and path=" + expectedPath);
    }
}