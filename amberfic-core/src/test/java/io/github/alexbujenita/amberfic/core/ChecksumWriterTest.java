package io.github.alexbujenita.amberfic.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChecksumWriterTest {
    @Test
    void itHasTheEntriesInCorrectOrder() throws IOException {
        // Given two results
        final ChecksumEntry a = new ChecksumEntry("hash_123", "sub/b.png");
        final ChecksumEntry b = new ChecksumEntry("hash_456", "a.png");
        final StringWriter writer = new StringWriter();

        // when we call the writer
        final ChecksumWriter checksumWriter = new ChecksumWriter(List.of(a, b));
        checksumWriter.write(writer);

        // then we have the desired output
        final String expected = """
                hash_456  a.png
                hash_123  sub/b.png
                """;
        assertThat(writer).hasToString(expected);
    }
}
