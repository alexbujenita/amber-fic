package io.github.alexbujenita.amberfic.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
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

    @Test
    void preservesExistingManifestWhenWritingFails(@TempDir Path path) throws IOException {
        // given an existing checksum file
        final Path checksumFile = Files.createFile(path.resolve("amberfic_checksum.sha512"));
        final String entry = "old_hash  existing.jpg\n";

        try (BufferedWriter writer = Files.newBufferedWriter(checksumFile)) {
            writer.write(entry);
        }

        // when we call the writer with write(Writer w) throwing
        final ChecksumWriter checksumWriter = new ChecksumWriter(List.of(new ChecksumEntry("hash_123", "random.jpg"))) {
            @Override
            void write(Writer writer) throws IOException {
                writer.write("writing...");
                throw new IOException("Error in writing");
            }
        };

        // then it throws
        assertThatThrownBy(() -> checksumWriter.write(checksumFile)).isInstanceOf(IOException.class);

        // and the original file is unaffected
        assertThat(checksumFile).hasBinaryContent(entry.getBytes(StandardCharsets.UTF_8));

        // and no temporary file remains
        try (Stream<Path> filesStream = Files.list(path)) {
            List<Path> list = filesStream.toList();
            assertThat(list).containsExactly(checksumFile);
        }
    }

    @Test
    void replacesExistingManifestWithoutLeavingTemporaryFiles(@TempDir Path path) throws IOException {
        // given an existing checksum file
        final Path checksumFile = Files.createFile(path.resolve("amberfic_checksum.sha512"));
        final String entry = """
                hash_456  a.png
                hash_123  sub/b.png
                hash_abc  sub/docs/some.pdf
                """;

        try (BufferedWriter writer = Files.newBufferedWriter(checksumFile)) {
            writer.write(entry);
        }

        // when we call the writer
        final ChecksumWriter checksumWriter = new ChecksumWriter(List.of(new ChecksumEntry("hash_xyz", "random.jpg")));
        checksumWriter.write(checksumFile);

        // then the checksum file has the correct entries
        final String expected = "hash_xyz  random.jpg\n";
        assertThat(checksumFile).hasBinaryContent(expected.getBytes(StandardCharsets.UTF_8));

        // and no temporary file remains
        try (Stream<Path> filesStream = Files.list(path)) {
            List<Path> list = filesStream.toList();
            assertThat(list).containsExactly(checksumFile);
        }
    }
}
