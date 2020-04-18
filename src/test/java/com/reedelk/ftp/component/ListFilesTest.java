package com.reedelk.ftp.component;

import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.flow.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ListFilesTest {

    @Mock
    private FlowContext context;

    private static FakeFtpServer fakeFtpServer;
    private static final String TEST_HOST = "localhost";
    private static final String TEST_USERNAME = "testUser";
    private static final String TEST_PASSWORD = "testPassword";

    @BeforeAll
    static void setUpAll() {
        fakeFtpServer = new FakeFtpServer();
        fakeFtpServer.addUserAccount(new UserAccount(TEST_USERNAME, TEST_PASSWORD, "/data"));

        FileSystem fileSystem = new UnixFakeFileSystem();
        fileSystem.add(new DirectoryEntry("/data"));
        fileSystem.add(new FileEntry("/data/foobar.txt", "abcdef 1234567890"));

        fakeFtpServer.setFileSystem(fileSystem);
        fakeFtpServer.setServerControlPort(0);
        fakeFtpServer.start();
    }

    @AfterAll
    static void tearDownAll() {
        if (fakeFtpServer != null) {
            fakeFtpServer.stop();
        }
    }

    private ListFiles component;

    @BeforeEach
    void setUp() {
        ConnectionConfiguration configuration = new ConnectionConfiguration();
        configuration.setPort(fakeFtpServer.getServerControlPort());
        configuration.setHost(TEST_HOST);
        configuration.setUsername(TEST_USERNAME);
        configuration.setPassword(TEST_PASSWORD);
        component = new ListFiles();
        component.setConfiguration(configuration);
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
    }

    static class TestComponent implements ProcessorSync {

        @Override
        public Message apply(FlowContext flowContext, Message message) {
            throw new UnsupportedOperationException("Test only component");
        }
    }

}