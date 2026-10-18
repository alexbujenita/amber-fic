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
        final IOException errorInWriting = new IOException("Error in writing");
        final ChecksumWriter checksumWriter = new ChecksumWriter(List.of(new ChecksumEntry("hash_123", "random.jpg"))) {
            @Override
            void write(Writer writer) throws IOException {
                writer.write("writing...");
                throw errorInWriting;
            }
        };

        // then it throws
        assertThatThrownBy(() -> checksumWriter.write(checksumFile)).isEqualTo(errorInWriting);

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
        // given an existing checksum file in a nested dir
        Path nestedDir = Files.createDirectories(path.resolve("nested"));
        final Path checksumFile = Files.createFile(nestedDir.resolve("amberfic_checksum.sha512"));
        final String entry = """
                hash_456  a.png
                hash_123  sub/b.png
                hash_abc  sub/docs/some.pdf
                """;

        try (BufferedWriter writer = Files.newBufferedWriter(checksumFile)) {
            writer.write(entry);
        }

        // when we call the writer
        final Path relativeChecksumFile = Path.of("").toAbsolutePath().relativize(checksumFile);
        final ChecksumWriter checksumWriter = new ChecksumWriter(List.of(new ChecksumEntry("hash_xyz", "random.jpg")));
        checksumWriter.write(relativeChecksumFile);

        // then the checksum file has the correct entries
        final String expected = "hash_xyz  random.jpg\n";
        assertThat(checksumFile).hasBinaryContent(expected.getBytes(StandardCharsets.UTF_8));

        // and no temporary file remains
        try (Stream<Path> filesStream = Files.walk(path)) {
            List<Path> list = filesStream.toList();
            assertThat(list).hasSize(3).contains(checksumFile);
        }
    }

    @Test
    void cleansUpTemporaryFileWhenMoveFails(@TempDir Path path) throws IOException {
        // given a file in a dir
        Path dir = Files.createDirectories(path.resolve("amberfic_checksum.sha512"));
        final Path sentinelFile = Files.createFile(dir.resolve("file_must_be_protected_all_all_costs"));
        final String entry = "nothing to see here\n";

        try (BufferedWriter writer = Files.newBufferedWriter(sentinelFile)) {
            writer.write(entry);
        }

        // when we call the writer, and it throws (passing a dir as destination)
        final ChecksumWriter checksumWriter = new ChecksumWriter(List.of(new ChecksumEntry("hash_xyz", "random.jpg")));
        assertThatThrownBy(() -> checksumWriter.write(dir)).isInstanceOf(IOException.class);

        // then the check file is unchanged
        assertThat(sentinelFile).hasBinaryContent(entry.getBytes(StandardCharsets.UTF_8));

        // and no temporary file remains
        try (Stream<Path> filesStream = Files.walk(path)) {
            List<Path> list = filesStream.toList();
            assertThat(list).hasSize(3).contains(sentinelFile);
        }
    }
}
