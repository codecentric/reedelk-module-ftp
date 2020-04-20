package com.reedelk.ftp.component;

import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


class FTPListTest extends AbstractTest {

    private FTPList component;

    @BeforeEach
    void setUp() {
        super.setUp();
        component = new FTPList();
        component.setConnection(connection);
    }

    @Test
    void shouldListFilesCorrectly() {
        // Given
        component.initialize();

        Message message = MessageBuilder.get(TestComponent.class)
                .empty()
                .build();

        // When
        Message actual = component.apply(context, message);

        // Then
        List<Map<String, Serializable>> payload = actual.payload();
        assertThat(payload).hasSize(1);

        Map<String, Serializable> fileEntry = payload.get(0);
        assertThat(fileEntry).containsEntry("name", "foobar.txt");
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