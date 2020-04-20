package com.reedelk.ftp.component;

import com.reedelk.runtime.api.flow.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.ScriptEngineService;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicValue;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
abstract class AbstractTest {

    private static FakeFtpServer fakeFtpServer;

    protected static final int TEST_PORT = 0;
    protected static final String TEST_HOST = "localhost";
    protected static final String TEST_USERNAME = "testUser";
    protected static final String TEST_PASSWORD = "testPassword";
    private static UnixFakeFileSystem fileSystem;

    @Mock
    protected FlowContext context;
    @Mock
    protected ScriptEngineService scriptEngine;

    protected static ConnectionConfiguration connection;

    @BeforeAll
    static void setUpAll() {
        fakeFtpServer = new FakeFtpServer();
        fakeFtpServer.addUserAccount(new UserAccount(TEST_USERNAME, TEST_PASSWORD, "/data"));

        fileSystem = new UnixFakeFileSystem();

        fakeFtpServer.setFileSystem(fileSystem);
        fakeFtpServer.setServerControlPort(TEST_PORT);
        fakeFtpServer.start();

        connection = new ConnectionConfiguration();
        connection.setPort(getServerPort());
        connection.setType(ConnectionType.FTP);
        connection.setHost(TEST_HOST);
        connection.setUsername(TEST_USERNAME);
        connection.setPassword(TEST_PASSWORD);
    }

    @BeforeEach
    void setUp() {
        configure(fileSystem);
        lenient().doAnswer(invocation -> {
            DynamicValue<?> dynamicValue = invocation.getArgument(0);
            return Optional.ofNullable(dynamicValue.value());
        }).when(scriptEngine).evaluate(any(DynamicValue.class), eq(context), any(Message.class));
    }

    @AfterEach
    void tearDown() {
        clean(fileSystem);
    }

    @AfterAll
    static void tearDownAll() {
        if (fakeFtpServer != null) {
            fakeFtpServer.stop();
        }
    }

    protected void configure(FileSystem fileSystem) {
        // Subclasses might optionally extend it.
    }

    protected void clean(FileSystem fileSystem) {
        // Subclasses might optionally extend it.
    }

    protected FileSystem getFileSystem() {
        return fileSystem;
    }

    protected static int getServerPort() {
        return fakeFtpServer.getServerControlPort();
    }
}
