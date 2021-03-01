package de.codecentric.reedelk.ftp.internal.commons;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UtilsTest {

    @Test
    void shouldJoinPathCorrectly() {
        // Given
        String parent = "/documents/";
        String path = "file.pdf";

        // When
        String actual = Utils.joinPath(parent, path);

        // Then
        assertThat(actual).isEqualTo("/documents/file.pdf");
    }

    @Test
    void shouldJoinPathCorrectlyWhenPathStartsWithSlash() {
        // Given
        String parent = "/documents/";
        String path = "/file.pdf";

        // When
        String actual = Utils.joinPath(parent, path);

        // Then
        assertThat(actual).isEqualTo("/documents/file.pdf");
    }

    @Test
    void shouldJoinPathCorrectlyWhenParentIsNull() {
        // Given
        String parent = null;
        String path = "/file.pdf";

        // When
        String actual = Utils.joinPath(parent, path);

        // Then
        assertThat(actual).isEqualTo("/file.pdf");
    }

    @Test
    void shouldJoinPathCorrectlyWhenParentIsEmpty() {
        // Given
        String parent = "";
        String path = "/file.pdf";

        // When
        String actual = Utils.joinPath(parent, path);

        // Then
        assertThat(actual).isEqualTo("/file.pdf");
    }

    @Test
    void shouldJoinPathCorrectlyWhenParentIsEmptyAndPathDoesNotStartWithSlash() {
        // Given
        String parent = "";
        String path = "file.pdf";

        // When
        String actual = Utils.joinPath(parent, path);

        // Then
        assertThat(actual).isEqualTo("file.pdf");
    }
}