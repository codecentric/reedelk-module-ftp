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

public class FTPRetrieveTest extends AbstractTest {

    private FTPRetrieve component;

    @BeforeEach
    void setUp() {
        super.setUp();
        ConnectionConfiguration configuration = new ConnectionConfiguration();
        configuration.setPort(getPort());
        configuration.setHost(TEST_HOST);
        configuration.setUsername(TEST_USERNAME);
        configuration.setPassword(TEST_PASSWORD);
        component = new FTPRetrieve();
        component.scriptEngine = scriptEngine;
        component.setConnection(configuration);
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

    @Test
    void shouldRetrieveFileContentCorrectly() {
        // Given
        component.setPath(DynamicString.from("/data/foobar.txt"));
        component.initialize();

        Message message = MessageBuilder.get(TestComponent.class).empty().build();

        // When
        Message actual = component.apply(context, message);

        // Then
        byte[] data = actual.payload();
        assertThat(new String(data)).isEqualTo("abcdef 1234567890");
    }
}
