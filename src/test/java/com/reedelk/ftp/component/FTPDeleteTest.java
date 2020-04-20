package com.reedelk.ftp.component;

import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;

import static org.assertj.core.api.Assertions.assertThat;

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
    void shouldSuccessfullyDeleteFileFile() {
        // Given
        String path = "/data/foobar.txt";
        component.setPath(DynamicString.from(path));
        component.initialize();

        Message message = MessageBuilder.get(TestComponent.class).empty().build();

        // When
        Message actual = component.apply(context, message);

        // Then
        boolean deleteSuccess = actual.payload();
        assertThat(deleteSuccess).isTrue();

        boolean existFile = getFileSystem().exists(path);
        assertThat(existFile).isFalse();
    }

    @Override
    protected void configure(FileSystem fileSystem) {
        fileSystem.add(new DirectoryEntry("/data"));
        fileSystem.add(new FileEntry("/data/foobar.txt", "abcdef 1234567890"));
    }

    @Override
    protected void clean(FileSystem fileSystem) {
        fileSystem.delete("/data");
        fileSystem.delete("/data/foobar.txt");
    }
}