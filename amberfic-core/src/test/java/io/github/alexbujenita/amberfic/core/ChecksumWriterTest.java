package io.github.alexbujenita.amberfic.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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

    @Test
    void writesCorrectlyToAFile(@TempDir Path path) throws IOException {
        // Given a path for the result output
        final Path outputFile = path.resolve("amberfic_checksum.sha512.tmp");
        final ChecksumEntry a = new ChecksumEntry("hash_123", "café.png");

        // when we call the writer
        final ChecksumWriter checksumWriter = new ChecksumWriter(List.of(a));
        checksumWriter.write(outputFile);

        // then we have the desired output
        final String expected = "hash_123  café.png\n";

        assertThat(outputFile).hasBinaryContent(expected.getBytes(StandardCharsets.UTF_8));
    }
}
