package com.reedelk.ftp.component;

import com.reedelk.runtime.api.commons.ByteArrayUtils;
import com.reedelk.runtime.api.converter.ConverterService;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.FileSystemEntry;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class FTPStoreTest extends AbstractTest {

    private FTPStore component;

    @Mock
    private ConverterService converterService;

    @BeforeEach
    void setUp() {
        super.setUp();
        ConnectionConfiguration configuration = new ConnectionConfiguration();
        configuration.setPort(getPort());
        configuration.setHost(TEST_HOST);
        configuration.setUsername(TEST_USERNAME);
        configuration.setPassword(TEST_PASSWORD);

        component = new FTPStore();
        component.scriptEngine = scriptEngine;
        component.converterService = converterService;
        component.setConfiguration(configuration);
    }

    @Test
    void shouldCorrectlyUploadData() throws IOException {
        // Given
        component.setFileName(DynamicString.from("/myFile.txt"));
        component.initialize();
        String textData = "My data";
        Mockito.doReturn(textData.getBytes()).when(converterService).convert(textData, byte[].class);

        Message message = MessageBuilder.get(TestComponent.class)
                .withText(textData)
                .build();

        // When
        Message actual = component.apply(context, message);

        // Then
        assertThat(actual).isNotNull();

        FileSystem fileSystem = getFileSystem();
        FileSystemEntry entry = fileSystem.getEntry("/myFile.txt");
        assertThat(entry).isNotNull();
        assertThat(entry.getName()).isEqualTo("myFile.txt");

        FileEntry myFile = (FileEntry) entry;
        try (InputStream inputStream = myFile.createInputStream()) {
            byte[] data = ByteArrayUtils.from(inputStream);
            assertThat(new String(data)).isEqualTo(textData);
        }
    }

    @Override
    protected void configure(FileSystem fileSystem) {
        fileSystem.add(new DirectoryEntry("/data"));
    }

    @Override
    protected void clean(FileSystem fileSystem) {
        fileSystem.delete("/data");
        fileSystem.delete("/data/foobar.txt");
    }
}
