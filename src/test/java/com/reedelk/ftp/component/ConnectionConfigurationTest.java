package com.reedelk.ftp.component;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConnectionConfigurationTest {

    @Test
    void shouldReturnEmptyStringWhenNotDefined() {
        // Given
        ConnectionConfiguration configuration = new ConnectionConfiguration();

        // Expect
        String workingDir = configuration.getWorkingDir();
        assertThat(workingDir).isEmpty();
    }

    @Test
    void shouldReturnCorrectWorkingDirWhenNotEmpty() {
        // Given
        ConnectionConfiguration configuration = new ConnectionConfiguration();
        configuration.setWorkingDir("/documents");

        // Expect
        String workingDir = configuration.getWorkingDir();
        assertThat(workingDir).isEqualTo("/documents/");
    }

    @Test
    void shouldReturnCorrectWorkingDirWhenNotEmptyAndEndsWithFrontSlash() {
        // Given
        ConnectionConfiguration configuration = new ConnectionConfiguration();
        configuration.setWorkingDir("/documents/");

        // Expect
        String workingDir = configuration.getWorkingDir();
        assertThat(workingDir).isEqualTo("/documents/");
    }
}